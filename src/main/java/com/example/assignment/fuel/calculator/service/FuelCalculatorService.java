package com.example.assignment.fuel.calculator.service;

import com.example.assignment.fuel.calculator.common.StateCodes;
import com.example.assignment.fuel.calculator.model.Country;
import com.example.assignment.fuel.calculator.response.FuelPriceResponse;
import com.example.assignment.fuel.calculator.util.FuelCalculatorUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Optional;

@Service
@Slf4j
public class FuelCalculatorService {

    private static final String INPUT_FILE_INDIAN_CITIES = "IndianCities.json";
    private static final String HEADER_API_KEY = "x-rapidapi-key";
    private static final String HEADER_API_HOST = "x-rapidapi-host";
    private static final String HEADER_API_KEY_VALUE = "2e3ac1d5bamsh0fab9f0e3cb8b3ap13922bjsn735fb921f275";
    private static final String HEADER_API_HOST_VALUE = "daily-fuel-prices-india.p.rapidapi.com";
    private static final String FUEL_PRICE_API_URL = "https://daily-fuel-prices-india.p.rapidapi.com/api/proxy/hp/states/";

    /**
     * This method calls Fuel Service API and calculates Fuel Price
     *
     * @param city city for which fuel price needs to be calculated
     */
    public Optional<FuelPriceResponse> fetchFuelPriceByCityName(String city) {
        Optional<FuelPriceResponse> filteredFuelPricesByCity = Optional.empty();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //TODO Implement caching for the Fuel Price API Response
            HttpResponse<String> response = callFuelPriceApi(findStateCodeForInputCity(city));
            log.info("Received response is:: {} ", response.body());
            FuelPriceResponse[] fuelPrices = objectMapper.readValue(response.body(), FuelPriceResponse[].class);

            filteredFuelPricesByCity = Arrays
                    .stream(fuelPrices)
                    .filter(fuel -> fuel.getTownname().equalsIgnoreCase(city))
                    .findFirst();

        } catch (JsonProcessingException exception) {
            log.error("Error occurred while processing city details json {}", exception);
        } catch (IOException | InterruptedException exception) {
            log.error("Error occurred while retrieving fuel prices {}", exception);
        }
        return filteredFuelPricesByCity;
    }

    /**
     * This method finds state code for the input location (city)
     *
     * @param city city for which fuel price needs to be looked up
     * @return state code for input city
     * @throws JsonProcessingException jsonProcessingException
     */
    private String findStateCodeForInputCity(String city) throws JsonProcessingException {
        String stateName = "";
        JSONArray jsonArray = new JSONArray(FuelCalculatorUtil.readJsonFile(INPUT_FILE_INDIAN_CITIES));

        ObjectMapper objectMapper = new ObjectMapper();
        Country[] countryDetail = objectMapper.readValue(jsonArray.toString(), Country[].class);
        Optional<Country> filteredCountryDetails = Arrays.stream(countryDetail)
                .filter(cityName -> cityName.getName().equalsIgnoreCase(city))
                .findFirst();
        if (filteredCountryDetails.isPresent()) {
            stateName = filteredCountryDetails.get().getState();
        }

        String inputStateName = stateName.replace(" ", "_");
        return StateCodes.valueOf(inputStateName).getStateCode();
    }

    /**
     * This method builds Http Request for Fuel Price API
     *
     * @param stateCode state code for given input city
     * @return Http Request
     */
    private HttpRequest buildRequestForFuelPrice(String stateCode) {
        return HttpRequest.newBuilder()
                .uri(URI.create(FUEL_PRICE_API_URL + stateCode))
                .header(HEADER_API_KEY, HEADER_API_KEY_VALUE)
                .header(HEADER_API_HOST, HEADER_API_HOST_VALUE)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
    }

    /**
     * This method calls Fuel Price API for the given city using state code and returns
     * response received from the service
     *
     * @param stateCode state code
     * @return Http Response of Fuel price API
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    private HttpResponse callFuelPriceApi(String stateCode) throws IOException, InterruptedException {
        HttpRequest request = buildRequestForFuelPrice(stateCode);
        log.info("Request created is:: {}", request.uri());
        HttpResponse<String> httpResponse = HttpClient
                .newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        return httpResponse;
    }

}
