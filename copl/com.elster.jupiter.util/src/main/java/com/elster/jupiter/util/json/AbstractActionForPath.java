package com.elster.jupiter.util.json;

import java.util.List;

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
