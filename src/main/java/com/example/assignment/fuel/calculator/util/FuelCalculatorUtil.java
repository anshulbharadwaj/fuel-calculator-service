package com.example.assignment.fuel.calculator.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;

@UtilityClass
@Slf4j
public class FuelCalculatorUtil {

    /**
     *  This is a utility method for reading input json file
     * @return given input json into a String
     */
    public static String readJsonFile(String inputFileName) {
        String json = "";
        try {
            json = StreamUtils
                    .copyToString(new ClassPathResource(inputFileName).getInputStream(), Charset.defaultCharset());
        } catch (Exception exception) {
            log.error("Error occurred while reading json file {} ", exception);
        }
        return json;
    }
}
