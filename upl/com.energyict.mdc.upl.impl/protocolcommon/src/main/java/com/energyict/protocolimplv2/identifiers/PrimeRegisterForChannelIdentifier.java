package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.Device;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;

/**
 * Provides an implementation for the {@link RegisterIdentifier} interface
 * that returns the prime register of that channel that constitutes
 * this identification information.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (17:29)
 */
@XmlRootElement
public class PrimeRegisterForChannelIdentifier implements RegisterIdentifier {

    private final ObisCode registerObisCode;
    private final int channelIndex;
    private final DeviceIdentifier deviceIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private PrimeRegisterForChannelIdentifier() {
        this.registerObisCode = null;
        this.channelIndex = 0;
        this.deviceIdentifier = null;
    }

    public PrimeRegisterForChannelIdentifier(DeviceIdentifier deviceIdentifier, int channelIndex, ObisCode registerObisCode) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.channelIndex = channelIndex;
        this.registerObisCode = registerObisCode;
    }

    @Override
    public Register findRegister () {
        Device device = this.deviceIdentifier.findDevice();
        if (this.channelIndex <= device.getChannels().size()) {
            Register primeRegister = device.getChannel(this.channelIndex).getPrimeRegister();
            if (primeRegister == null) {
                throw NotFoundException.notFound(Register.class, this.toString());
            }
            else {
                return primeRegister;
            }
        }
        else {
            // Not enough channels
            throw NotFoundException.notFound(Register.class, this.toString());
        }
    }

    @XmlAttribute
    public ObisCode getRegisterObisCode() {
        return registerObisCode;
    }

    @XmlAttribute
    public int getChannelIndex() {
        return channelIndex;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public String toString () {
        return MessageFormat.format("Prime register for device {0} for channel index {1}", deviceIdentifier.toString(), channelIndex);
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
                    return getChannelIndex();
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