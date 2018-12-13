package com.elster.protocolimpl.lis200.registers;

/**
 * User: heuckeg
 * Date: 14.04.11
 * Time: 14:00
 */
public abstract class RegisterDefinition {

    /** The obisCode from the register */
    private final Lis200ObisCode obiscode;
    /** instance of value */
    private final int instance;
    /* address of value */
    private final String address;

    public RegisterDefinition(Lis200ObisCode obisCode, int instance, String address) {
        this.obiscode = obisCode;
        this.instance = instance;
        this.address = address;
    }

    public Lis200ObisCode getObiscode() {
        return obiscode;
    }

    public int getInstance() {
        return instance;
    }

    public String getAddress() {
        return address;
    }
}
