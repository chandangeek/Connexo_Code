package com.energyict.mdc.common;

import java.io.Serializable;
import java.math.BigInteger;

import static com.elster.jupiter.util.Checks.is;

/**
 * User: gde
 * Date: 17/04/13
 */
public class HexString implements com.energyict.mdc.upl.properties.HexString, Comparable<com.energyict.mdc.upl.properties.HexString>, Serializable {

    private String content;

    public HexString() {
        super();
    }

    public HexString(String hexString) {
        if (!is(hexString).emptyOrOnlyWhiteSpace()) {
            try {
                new BigInteger(hexString.toUpperCase(), 16);
                setContent(hexString);
            } catch (NumberFormatException x) {
                throw new IllegalArgumentException("Invalid HexString");
            }
        }
    }

    public boolean isValid() {
        return true;
    }

    public String getContent() {
        return content;
    }

    protected void setContent(String hexString) {
        content = hexString.toUpperCase();
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    public int length() {
        return (content == null ? 0 : content.length());
    }

    public int compareTo(com.energyict.mdc.upl.properties.HexString other) {
        if (getContent() == null) {
            if (other.getContent() == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (other.getContent() == null) {
            if (getContent() == null) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return getContent().compareTo(other.getContent());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof com.energyict.mdc.upl.properties.HexString)) {
            return false;
        }
        com.energyict.mdc.upl.properties.HexString other = (com.energyict.mdc.upl.properties.HexString) o;
        if (getContent() == null) {
            return other.getContent() == null;
        }
        if (other.getContent() == null) {
            return getContent() == null;
        }
        return getContent().equals(other.getContent());
    }

    @Override
    public int hashCode() {
        if (content != null) {
            return content.hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return getContent();
    }

}