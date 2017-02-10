package com.energyict.mdc.protocol.pluggable.adapters.upl.cps;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/01/2017 - 14:04
 */
public class UnableToCreateCustomPropertySet extends RuntimeException {

    public UnableToCreateCustomPropertySet(Throwable cause, Class cpsClass, String fileName) {
        super("Unable to create instance of class " + cpsClass.getName() + " that was configured in mapping file " + fileName, cause);
    }
}