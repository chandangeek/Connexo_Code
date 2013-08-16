package com.elster.jupiter.util.json;

import java.util.List;

public interface ValueMatcher {
    List<String> getValues();

    void addResult(String value);

    boolean matches(List<String> path);
}
