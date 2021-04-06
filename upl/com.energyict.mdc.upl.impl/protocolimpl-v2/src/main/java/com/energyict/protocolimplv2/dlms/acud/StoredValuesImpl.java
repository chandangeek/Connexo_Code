package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class StoredValuesImpl implements StoredValues {

    public static final ObisCode OBISCODE_BILLING_PROFILE = ObisCode.fromString("1.0.98.1.2.255");
    protected final Acud acud;
    private final CosemObjectFactory cosemObjectFactory;
    protected DataContainer buffer = null;
    private ProfileGeneric profileGeneric = null;

    public StoredValuesImpl(Acud acud) {
        this.cosemObjectFactory = acud.getDlmsSession().getCosemObjectFactory();
        this.acud = acud;
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

        Calendar calHistoricalDate = Calendar.getInstance(getProtocol().getTimeZone());
        calHistoricalDate.setTimeInMillis(intervalData.getEndTime().getTime());

        Date eventTime = null;

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
                        } else if (structure.isOctetString(channel)){
                            Calendar cal = structure.getOctetString(channel).toCalendar(getProtocol().getTimeZone());
                            value = new IntervalValue(cal.getTimeInMillis(), 0, 0);
                        } else {
                            value = new IntervalValue(null, 0, 0);
                        }

                        values.add(value);
                    }
                } catch (Exception ex){
                    getProtocol().getLogger().warning(ex.getMessage());
                    if (channel>0){
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
        Map<ObisCode, Unit> unitMap = acud.getAcudProfileDataReader().readUnits( null, Arrays.asList(baseObisCode));
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
            throw DLMSIOExceptionHandler.handle(e, acud.getDlmsSession().getProperties().getRetries()+1);
        }
    }

    protected AbstractDlmsProtocol getProtocol(){
        return acud;
    }
}