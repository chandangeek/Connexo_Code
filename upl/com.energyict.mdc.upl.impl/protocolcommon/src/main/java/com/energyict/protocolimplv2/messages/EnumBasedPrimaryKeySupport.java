package com.energyict.protocolimplv2.messages;

/**
 * Support building and parsing primary key values for enum based factories.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-01 (14:22)
 */
final class EnumBasedPrimaryKeySupport {

    static final String CARDINAL_REGEX = "#";
    static final String DOLLAR_REGEX = "\\$";
    static final String DOLLAR_SIGN = "$";

    /**
     * We need to <i>cleanup</i> the className as it might contain rubbish from inner-class enum variables ...
     * (ex. com.energyict.mdc.messages.DeviceMessageTestCategories$1 -> this is not reversible to the original enum value)
     * <p/>
     * We should check if the className contains dollar ('{@link #DOLLAR_SIGN}') signs and a numerical value after the sign.
     * If this occurs, then the last part may be skipped, otherwise it may not be skipped!
     *
     * @param dirtyClassName the className containing inner-class enum rubbish
     * @return the <i>cleanClassName</i> where we can perform working <code>Class.forName(cleanClassName)</code> on
     */
    static String cleanUpClassName(String dirtyClassName) {
        String[] parts = dirtyClassName.split(DOLLAR_REGEX);
        if (isNumeric(parts[parts.length - 1])) {
            return dirtyClassName.substring(0, dirtyClassName.lastIndexOf(DOLLAR_SIGN));
        } else {
            return dirtyClassName;
        }
    }

    private static boolean isNumeric(String possibleInt) {
        if (possibleInt != null) {
            try {
                Double.parseDouble(possibleInt);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    // Hide utility class constructor
    private EnumBasedPrimaryKeySupport() {}
}