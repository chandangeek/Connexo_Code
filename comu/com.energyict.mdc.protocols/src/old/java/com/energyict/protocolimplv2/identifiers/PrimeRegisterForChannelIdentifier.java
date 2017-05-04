package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

import java.text.MessageFormat;

/**
 * Provides an implementation for the {@link RegisterIdentifier} interface
 * that returns the prime register of that channel that constitutes
 * this identification information.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (17:29)
 */
public class PrimeRegisterForChannelIdentifier implements RegisterIdentifier {

    private DeviceIdentifier deviceIdentifier;
    private final ObisCode registerObisCode;
    private final ObisCode deviceRegisterObisCode;
    private long channelIndex;

    public PrimeRegisterForChannelIdentifier(DeviceIdentifier deviceIdentifier, ObisCode registerObisCode, ObisCode deviceRegisterObisCode, long channelIndex) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.registerObisCode = registerObisCode;
        this.deviceRegisterObisCode = deviceRegisterObisCode;
        this.channelIndex = channelIndex;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public ObisCode getRegisterObisCode() {
        return this.registerObisCode;
    }

    @Override
    public String toString () {
        return MessageFormat.format("prime register for device ''{0}'' for channel index {1}", deviceIdentifier.toString(), channelIndex);
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "PrimeRegisterForChannel";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "device": {
                    return PrimeRegisterForChannelIdentifier.this.deviceIdentifier;
                }
                case "channelIndex": {
                    return channelIndex;
                }
                case "obisCode": {
                    return getRegisterObisCode();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}