package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.Register;
import com.energyict.mdc.protocol.api.device.Device;

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
    private int channelIndex;

    public PrimeRegisterForChannelIdentifier(DeviceIdentifier deviceIdentifier, ObisCode registerObisCode, ObisCode deviceRegisterObisCode, int channelIndex) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.registerObisCode = registerObisCode;
        this.deviceRegisterObisCode = deviceRegisterObisCode;
        this.channelIndex = channelIndex;
    }

    @Override
    public Register findRegister () {
        Device device = this.deviceIdentifier.findDevice();
        if (this.channelIndex <= device.getChannels().size()) {
            Register primeRegister = device.getChannel(this.channelIndex).getPrimeRegister();
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
    public ObisCode getObisCode() {
        return this.registerObisCode;
    }

    @Override
    public ObisCode getDeviceRegisterObisCode() {
        return this.deviceRegisterObisCode;
    }

    @Override
    public String toString () {
        return MessageFormat.format("Prime register for device {0} for channel index {1}", deviceIdentifier.toString(), channelIndex);
    }

}