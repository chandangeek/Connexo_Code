package com.energyict.protocolimplv2.dlms.as253;

import com.energyict.cbo.Unit;
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
import com.energyict.protocolimplv2.dlms.idis.am500.registers.ExtendedRegisterChannelIndex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AS253StoredValues implements StoredValues {

    private static final ObisCode SELF_READ_TOTAL = ObisCode.fromString("0.0.98.1.0.255");
    private static final ObisCode SELF_READ_TIER_1 = ObisCode.fromString("0.0.98.1.1.255");
    private static final ObisCode SELF_READ_TIER_2 = ObisCode.fromString("0.0.98.1.2.255");
    private static final ObisCode SELF_READ_TIER_3 = ObisCode.fromString("0.0.98.1.3.255");
    private static final ObisCode SELF_READ_TIER_4 = ObisCode.fromString("0.0.98.1.4.255");

    private static final ObisCode PREVIOUS_BILLING_TOTAL = ObisCode.fromString("0.0.98.2.0.255");
    private static final ObisCode PREVIOUS_BILLING_TIER_1 = ObisCode.fromString("0.0.98.2.1.255");
    private static final ObisCode PREVIOUS_BILLING_TIER_2 = ObisCode.fromString("0.0.98.2.2.255");
    private static final ObisCode PREVIOUS_BILLING_TIER_3 = ObisCode.fromString("0.0.98.2.3.255");
    private static final ObisCode PREVIOUS_BILLING_TIER_4 = ObisCode.fromString("0.0.98.2.4.255");

    private final AS253 protocol;
    private final CosemObjectFactory cosemObjectFactory;

    private ObisCode profileGenericObisCode = SELF_READ_TOTAL;
    private boolean profileGenericObisCodeChanged = true;
    private ProfileGeneric profileGeneric = null;

    public AS253StoredValues(AS253 protocol){
        this.protocol = protocol;
        this.cosemObjectFactory = protocol.getDlmsSession().getCosemObjectFactory();
    }


    @Override
    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        ObisCode baseObisCode = getBaseObisCode(obisCode);
        updateProfileGenericObisCode(obisCode);
        ExtendedRegisterChannelIndex extendedChannelIndex = new ExtendedRegisterChannelIndex(baseObisCode, getProfileGeneric().getCaptureObjects());
        int channelIndex = extendedChannelIndex.getValueIndex();
        int billingPoint = obisCode.getF() > 12 ? (obisCode.getF() - 13) : obisCode.getF();
        if (!isValidBillingPoint(obisCode)) {
            throw new NoSuchRegisterException("Billing point " + obisCode.getF() + " doesn't exist for obiscode " + baseObisCode + ".");
        }

        List<IntervalValue> intervalValueList = getProfileData().getIntervalData(getReversedBillingPoint(billingPoint)).getIntervalValues();

        int value = intervalValueList.get(channelIndex - 1).getNumber().intValue();

        Date eventTime = null;

        if (extendedChannelIndex.getEventTimeIndex() > 0) {
            final IntervalValue eventTimeMillis = intervalValueList.get(extendedChannelIndex.getEventTimeIndex() - 1);

            if (eventTimeMillis.getNumber() != null) {
                final Calendar calendar = Calendar.getInstance(this.protocol.getTimeZone());
                calendar.setTimeInMillis(eventTimeMillis.getNumber().longValue());

                eventTime = calendar.getTime();
            }
        }

        HistoricalRegister cosemValue = new HistoricalRegister();

        cosemValue.setQuantityValue(BigDecimal.valueOf(value), getUnit(extendedChannelIndex.getCaptureObjects().get(channelIndex)));
        return new HistoricalValue(cosemValue, getBillingPointTimeDate(getReversedBillingPoint(billingPoint)), eventTime, 0);
    }

    private Unit getUnit(CapturedObject capturedObject) throws IOException {
        return protocol.getLoadProfileDataReader().readUnitFromDevice(capturedObject);
    }

    @Override
    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        return getProfileData().getIntervalData(billingPoint).getEndTime();
    }

    @Override
    public int getBillingPointCounter() throws IOException {
        return getProfileGeneric().getEntriesInUse();
    }

    @Override
    public void retrieve() throws IOException {
        //Not implemented
    }

    @Override
    public ProfileGeneric getProfileGeneric() throws NotInObjectListException {
        if (profileGeneric == null || profileGenericObisCodeChanged) {
            profileGeneric = cosemObjectFactory.getProfileGeneric(profileGenericObisCode);
        }
        return profileGeneric;
    }

    private void updateProfileGenericObisCode(ObisCode obisCode) {
        if(obisCode.getE() > 0){
            switch (obisCode.getE()){
                case 1:
                    if(obisCode.getF() > 12){
                        setProfileGenericObisCode(PREVIOUS_BILLING_TIER_1);
                    }else {
                        setProfileGenericObisCode(SELF_READ_TIER_1);
                    }
                    break;
                case 2:
                    if(obisCode.getF() > 12){
                        setProfileGenericObisCode(PREVIOUS_BILLING_TIER_2);
                    }else {
                        setProfileGenericObisCode(SELF_READ_TIER_2);
                    }
                    break;
                case 3:
                    if(obisCode.getF() > 12){
                        setProfileGenericObisCode(PREVIOUS_BILLING_TIER_3);
                    }else {
                        setProfileGenericObisCode(SELF_READ_TIER_3);
                    }
                    break;
                case 4:
                    if(obisCode.getF() > 12){
                        setProfileGenericObisCode(PREVIOUS_BILLING_TIER_4);
                    }else {
                        setProfileGenericObisCode(SELF_READ_TIER_4);
                    }
                    break;
            }
        }else {
            if(obisCode.getF() > 12){
                setProfileGenericObisCode(PREVIOUS_BILLING_TOTAL);
            }else {
                setProfileGenericObisCode(SELF_READ_TOTAL);
            }
        }
    }

    private void setProfileGenericObisCode(ObisCode profileGenericObisCode) {
        profileGenericObisCodeChanged = false;
        if(!this.profileGenericObisCode.equals(profileGenericObisCode)){
            this.profileGenericObisCode = profileGenericObisCode;
            profileGenericObisCodeChanged = true;
        }
    }

    private boolean isValidBillingPoint(ObisCode obisCode) {
        try {
            int point = obisCode.getF();
            int billingPoint = point > 12 ? (point - 13) : point;   // If point is > 12, then Previous billing total is requested.

            return ((billingPoint >= 0) && (billingPoint < getBillingPointCounter())) && (obisCode.getE() >= 0 && obisCode.getE() <=4);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    private ObisCode getBaseObisCode(ObisCode obisCode){
        return ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
    }

    @SuppressWarnings("Duplicates")
    public ProfileData getProfileData() throws IOException {
        ProfileData profileData = new ProfileData();
        List<IntervalData> intervalDatas = new ArrayList<>();
        IntervalValue value;

        for (int index = 0; index < getBillingPointCounter(); index++) {
            DataStructure structure = getProfileGeneric().getBuffer().getRoot().getStructure(index);
            Calendar timeStamp = Calendar.getInstance();
            List<IntervalValue> values = new ArrayList<>();
            for (int channel = 0; channel < structure.getNrOfElements(); channel++) {
                try {
                    if (channel == 0) {
                        timeStamp = structure.getOctetString(0).toCalendar(protocol.getTimeZone());
                    } else {
                        if (structure.isInteger(channel)) {
                            value = new IntervalValue(structure.getInteger(channel), 0, 0);
                        } else if (structure.isLong(channel)) {
                            value = new IntervalValue(structure.getLong(channel), 0, 0);
                        } else if (structure.isOctetString(channel)){
                            Calendar cal = structure.getOctetString(channel).toCalendar(protocol.getTimeZone());
                            value = new IntervalValue(cal.getTimeInMillis(), 0, 0);
                        } else {
                            value = new IntervalValue(null, 0, 0);
                        }

                        values.add(value);
                    }
                } catch (Exception ex){
                    protocol.getLogger().warning(ex.getMessage());
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

    private int getReversedBillingPoint(int billingPoint) throws IOException {
        return getBillingPointCounter() - billingPoint - 1;
    }
}
