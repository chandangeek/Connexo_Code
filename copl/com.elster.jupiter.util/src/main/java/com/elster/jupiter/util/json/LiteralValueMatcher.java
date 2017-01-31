/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import java.util.ArrayList;
import java.util.List;

/**
 * ValueMatcher that Matches on the entire path being literally the same as the one configured to match.
 */
public class LiteralValueMatcher implements ValueMatcher {

    private final List<String> path;

    /**
     * @param path the full literal path to match
     */
    public LiteralValueMatcher(List<String> path) {
        this.path = new ArrayList<>(path);
    }

    @Override
    public boolean matches(List<String> path) {
        return this.path.equals(path);
    }
}
