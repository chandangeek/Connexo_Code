package com.energyict.protocols.mdc.adapter.cps;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/01/2017 - 14:06
 */
public class UnableToLoadCustomPropertySetClass extends RuntimeException {

    public UnableToLoadCustomPropertySetClass(ClassNotFoundException cause, String className, String fileName) {
        super("Unable to load class " + className + " that was configured in mapping file " + fileName, cause);
    }
}