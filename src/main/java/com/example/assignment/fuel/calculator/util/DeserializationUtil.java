package com.example.assignment.fuel.calculator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;

import javax.jms.Message;
import javax.jms.JMSException;
import javax.jms.TextMessage;

@UtilityClass
public class DeserializationUtil {
    private ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    /**
     * Deserialize the Text message body into Object of given class.
     * @param message Text JMS Message
     * @param clazz Output object class
     * @param <T> output object type
     * @return Object of provided class
     * @throws JMSException JMSException
     * @throws JsonProcessingException JsonProcessingException
     */
    public static <T> T getObjectFromMessage(Message message, Class<T> clazz)
            throws JMSException, JsonProcessingException {
        return objectMapper.readValue(((TextMessage) message).getText(), clazz);
    }
}
