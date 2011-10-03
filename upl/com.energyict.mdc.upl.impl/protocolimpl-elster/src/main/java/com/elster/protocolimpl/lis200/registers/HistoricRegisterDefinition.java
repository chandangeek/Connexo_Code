package com.elster.protocolimpl.lis200.registers;

/**
 * User: heuckeg
 * Date: 14.04.11
 * Time: 14:08
 */
public class HistoricRegisterDefinition extends RegisterDefinition {

    public HistoricRegisterDefinition(Lis200ObisCode obisCode, int instance, String value) {
        super(obisCode, instance, value);
    }
}
