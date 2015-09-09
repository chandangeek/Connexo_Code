package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.impl.MessageSeeds;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;

import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@XmlRootElement
public class JsonQueryFilter {
    private static final String PROPERTY = "property";
    private static final String VALUE = "value";
    private static final Function<JsonNode, String> AS_STRING = node -> node != null ? node.textValue() : null;
    private static final Function<JsonNode, String> AS_JSON_STRING = node -> node != null && !node.isNull() ? node.toString() : null;
    private static final Function<JsonNode, Integer> AS_INT = node -> {
        if (node != null){
            Number number = node.numberValue();
            if (number != null){
                return number.intValue();
            }
        }
        return null;
    };
    private static final Function<JsonNode, Long> AS_LONG = node -> {
        if (node != null){
            Number number = node.numberValue();
            if (number != null){
                return number.longValue();
            }
        }
        return null;
    };
    private static final Function<JsonNode, Boolean> AS_BOOLEAN = node -> node != null ? node.asBoolean() : null;
    private static final Function<JsonNode, Instant> AS_INSTANT = node -> {
        if (node != null){
            Number number = node.numberValue();
            if (number != null){
                return Instant.ofEpochMilli(number.longValue());
            }
        }
        return null;
    };

    private final Map<String, JsonNode> filterProperties = new HashMap<>();

    public JsonQueryFilter(@QueryParam("filter") String source) {
        try {
            if (source != null) {
                JsonNode node = new ObjectMapper().readValue(new ByteArrayInputStream(source.getBytes()), JsonNode.class);
                if (node != null && node.isArray()) {
                    for (JsonNode singleFilter : node) {
                        JsonNode property = singleFilter.get(PROPERTY);
                        if(property!=null && property.textValue()!=null)
                            filterProperties.put(property.textValue(), singleFilter.get(VALUE));
                    }
                }
            }
        } catch (Exception ex){
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "filter");
        }
    }

    private <T> T unmarshalValue(String name, String value, XmlAdapter<String, T> adapter){
        try {
            return adapter.unmarshal(value);
        } catch (Exception e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, name);
        }
    }

    private Map<String, JsonNode> getFilterProperties() {
        return filterProperties;
    }

    public boolean hasFilters(){
        return !getFilterProperties().isEmpty();
    }

    public boolean hasProperty(String name){
        return getFilterProperties().get(name) != null;
    }

    public <T> T getProperty(String name, Function<JsonNode, T> mapper){
        return mapper.apply(getFilterProperties().get(name));
    }

    public String getComplexProperty(String name){
        return getProperty(name, AS_JSON_STRING);
    }

    public <T> T getProperty(String name, XmlAdapter<String, T> adapter) {
        return unmarshalValue(name, getString(name), adapter);
    }

    public String getString(String name){
        return getProperty(name, AS_STRING);
    }

    public Integer getInteger(String name){
        return getProperty(name, AS_INT);
    }

    public Long getLong(String name) {
        return getProperty(name, AS_LONG);
    }

    public Instant getInstant(String name) {
        return getProperty(name, AS_INSTANT);
    }

    public Range<Instant> getClosedRange(String from, String to) {
        Instant startedOnFrom = this.getInstant(from);
        Instant startedOnTo = this.getInstant(to);
        if (startedOnFrom==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_RANGE, from);
        }
        if (startedOnTo==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_RANGE, to);
        }
        if (startedOnFrom.isAfter(startedOnTo)) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_RANGE_FROM_AFTER_TO, from);
        }
        try {
            return Range.closed(startedOnFrom, startedOnTo);
        } catch (IllegalArgumentException e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_RANGE, from);
        }
    }

    public Boolean getBoolean(String name) {
        return getProperty(name, AS_BOOLEAN);
    }

    public <T> List<T> getPropertyList(String name, Function<JsonNode, T> mapper){
        JsonNode node = getFilterProperties().get(name);
        List<T> values = new ArrayList<>();
        if (node != null && node.isArray()){
            for (JsonNode value : node) {
                values.add(mapper.apply(value));
            }
        }
        return values;
    }

    public List<String> getPropertyList(String name){
        return getPropertyList(name, AS_JSON_STRING);
    }

    public <T> List<T> getPropertyList(String name, XmlAdapter<String, T> adapter) {
        return getPropertyList(name, AS_STRING.andThen(s -> unmarshalValue(name, s, adapter)));
    }

    public List<String> getStringList(String name){
        return getPropertyList(name, AS_STRING);
    }

    public List<Integer> getIntegerList(String name){
        return getPropertyList(name, AS_INT);
    }

    public List<Long> getLongList(String name){
        return getPropertyList(name, AS_LONG);
    }

    public List<Instant> getInstantList(String name){
        return getPropertyList(name, AS_INSTANT);
    }

    public List<Boolean> getBooleanList(String name){
        return getPropertyList(name, AS_BOOLEAN);
    }
}
