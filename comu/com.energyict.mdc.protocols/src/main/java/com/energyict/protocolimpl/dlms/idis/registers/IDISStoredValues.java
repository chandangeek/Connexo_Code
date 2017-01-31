/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.idis.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.HistoricalRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IDISStoredValues implements com.energyict.dlms.cosem.StoredValues {

    public static final ObisCode OBISCODE_BILLING_PROFILE = ObisCode.fromString("0.0.98.1.0.255");

    private final CosemObjectFactory cosemObjectFactory;
    private ProfileGeneric profileGeneric = null;
    private IDIS idis;
    private DataContainer buffer = null;

    public IDISStoredValues(CosemObjectFactory cosemObjectFactory, IDIS idis) {
        this.cosemObjectFactory = cosemObjectFactory;
        this.idis = idis;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public int getBillingPointCounter() throws IOException {
        return getProfileGeneric().getEntriesInUse();
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        return getProfileData().getIntervalData(billingPoint).getEndTime();
    }

    private DataContainer getFullBuffer() throws IOException {
        if (buffer == null) {
            buffer = profileGeneric.getBuffer();
        }
        return buffer;
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        int channelIndex = checkIfObisCodeIsCaptured(baseObisCode);
        int billingPoint = obisCode.getF();
        if (!isValidBillingPoint(billingPoint)) {
            throw new NoSuchRegisterException("Billing point " + obisCode.getF() + " doesn't exist for obiscode " + baseObisCode + ".");
        }
        int value = ((IntervalValue) getProfileData().getIntervalData(getReversedBillingPoint(billingPoint)).getIntervalValues().get(channelIndex - 1)).getNumber().intValue();
        HistoricalRegister cosemValue = new HistoricalRegister();
        cosemValue.setQuantityValue(BigDecimal.valueOf(value), getUnit(baseObisCode));

        return new HistoricalValue(cosemValue, getBillingPointTimeDate(getReversedBillingPoint(billingPoint)), new Date(), 0);
    }

    private int getReversedBillingPoint(int billingPoint) throws IOException {
        return getBillingPointCounter() - billingPoint - 1;
    }

    /**
     * Used the profile data object so we can sort the interval data (ascending)
     *
     * @return
     * @throws java.io.IOException
     */
    public ProfileData getProfileData() throws IOException {
        ProfileData profileData = new ProfileData();
        List<IntervalData> intervalDatas = new ArrayList<>();
        IntervalValue value;

        for (int index = 0; index < getBillingPointCounter(); index++) {
            DataStructure structure = getFullBuffer().getRoot().getStructure(index);
            Date timeStamp = new Date();
            List<IntervalValue> values = new ArrayList<>();
            for (int channel = 0; channel < structure.getNrOfElements(); channel++) {
                if (channel == 0) {
                    timeStamp = structure.getOctetString(0).toDate();
                } else {
                    value = new IntervalValue(structure.getInteger(channel), 0, 0);
                    values.add(value);
                }
            }
            intervalDatas.add(new IntervalData(timeStamp, 0, 0, 0, values));
        }
        profileData.setIntervalDatas(intervalDatas);
        profileData.sort();

        return profileData;
    }

    private Unit getUnit(ObisCode baseObisCode) throws IOException {
        return idis.readRegister(baseObisCode).getQuantity().getUnit();
    }

    private int checkIfObisCodeIsCaptured(ObisCode obisCode) throws IOException {
        int channelIndex = 0;
        for (CapturedObject capturedObject : getProfileGeneric().getCaptureObjects()) {
            if (capturedObject.getLogicalName().getObisCode().equals(obisCode)) {
                return channelIndex;
            }
            channelIndex++;
        }
        throw new NoSuchRegisterException("Obiscode " + obisCode.toString() + " is not stored in the billing profile");
    }

    public ProfileGeneric getProfileGeneric() {
        if (profileGeneric == null) {
            try {
                profileGeneric = getCosemObjectFactory().getProfileGeneric(OBISCODE_BILLING_PROFILE);
            } catch (IOException e) {
                //Absorb exception
            }
        }
        return profileGeneric;
    }

    public void retrieve() throws IOException {
        // Not implemented
    }

    private boolean isValidBillingPoint(int billingPoint) {
        try {
            return (billingPoint >= 0) && (billingPoint < getBillingPointCounter());
        } catch (IOException e) {
            return false;
        }
    }
}