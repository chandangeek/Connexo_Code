/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.a2.registers;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.HistoricalRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.dlms.idis.am500.AM500;
import com.energyict.protocolimplv2.dlms.idis.am500.registers.ExtendedRegisterChannelIndex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EIStoredValues implements StoredValues {

    public static final ObisCode OBISCODE_BILLING_PROFILE = ObisCode.fromString("7.0.98.11.0.255");
    protected final A2 a2;
    private final CosemObjectFactory cosemObjectFactory;
    protected DataContainer buffer = null;
    private ProfileGeneric profileGeneric = null;

    public EIStoredValues(A2 a2) {
        this.cosemObjectFactory = a2.getDlmsSession().getCosemObjectFactory();
        this.a2 = a2;
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public int getBillingPointCounter() throws IOException {
        return getProfileGeneric().getEntriesInUse();
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        return getProfileData().getIntervalData(billingPoint).getEndTime();
    }

    protected DataContainer getFullBuffer() throws IOException {
        if (buffer == null) {
            buffer = getProfileGeneric().getBuffer();
        }
        return buffer;
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        if(obisCode.getValue().equals("7.0.43.45.5.0")){
            baseObisCode = ProtocolTools.setObisCodeField(baseObisCode, 4, (byte) 0);
        }
        ExtendedRegisterChannelIndex channelIndex = new ExtendedRegisterChannelIndex(baseObisCode, getProfileGeneric().getCaptureObjects());
        int billingPoint = obisCode.getF();
        if (!isValidBillingPoint(billingPoint)) {
            throw new NoSuchRegisterException("Billing point " + obisCode.getF() + " doesn't exist for obiscode " + baseObisCode + ".");
        }
        int reverseBillingPoint = getReversedBillingPoint(billingPoint);
        ProfileData historicalProfileData = getProfileData();
        IntervalData intervalData = historicalProfileData.getIntervalData(reverseBillingPoint);

        // get the value
        IntervalValue intervalValue = (IntervalValue) intervalData.getIntervalValues().get(channelIndex.getValueIndex() - 1);
        int value = intervalValue.getNumber().intValue();
        if(obisCode.getValue().equals("7.0.43.45.5.0")){
            intervalValue = (IntervalValue) intervalData.getIntervalValues().get(channelIndex.getEventTimeIndex() - 1);
            value = (int)(intervalValue.getNumber().longValue()/1000); //ms to sec
        }
        Calendar calHistoricalDate = Calendar.getInstance(getProtocol().getTimeZone());
        calHistoricalDate.setTimeInMillis(intervalData.getEndTime().getTime());

        // try to see if we have also event time (i.e. for extended registers)
        Date eventTime = null;
        if (channelIndex.getEventTimeIndex() > 0) {
            IntervalValue capturedTime = (IntervalValue) intervalData.getIntervalValues().get(channelIndex.getEventTimeIndex() - 1);
            if (capturedTime.getNumber() != null) {
                Calendar calEventTime = Calendar.getInstance(getProtocol().getTimeZone());
                calEventTime.setTimeInMillis(capturedTime.getNumber().longValue());
                eventTime = calEventTime.getTime();
            }
        }

        HistoricalRegister cosemValue = new HistoricalRegister();
        cosemValue.setQuantityValue(BigDecimal.valueOf(value), getUnit(baseObisCode));


        return new HistoricalValue(cosemValue, calHistoricalDate.getTime(), eventTime, 0);
    }

    protected int getReversedBillingPoint(int billingPoint) throws IOException {
        return getBillingPointCounter() - billingPoint - 1;
    }

    /**
     * Used the profile data object so we can sort the interval data (ascending)
     */
    public ProfileData getProfileData() throws IOException {
        ProfileData profileData = new ProfileData();
        List<IntervalData> intervalDatas = new ArrayList<>();
        IntervalValue value;

        for (int index = 0; index < getBillingPointCounter(); index++) {
            DataStructure structure = getFullBuffer().getRoot().getStructure(index);
            Calendar timeStamp = Calendar.getInstance();
            List<IntervalValue> values = new ArrayList<>();
            for (int channel = 0; channel < structure.getNrOfElements(); channel++) {
                try {
                    if (channel == 0) {
                        timeStamp = structure.getOctetString(0).toCalendar(getProtocol().getTimeZone());
                    } else {
                        if (structure.isInteger(channel)) {
                            value = new IntervalValue(structure.getInteger(channel), 0, 0);
                        } else if (structure.isLong(channel)) {
                            value = new IntervalValue(structure.getLong(channel), 0, 0);
                        } else if (structure.isOctetString(channel)) {
                            if (structure.getOctetString(channel).getArray().length > 2) {
                                Calendar cal = structure.getOctetString(channel).toCalendar(getProtocol().getTimeZone());
                                value = new IntervalValue(cal.getTimeInMillis(), 0, 0);
                            } else{
                                value = new IntervalValue(0,0,0);
                            }
                        } else {
                            value = new IntervalValue(null, 0, 0);
                        }

                        values.add(value);
                    }
                } catch (Exception ex) {
                    getProtocol().getLogger().warning(ex.getMessage());
                    if (channel > 0) {
                        values.add(new IntervalValue(null, 0, 0));
                    }
                }
            }
            intervalDatas.add(new IntervalData(timeStamp.getTime(), 0, 0, 0, values));
        }
        profileData.setIntervalDatas(intervalDatas);
        profileData.sort();

        return profileData;
    }

    private Unit getUnit(ObisCode baseObisCode) throws IOException {
        Map<ObisCode, Unit> unitMap = a2.getProfileDataReader().readUnits(null, Arrays.asList(baseObisCode));
        return unitMap.get(baseObisCode);
    }

    public ProfileGeneric getProfileGeneric() throws NotInObjectListException {
        if (profileGeneric == null) {
            profileGeneric = getCosemObjectFactory().getProfileGeneric(OBISCODE_BILLING_PROFILE);
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
            throw DLMSIOExceptionHandler.handle(e, a2.getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    protected AbstractDlmsProtocol getProtocol() {
        return a2;
    }
}