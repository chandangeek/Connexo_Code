package com.elster.jupiter.util.json;

/**
 * Service interface for classes that serialize java Objects into Json Strings, and deserialze Json Strings into java Objects.
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
     * @throws JsonParseException
     */
    <T> T deserialize(String json, Class<T> type) throws JsonParseException;

    /**
     * Deserialize the byte[] payload as an object of the given type, by interpreting the bytes as a Json String
     * @param payload
     * @param type
     * @param <T>
     * @return
     * @throws JsonParseException
     */
    <T> T deserialize(byte[] payload, Class<T> type) throws JsonParseException;
}
