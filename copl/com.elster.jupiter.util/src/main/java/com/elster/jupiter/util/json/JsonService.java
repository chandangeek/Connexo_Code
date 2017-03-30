/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

/**
 * Service interface for classes that serialize java Objects into Json Strings, and deserialize Json Strings into java Objects.
 */
public interface JsonService {

    /**
     * Serialize to Json String
     * @param oject
     * @return A Json String
     */
    String serialize(Object oject);

    /**
     * Deserialize Json String as an object of the given type.
     * @param json
     * @param type
     * @param <T>
     * @return
     * @throws JsonDeserializeException
     */
    <T> T deserialize(String json, Class<T> type) throws JsonDeserializeException;

    /**
     * Deserialize the byte[] payload as an object of the given type, by interpreting the bytes as a Json String
     * @param payload
     * @param type
     * @param <T>
     * @return
     * @throws JsonDeserializeException
     */
    <T> T deserialize(byte[] payload, Class<T> type) throws JsonDeserializeException;
}
