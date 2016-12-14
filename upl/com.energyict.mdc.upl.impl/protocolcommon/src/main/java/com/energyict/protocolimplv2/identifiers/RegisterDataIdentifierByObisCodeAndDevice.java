package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RegisterFactory;
import com.energyict.mdw.core.RegisterFactoryProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies an {@link com.energyict.mdw.amr.Register} based on the ObisCode
 * of the {@link com.energyict.mdw.amr.RegisterMapping RegisterMapping} or the
 * {@link com.energyict.mdw.amr.RegisterSpec#getDeviceObisCode() RegisterSpec.getDeviceObisCode}
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:24
 */
@XmlRootElement
public class RegisterDataIdentifierByObisCodeAndDevice implements RegisterIdentifier {

    private final ObisCode registerObisCode;
    private final DeviceIdentifier deviceIdentifier;

    private Register register;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private RegisterDataIdentifierByObisCodeAndDevice() {
        this.registerObisCode = null;
        this.deviceIdentifier = new NullDeviceIdentifier();
    }

    public RegisterDataIdentifierByObisCodeAndDevice(ObisCode registerObisCode, DeviceIdentifier deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public Register findRegister() {
        if (this.register == null) {
            final List<Register> registers = getRegisterFactory().findByDevice(deviceIdentifier.findDevice());
            for (Register register : registers) {
                if (register.getDeviceObisCode().equals(registerObisCode)) {
                    this.register = register;
                    return this.register;
                }
            }
            throw NotFoundException.notFound(Register.class, this.toString());
        }
        return register;
    }

    @Override
    public String toString() {
        return "deviceIdentifier = " + this.deviceIdentifier + " and ObisCode = " + this.registerObisCode.toString();
    }

    @XmlAttribute
    public ObisCode getRegisterObisCode() {
        return registerObisCode;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RegisterDataIdentifierByObisCodeAndDevice)) {
            return false;
        }
        RegisterDataIdentifierByObisCodeAndDevice identifier = (RegisterDataIdentifierByObisCodeAndDevice) obj;
        if (identifier.getRegisterObisCode() != this.getRegisterObisCode() ||
                !identifier.getDeviceIdentifier().equals(this.getDeviceIdentifier())) {
            return false;
        }
        return true;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private RegisterFactory getRegisterFactory() {
        return RegisterFactoryProvider.instance.get().getRegisterFactory();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DeviceIdentifierAndObisCode";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "device": {
                    return getDeviceIdentifier();
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

    private static class NullDeviceIdentifier implements DeviceIdentifier {
        @Override
        public Device findDevice() {
            throw new UnsupportedOperationException("NullDeviceIdentifier is not capable of finding a device because there is not identifier");
        }

        @Override
        public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
            return new NullIntrospector();
        }
    }

    private static class NullIntrospector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Null";
        }

        @Override
        public Object getValue(String role) {
            throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
        }

    }

}