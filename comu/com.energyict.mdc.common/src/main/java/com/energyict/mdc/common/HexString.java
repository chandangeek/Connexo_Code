package com.energyict.mdc.common;

import com.elster.jupiter.util.Checks;

import java.io.Serializable;
import java.math.BigInteger;

import static com.elster.jupiter.util.Checks.*;

/**
 * User: gde
 * Date: 17/04/13
 */
public class HexString implements Comparable, Serializable, Nullable {

    private String content;

    public HexString () {
        super();
    }

    public HexString (String hexString) {
        if (is(hexString).emptyOrOnlyWhiteSpace()) {
            return;
        }
        else {
            try {
                new BigInteger(hexString.toUpperCase(), 16);
                this.content = hexString.toUpperCase();
            }
            catch (NumberFormatException x) {
                String invalidHexString = UserEnvironment.getDefault().getErrorMsg("invalidHexString");
                if (invalidHexString.startsWith(MultiBundleTranslator.MISSING_RESOURCE_PREFIX)) {
                    invalidHexString = "Invalid HexString";
                }
                throw new IllegalArgumentException(invalidHexString);
            }
        }
    }

    public String getContent () {
        return content;
    }

    public int compareTo (Object o) {
        HexString other = (HexString) o;
        if (getContent() == null) {
            if (other.getContent() == null) {
                return 0;
            }
            else {
                return -1;
            }
        }
        if (other.getContent() == null) {
            if (getContent() == null) {
                return 0;
            }
            else {
                return 1;
            }
        }
        return getContent().compareTo(other.getContent());
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof HexString)) {
            return false;
        }
        HexString other = (HexString) o;
        if (getContent() == null) {
            return other.getContent() == null;
        }
        if (other.getContent() == null) {
            return getContent() == null;
        }
        return getContent().equals(other.getContent());
    }

    @Override
    public int hashCode () {
        if (content != null) {
            return content.hashCode();
        }
        else {
            return 0;
        }
    }

    @Override
    public String toString () {
        return getContent();
    }

    @Override
    public boolean isNull () {
        return is(getContent()).emptyOrOnlyWhiteSpace();
    }

}