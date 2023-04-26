/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.itron.em620.registers;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.HistoricalRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EM620DlmsStoredValues implements StoredValues {

    public static final ObisCode CLOCK_OBIS = ObisCode.fromString("0.0.1.0.0.255");

    public static final int OBIS_CODE_F_FIELD_INDEX = 5;
    public static final int OBIS_CODE_F_FIELD_BASE = 255;

    private final DlmsSession session;
    private DataStructure buffer;
    private ProfileGeneric profileGeneric;
    private final ObisCode profileObiscode;

    private final Map<ObisCode, HistoricalRegister> cosemObjectMap = new HashMap<>();

    public EM620DlmsStoredValues(DlmsSession session, ObisCode profileObisCode) {
        this.session = session;
        this.profileObiscode = profileObisCode;
    }

    @Override
    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        int index = getCapturedObjectIndex(obisCode);
        if (index == -1) {
            throw new NoSuchRegisterException("StoredValues, register with ObisCode " + obisCode + " not found in the Capture Objects list of the billing profile.");
        }
        ObisCode baseObiscode = ProtocolTools.setObisCodeField(obisCode, OBIS_CODE_F_FIELD_INDEX, (byte) OBIS_CODE_F_FIELD_BASE);
        int reversedBillingPoint = getReversedBillingPoint(Math.abs(obisCode.getF()));

        DataStructure intervalData = (DataStructure) getBuffer().getElement(reversedBillingPoint);     //Get an interval
        HistoricalRegister cosemValue = cosemObjectMap.get(baseObiscode);
        if (cosemValue == null) {
            cosemValue = new HistoricalRegister();
            cosemValue.setQuantityValue(BigDecimal.valueOf(intervalData.convert2Long(index)), getUnit(baseObiscode));
            cosemObjectMap.put(baseObiscode, cosemValue);
        }
        Date billingTimeStamp = getBillingPointTimeDate(Math.abs(obisCode.getF()));
        return new HistoricalValue(cosemValue, billingTimeStamp, billingTimeStamp, 0);
}

    @Override
    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        DataStructure ds = (DataStructure) getBuffer().getElement(getReversedBillingPoint(billingPoint));
        Clock clock = new Clock(getCosemObjectFactory().getProtocolLink(), getCosemObjectFactory().getObjectReference(CLOCK_OBIS));
        clock.setDateTime((OctetString) ds.getElement(getCapturedObjectIndex(CLOCK_OBIS)));
        return clock.getDateTime();
    }

    @Override
    public int getBillingPointCounter() throws IOException {
        return getBuffer().getNrOfElements();
    }

    @Override
    public void retrieve() throws IOException {
    }

    private Unit getUnit(ObisCode baseObisCode) throws IOException {
        int classID = session.getMeterConfig().findObject(baseObisCode).getClassID();
        if (classID == DLMSClassId.REGISTER.getClassId()) {
            return getCosemObjectFactory().getRegister(baseObisCode).getScalerUnit().getEisUnit();
        } else if (classID == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            return getCosemObjectFactory().getExtendedRegister(baseObisCode).getScalerUnit().getEisUnit();
        } else if (classID == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            return getCosemObjectFactory().getDemandRegister(baseObisCode).getScalerUnit().getEisUnit();
        } else {
            throw new NoSuchRegisterException();
        }
    }

    private int getReversedBillingPoint(int billingPoint) throws IOException {
        return getBillingPointCounter() - billingPoint - 1;
    }

    /**
     * Find out which channel in the EOB load profile matches the requested register
     */
    protected int getCapturedObjectIndex(ObisCode obisCode) throws IOException {
        List<CapturedObject> captureObjects = getProfileGeneric().getCaptureObjects();
        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, OBIS_CODE_F_FIELD_INDEX, (byte) OBIS_CODE_F_FIELD_BASE);
        for (int i = 0; i < captureObjects.size(); i++) {
            CapturedObject cao = captureObjects.get(i);
            if (baseObisCode.equals(cao.getObisCode())) {
                return i;
            }
        }
        return -1;
    }

    protected DataStructure getBuffer() throws IOException {
        if (buffer == null) {
            buffer = getProfileGeneric().getBuffer().getRoot();
        }
        return buffer;
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }

    public ProfileGeneric getProfileGeneric() {
        if (profileGeneric == null) {
            try {
                profileGeneric = getCosemObjectFactory().getProfileGeneric(profileObiscode);
            } catch (IOException e) {
                //Absorb exception
                session.getLogger().warning(e.getMessage());
            }
        }
        return profileGeneric;
    }
}
