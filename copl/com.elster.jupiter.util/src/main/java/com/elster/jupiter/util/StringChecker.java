package com.elster.jupiter.util;

/**
 * Copyrights EnergyICT
 * Date: 29/05/13
 * Time: 10:44
 */
public class StringChecker extends ObjectChecker<String> {

    public StringChecker(String toCheck) {
        super(toCheck);
    }

    public boolean empty() {
        return toCheck == null || toCheck.isEmpty();
    }

    public boolean onlyWhiteSpace() {
        return !empty() && nonNullOnlyWhiteSpace();
    }

    public boolean emptyOrOnlyWhiteSpace() {
        return empty() || nonNullOnlyWhiteSpace();
    }

    private boolean nonNullOnlyWhiteSpace() {
        for (int i = 0; i < toCheck.length(); i++) {
            if (!Character.isWhitespace(toCheck.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
