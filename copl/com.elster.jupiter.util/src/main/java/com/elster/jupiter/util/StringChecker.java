/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

/**
 * Part of fluent API, see Checks.
 */
public class StringChecker extends ObjectChecker<String> {

    public StringChecker(String toCheck) {
        super(toCheck);
    }

    public boolean empty() {
        return getToCheck() == null || getToCheck().isEmpty();
    }

    public boolean onlyWhiteSpace() {
        return !empty() && nonNullOnlyWhiteSpace();
    }

    public boolean emptyOrOnlyWhiteSpace() {
        return empty() || nonNullOnlyWhiteSpace();
    }

    private boolean nonNullOnlyWhiteSpace() {
        for (int i = 0; i < getToCheck().length(); i++) {
            if (!Character.isWhitespace(getToCheck().charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean containingIgnoringCase(String other) {
        return getToCheck().toUpperCase().contains(other.toUpperCase());
    }
}
