/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import java.util.List;

/**
 * Abstract ActionForPath implementation that filters paths using a ValueMatcher, and upon match call perform(), which is to be implemented by subclasses.
 */
public abstract class AbstractActionForPath implements ActionForPath {

    private final ValueMatcher valueMatcher;

    protected AbstractActionForPath(ValueMatcher valueMatcher) {
        this.valueMatcher = valueMatcher;
    }

    @Override
    public final void action(List<String> path, String value) {
        if (valueMatcher.matches(path)) {
            perform(value);
        }
    }

    protected abstract void perform(String value);
}
