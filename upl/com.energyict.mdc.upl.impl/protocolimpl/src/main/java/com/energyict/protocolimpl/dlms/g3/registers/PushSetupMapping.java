package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.BeaconEventPushNotificationConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PushSetupAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Mapping of Push setup IC
 * class id = 40, version = 0, logical name = 0-0:25.9.0.255 (0000190900FF)
 * The push setup COSEM IC allows for configuration of upstream push events.
 */
public class PushSetupMapping extends G3Mapping {

    private PushSetupAttributesMapping pushEventMapping;

    protected PushSetupMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public ObisCode getBaseObisCode() {                 //Set the B-field to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 5, (byte) 255);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (pushEventMapping == null) {
            pushEventMapping = new PushSetupAttributesMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getF();
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (pushEventMapping.canRead(getObisCode())) {
            return pushEventMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (pushEventMapping.canRead(obisCode)) {
            return pushEventMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if (getObisCode().equalsIgnoreBChannel(BeaconEventPushNotificationConfig.getDefaultObisCode())) {
            return DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP.getClassId();
        } else {
            return -1;
        }
    }
}
