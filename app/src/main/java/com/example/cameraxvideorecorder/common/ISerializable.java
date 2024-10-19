package com.example.cameraxvideorecorder.common;

import static com.example.cameraxvideorecorder.common.Logcat.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface ISerializable {
    public default String serialize() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log(e.toString());
            throw e;
        }
    }

    public static String serialize(ISerializable[] items) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            log(e.toString());
            throw e;
        }
    }
}
