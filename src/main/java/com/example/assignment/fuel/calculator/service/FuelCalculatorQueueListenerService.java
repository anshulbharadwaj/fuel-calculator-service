package com.example.assignment.fuel.calculator.service;

import com.assignment.fuel.service.resource.EventDetail;
import com.example.assignment.fuel.calculator.response.FuelPriceResponse;
import com.example.assignment.fuel.calculator.util.DeserializationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import javax.jms.Message;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class FuelCalculatorQueueListenerService {
    private final FuelCalculatorService fuelCalculatorService;

    private static final BigDecimal TIME_INTERVAL_BETWEEN_TWO_EVENTS = BigDecimal.valueOf(120);

    /**
     * This method consumes messages from Fuel Detail Queue.
     *
     * @param message message
     */
    @JmsListener(destination = "FUEL.DETAIL.QUEUE")
    public void receiveMessage(Message message) {
        EventDetail eventDetail = null;

        Map<String, LocalDateTime> cityMapping = new HashMap();
        Map<String, String> fuelPriceMapping = new HashMap<>();
        Map<String, Boolean> cityFuelLidMapping = new HashMap();
        Map<String, Integer> cityCounterMapping = new HashMap<>();

        long timeDifferenceInSeconds = 0l;

        try {
            eventDetail = DeserializationUtil.getObjectFromMessage(message, EventDetail.class);
            log.info("Received Message in Fuel Detail queue: {} ", eventDetail);

            cityMapping.put(eventDetail.getCity(), eventDetail.getLocalDateTime());
            cityFuelLidMapping.put(eventDetail.getCity(), eventDetail.isFuelLid());

            if (cityFuelLidMapping.containsKey(eventDetail.getCity())
                    && BooleanUtils.isFalse(cityFuelLidMapping.get(eventDetail.getCity()))) {
                cityCounterMapping.put(eventDetail.getCity(), 1);
            }
            if (cityMapping.containsKey(eventDetail.getCity())
                    && BooleanUtils.isFalse(cityFuelLidMapping.get(eventDetail.getCity()))) {
                LocalDateTime initialDateTime = cityMapping.get(eventDetail.getCity());
                LocalDateTime currentDateTime = LocalDateTime.now();
                timeDifferenceInSeconds = TimeUnit.MILLISECONDS
                        .toSeconds(Duration.between(initialDateTime, currentDateTime).toMillis());
            }

            if (BooleanUtils.isTrue(eventDetail.isFuelLid())
                    && StringUtils.isNotEmpty(eventDetail.getCity())) {

                Optional<FuelPriceResponse> fuelPriceResponse =
                        fuelCalculatorService.fetchFuelPriceByCityName(eventDetail.getCity());

                if (fuelPriceResponse.isPresent()) {
                    fuelPriceMapping.put(eventDetail.getCity(), fuelPriceResponse.get().getPetrol());
                    calculateFuelPriceForInputCityBetweenFixedInterval(new BigDecimal(fuelPriceMapping
                            .get(eventDetail.getCity())));
                } else {
                    log.error("Fuel Price API() returned empty response");
                }

                if (!CollectionUtils.isEmpty(cityCounterMapping)) {
                    if (cityCounterMapping.get(eventDetail.getCity()) > 0) {
                        BigDecimal fuelPriceForCity = calculateFuelPriceForClosedFuelLidForSameCity(new BigDecimal(fuelPriceMapping
                                .get(eventDetail.getCity())), timeDifferenceInSeconds);
                        log.info("Final Calculated Fuel Price is:: ", fuelPriceForCity);
                    }
                }
            }
        } catch (Exception exception) {
            log.error("Error occurred while processing Event detail message ", exception);
        }
    }

    /**
     * This method calculates fuel price for a given city
     *
     * @param fuelPrice fuel price received from fuel price API
     * @param duration  duration is calculated between true and false event i.e.
     *                  difference between event time - fuelLid open and closed.
     * @return
     */
    private BigDecimal calculateFuelPriceForClosedFuelLidForSameCity(BigDecimal fuelPrice, long duration) {
        BigDecimal price = BigDecimal.ZERO;
        if (duration > 0) {
            long fuelInTank = duration / 30;
            log.info("Fuel Filled in Tank in litres:: {}", fuelInTank);
            price = fuelPrice.multiply(BigDecimal.valueOf(fuelInTank));
            log.info("Final Calculated Fuel Price is:: ", price);
        }
        return price;
    }

    /**
     * This method calculates price for a given input city with duration of filling the tank is equivalent to
     * receiving of another event from fuel service
     *
     * @param fuelPrice
     * @return fuel price.
     */
    private BigDecimal calculateFuelPriceForInputCityBetweenFixedInterval(BigDecimal fuelPrice) {
        long fuelInTank = 120 / 30;
        log.info("Fuel Filled in Tank for fixed interval event in litres:: {},", fuelInTank);
        BigDecimal price = fuelPrice.multiply(BigDecimal.valueOf(fuelInTank));
        log.info(" Fuel Price for fixed interval event is Rs. {} ", price);

        return price;

    }

}
