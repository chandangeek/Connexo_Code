package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonFilterParametersBean {

    private JsonQueryFilter jsonQueryFilter;

    public JsonFilterParametersBean(JsonQueryFilter jsonQueryFilter) {
        this.jsonQueryFilter = jsonQueryFilter;
    }

    public Optional<List<String>> getStringList(String key) {
        List<String> values = jsonQueryFilter.getStringList(key);
        if (values != null && !values.isEmpty()) {
            return Optional.of(values);
        }
        return Optional.empty();
    }

    public Optional<Instant> getInstant(String key) {
        String value = jsonQueryFilter.getString(key);
        if (value != null) {
            return Optional.of(Instant.ofEpochMilli(Long.parseLong(value)));
        }
        return Optional.empty();

    }
}
