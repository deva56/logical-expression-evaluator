package com.leapwise.logicalexpressionevaluator.aplication_services.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leapwise.logicalexpressionevaluator.aplication_services.types.exceptions.MalformedJsonException;

import java.util.Map;

public class JsonUtil {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public static boolean isValidJson(Object jsonBody) {
        try {
            if (jsonBody instanceof String) {
                JsonNode jsonNode = convertStringToJson((String) jsonBody);
                convertJsonDataToMap(jsonNode);
                return true;
            } else if (jsonBody instanceof Map<?, ?>) {
                convertJsonDataToMap(jsonBody);
                return true;
            } else if (jsonBody instanceof JsonNode) {
                convertJsonDataToMap(jsonBody);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static JsonNode convertStringToJson(String string) throws Exception {
        try {
            return objectMapper.readTree(string);
        } catch (Exception e) {
            throw new Exception("Provided json string couldn't be converted to jsonObject. " + e);
        }
    }


    public static Map<String, Object> convertJsonDataToMap(Object jsonData) {
        try {
            if (jsonData instanceof String) {
                JsonNode jsonNode = convertStringToJson((String) jsonData);
                return objectMapper.convertValue(jsonNode, Map.class);
            } else if (jsonData instanceof Map<?, ?>) {
                return objectMapper.convertValue(jsonData, Map.class);
            } else if (jsonData instanceof JsonNode) {
                return objectMapper.convertValue(jsonData, Map.class);
            } else {
                throw new MalformedJsonException("Provided json data couldn't be converted to needed format.");
            }
        } catch (Exception e) {
            throw new MalformedJsonException("Provided json data couldn't be converted to needed format.", e);
        }
    }
}
