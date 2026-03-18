package com.tpg.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    public static <T> T convertToObject(String content, Class<T> t) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            return mapper.readValue(content, t);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> String getJsonAsString(T t) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> List<T> convertToList(String content, Class<T> t) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            return mapper.readValue(content, mapper.getTypeFactory().constructCollectionType(List.class, t));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <P, T> P getParameterizedObject(Class<P> genericType, Class<T> parameterType, String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            JavaType type = mapper.getTypeFactory().constructParametricType(genericType, parameterType);
            return mapper.readValue(jsonString, type);
        } catch (IOException ie) {
            throw new IllegalStateException("Json parsing failed or some other format encountered", ie);
        }
    }
}
