package com.elster.jupiter.util.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LiteralValueMatcher implements ValueMatcher {

    private final List<String> path;
    private List<String> results;

    public LiteralValueMatcher(List<String> path) {
        this.path = new ArrayList<>(path);
    }

    @Override
    public void addResult(String value) {
        results.add(value);
    }

    @Override
    public List<String> getValues() {
        return Collections.unmodifiableList(results);
    }

    @Override
    public boolean matches(List<String> path) {
        return this.path.equals(path);
    }
}
