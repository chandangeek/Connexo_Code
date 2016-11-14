package com.energyict.mdc.upl.messages;

public abstract class AbstractDeviceMessagePrimaryKey {

    public static final String CARDINAL_REGEX = "#";
    public static final String DOLLAR_REGEX = "\\$";
    public static final String DOLLAR_SIGN = "$";

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
    protected String cleanUpClassName(String dirtyClassName) {
        String[] parts = dirtyClassName.split(DeviceMessageCategoryPrimaryKey.DOLLAR_REGEX);
        if (isGivenStringNumeric(parts[parts.length - 1])) {
            return dirtyClassName.substring(0, dirtyClassName.lastIndexOf(DeviceMessageCategoryPrimaryKey.DOLLAR_SIGN));
        } else {
            return dirtyClassName;
        }
    }

    protected boolean isGivenStringNumeric(String possibleInt) {
        if (possibleInt != null) {
            try {
                Double.parseDouble(possibleInt);
                return true;
            } catch (NumberFormatException e) {
                // we eat it because this determines that the given string is not a number ...
                return false;
            }
        } else {
            return false;
        }
    }

}