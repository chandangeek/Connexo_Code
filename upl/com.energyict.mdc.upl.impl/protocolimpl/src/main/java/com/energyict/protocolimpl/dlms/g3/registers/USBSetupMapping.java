package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.methods.USBSetup;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.USBSetupAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Copywrite EnergyICT 22.09.2016.
 */
public class USBSetupMapping extends G3Mapping {

    private USBSetupAttributesMapping usbSetupAttributesMapping;

    protected USBSetupMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    //Set the F-Filed to 255
    public ObisCode getBaseObisCode() {                 //Set the E-Filed to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 5, (byte) 255);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (usbSetupAttributesMapping == null) {
            usbSetupAttributesMapping = new USBSetupAttributesMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getF();        //The B-field of the obiscode indicates which attribute is being read
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (usbSetupAttributesMapping.canRead(getObisCode())) {
            return usbSetupAttributesMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (usbSetupAttributesMapping.canRead(obisCode)) {
            return usbSetupAttributesMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if (    getObisCode().equalsIgnoreBillingField(USBSetup.getDefaultObisCode()) ||
                getObisCode().equalsIgnoreBillingField(USBSetup.getLegacyObisCode())) {
            return DLMSClassId.USB_SETUP.getClassId();
        } else {
            return -1;
        }
    }
}

