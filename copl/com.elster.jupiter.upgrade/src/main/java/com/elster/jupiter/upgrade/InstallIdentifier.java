package com.elster.jupiter.upgrade;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InstallIdentifier {

    private static Pattern IDENTIFIER_VALID_PATTERN = Pattern.compile("[A-Z]{3}");

    private final String identifier;

    private InstallIdentifier(String identifier) {
        Matcher matcher = IDENTIFIER_VALID_PATTERN.matcher(identifier);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        this.identifier = identifier;
    }

    public static final InstallIdentifier identifier(String identifier) {
        return new InstallIdentifier(identifier);
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
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }
}
