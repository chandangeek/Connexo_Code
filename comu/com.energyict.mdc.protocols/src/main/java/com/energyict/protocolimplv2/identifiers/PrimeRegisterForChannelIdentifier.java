package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.upl.meterdata.Register;
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
    public Register findRegister () {
        com.energyict.mdc.device.data.Device device = (com.energyict.mdc.device.data.Device) deviceIdentifier.findDevice();  //Downcast to the Connexo Device
        if (this.channelIndex <= device.getChannels().size()) {
//            Register primeRegister = device.getChannel(this.channelIndex).getRegisterTypeObisCode();
            // Linking Registers to Channels is not done in Jupiter
            Register primeRegister = device.getRegisterWithDeviceObisCode(registerObisCode);
            if (primeRegister == null) {
                throw new NotFoundException("Prime register of channel " + this.channelIndex + " of device " + this.deviceIdentifier + " not found!");
            }
            else {
                return primeRegister;
            }
        }
        else {
            // Not enough channels
            throw new NotFoundException("Prime register of channel " + this.channelIndex + " of device " + this.deviceIdentifier + " not found because the channel does not exist!");
        }
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public ObisCode getObisCode() {
        return this.registerObisCode;
    }

    @Override
    public ObisCode getDeviceRegisterObisCode() {
        return this.deviceRegisterObisCode;
    }

    @Override
    public String toString () {
        return MessageFormat.format("prime register for device ''{0}'' for channel index {1}", deviceIdentifier.toString(), channelIndex);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.deviceIdentifier;
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
                    return getDeviceIdentifier();
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