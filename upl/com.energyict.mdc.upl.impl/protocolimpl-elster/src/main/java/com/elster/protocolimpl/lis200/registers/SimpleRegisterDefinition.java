package com.elster.protocolimpl.lis200.registers;

/**
 * User: heuckeg
 * Date: 14.04.11
 * Time: 14:05
 */
public class SimpleRegisterDefinition extends RegisterDefinition {


    public SimpleRegisterDefinition(Lis200ObisCode obisCode, int instance, String address) {
        super(obisCode, instance, address);
    }
}
