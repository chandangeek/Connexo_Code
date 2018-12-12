/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InstallIdentifier {

    private static Pattern IDENTIFIER_VALID_PATTERN = Pattern.compile("[A-Z][A-Z0-9]{2}");

    private final String application;
    private final String identifier;

    private InstallIdentifier(String application, String identifier) {
        this.application = application;
        Matcher matcher = IDENTIFIER_VALID_PATTERN.matcher(identifier);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Install identifier '" + identifier + "' does not match pattern " + IDENTIFIER_VALID_PATTERN.toString());
        }
        this.identifier = identifier;
    }

    public static InstallIdentifier identifier(String application, String identifier) {
        return new InstallIdentifier(application, identifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstallIdentifier that = (InstallIdentifier) o;
        return Objects.equals(application, that.application) &&
                Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, identifier);
    }

    @Override
    public String toString() {
        return application + ':' + identifier;
    }

    public String application() {
        return application;
    }

    public String name() {
        return identifier;
    }
}
