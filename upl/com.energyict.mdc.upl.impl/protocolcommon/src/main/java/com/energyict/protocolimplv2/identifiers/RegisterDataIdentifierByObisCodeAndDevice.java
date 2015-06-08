package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.util.Collections;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifierType;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RegisterFactory;
import com.energyict.mdw.core.RegisterFactoryProvider;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies an {@link com.energyict.mdw.amr.Register} based on the ObisCode
 * of the {@link com.energyict.mdw.amr.RegisterMapping RegisterMapping} or the
 * {@link com.energyict.mdw.amr.RegisterSpec#getDeviceObisCode() RegisterSpec.getDeviceObisCode}
 *
 *
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
        this.deviceIdentifier = null;
    }

    public RegisterDataIdentifierByObisCodeAndDevice(ObisCode registerObisCode, DeviceIdentifier deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public Register findRegister () {
        if(this.register == null){
            final List<Register> registers = getRegisterFactory().findByDevice(deviceIdentifier.findDevice());
            for (Register register : registers) {
                if (register.getDeviceObisCode().equals(registerObisCode)){
                    this.register = register;
                    return this.register;
                }
            }
        }
        throw new NotFoundException("Register " + this.registerObisCode.toString() + " for device with " + this.deviceIdentifier + " not found");
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

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
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
    public RegisterIdentifierType getRegisterIdentifierType() {
        return RegisterIdentifierType.DeviceIdentifierAndObisCode;
    }

    @Override
    public List<Object> getIdentifier() {
        return Collections.toList((Object) getDeviceIdentifier(), getRegisterObisCode());
    }

    private RegisterFactory getRegisterFactory() {
        return RegisterFactoryProvider.instance.get().getRegisterFactory();
    }
}
