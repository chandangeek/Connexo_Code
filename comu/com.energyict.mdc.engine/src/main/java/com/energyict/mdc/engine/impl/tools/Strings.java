package com.energyict.mdc.engine.impl.tools;

/**
 * Provides various methods to manipulate/test Strings.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-06 (16:13)
 */
public class Strings {

    /**
     * Returns the length of the specified String in a null-safe way.
     * The length of a <code>null</code> String is zero.
     *
     * @param strings The String
     * @return The length of the String
     */
    public static int length (String... strings) {
        if (strings == null) {
            return 0;
        }
        else {
            int totalLength = 0;
            for (String string : strings) {
                if (string != null) {
                    totalLength = totalLength + string.length();
                }
            }
            return totalLength;
        }
    }

}