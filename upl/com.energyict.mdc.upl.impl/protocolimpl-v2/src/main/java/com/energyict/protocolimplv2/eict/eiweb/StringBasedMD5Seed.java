package com.energyict.protocolimplv2.eict.eiweb;

/**
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (14:44)
 */
public class StringBasedMD5Seed {

    private String value;

    public StringBasedMD5Seed(String value) {
        super();
        this.value = value;
    }

    public byte[] getBytes() {
        return this.value.getBytes();
    }
}