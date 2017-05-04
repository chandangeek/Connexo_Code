/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers;


import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.BeaconEventPushNotificationConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.BeaconPushEventNotificationAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Created by cisac on 1/5/2016.
 */
public class BeaconEventNotificationMapping extends G3Mapping{

    private BeaconPushEventNotificationAttributesMapping pushEventMapping;

    protected BeaconEventNotificationMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public ObisCode getBaseObisCode() {                 //Set the B-field to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 1, (byte) 0);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (pushEventMapping == null) {
            pushEventMapping = new BeaconPushEventNotificationAttributesMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getB();        //The B-field of the obiscode indicates which attribute is being read
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
            return DLMSClassId.EVENT_NOTIFICATION.getClassId();
        } else {
            return -1;
        }
    }
}
