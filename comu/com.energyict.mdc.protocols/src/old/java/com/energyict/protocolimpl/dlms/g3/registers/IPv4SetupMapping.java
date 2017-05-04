/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.IPv4Setup;
import com.energyict.protocolimpl.dlms.g3.AS330D;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.IPv4SetupAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Created by cisac on 1/6/2016.
 */
public class IPv4SetupMapping extends G3Mapping{

    private IPv4SetupAttributesMapping iPv4SetupAttributesMapping;

    protected IPv4SetupMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public ObisCode getBaseObisCode() {                 //Set the E-Filed to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 4, (byte) 0);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        instantiateMappers(as330D.getSession().getCosemObjectFactory());
        return readRegister(getObisCode());
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (iPv4SetupAttributesMapping == null) {
            iPv4SetupAttributesMapping = new IPv4SetupAttributesMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getE();        //The E-field of the obiscode indicates which attribute is being read
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (iPv4SetupAttributesMapping.canRead(getObisCode())) {
            return iPv4SetupAttributesMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (iPv4SetupAttributesMapping.canRead(obisCode)) {
            return iPv4SetupAttributesMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if(getObisCode().equalsIgnoreBAndEChannel(IPv4Setup.getDefaultObisCode()) ){
            return DLMSClassId.IPV4_SETUP.getClassId();
        } else {
            return -1;
        }
    }
}
