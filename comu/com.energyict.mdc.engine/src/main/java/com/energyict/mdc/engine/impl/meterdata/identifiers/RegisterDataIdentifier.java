package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifierType;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies an {@link com.energyict.mdc.upl.meterdata.Register} based on the ObisCode
 * of the RegisterType or the
 * {@link com.energyict.mdc.device.config.RegisterSpec#getDeviceObisCode() RegisterSpec.getDeviceObisCode}
 * <p>
 * Copyrights EnergyICT
 * Date: 15/10/12
 * Time: 16:29
 */
public class RegisterDataIdentifier implements RegisterIdentifier {

    private final ObisCode registerObisCode;
    private final ObisCode deviceRegisterObisCode;
    private final DeviceIdentifier deviceIdentifier;

    private Register register;

    public RegisterDataIdentifier(ObisCode registerObisCode, ObisCode deviceRegisterObisCode, DeviceIdentifier deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceRegisterObisCode = deviceRegisterObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public Register findRegister() {
        if (this.register == null) {
            List<Register> registers = ((Device) this.deviceIdentifier.findDevice()).getRegisters();        //Downcast to the Connexo Device
            for (Register register : registers) {
                // first need to check the DeviceObisCode
                if (register.getDeviceObisCode() != null && register.getDeviceObisCode().equals(deviceRegisterObisCode)) {
                    this.register = register;
                    break;
                } else if (register.getRegisterTypeObisCode().equals(registerObisCode)) {
                    this.register = register;
                    break;
                }
            }
        }
        return this.register;
    }

    @Override
    public ObisCode getRegisterObisCode() {
        return this.registerObisCode;
    }

    @Override
    public RegisterIdentifierType getRegisterIdentifierType() {
        return RegisterIdentifierType.DeviceIdentifierAndObisCode;
    }

    @Override
    public List<Object> getParts() {
        return Arrays.asList(registerObisCode, deviceRegisterObisCode, deviceIdentifier);
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public String toString() {
        return "register having OBIS code " + this.registerObisCode;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }
}