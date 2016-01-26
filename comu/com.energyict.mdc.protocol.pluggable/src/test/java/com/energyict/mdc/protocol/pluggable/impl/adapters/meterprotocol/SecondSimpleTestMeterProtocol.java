package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

/**
 * A second SimpleTest protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 11:19
 */
public class SecondSimpleTestMeterProtocol extends SimpleTestMeterProtocol {

    @Override
    public String getProtocolDescription() {
        return this.getClass().getName();
    }

    public SecondSimpleTestMeterProtocol() {
        super();
    }
}
