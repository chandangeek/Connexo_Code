package com.elster.protocolimpl.lis200.registers;

/**
 * User: heuckeg
 * Date: 14.04.11
 * Time: 14:07
 */
public class ValueRegisterDefinition extends RegisterDefinition {

    public ValueRegisterDefinition(Lis200ObisCode obisCode, int instance, String address) {
        super(obisCode, instance, address);
    }
}
