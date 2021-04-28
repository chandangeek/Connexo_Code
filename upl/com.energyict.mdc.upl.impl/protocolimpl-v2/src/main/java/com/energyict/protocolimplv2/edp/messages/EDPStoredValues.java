package com.energyict.protocolimplv2.edp.messages;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.cosem.*;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.edp.CX20009;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@SuppressWarnings("Duplicates")
public class EDPStoredValues implements StoredValues {

    private static final ObisCode DAILY_BILLING_PROFILE = ObisCode.fromString("0.0.98.2.0.255");   //E-field is filled with the tariff (1 or 2) later on
    private static final ObisCode MONTHLY_BILLING_PROFILE = ObisCode.fromString("0.0.98.1.0.255");

    private final CX20009 protocol;
    private DataContainer dailyBufferT1 = null;
    private DataContainer dailyBufferT2 = null;
    private DataContainer monthlyBufferT1 = null;
    private DataContainer monthlyBufferT2 = null;
    private List<CapturedObject> dailyCapturedObjectsT1 = null;
    private List<CapturedObject> dailyCapturedObjectsT2 = null;
    private List<CapturedObject> monthlyCapturedObjectsT1 = null;
    private List<CapturedObject> monthlyCapturedObjectsT2 = null;
    private ObisCode currentObisCode;

    public EDPStoredValues(CX20009 protocol) {
        this.protocol = protocol;
    }

    public int getBillingPointCounter() throws IOException {
        return getProfileGeneric().getEntriesInUse();
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        return getProfileData().getIntervalData(billingPoint).getEndTime();
    }

    private DataContainer getFullBuffer() throws IOException {
        if (isDailyRegister() && isTariff1()) {
            if (dailyBufferT1 == null) {              //Cache the buffer, we don't want to fully read it again for every billing point
                dailyBufferT1 = getProfileGeneric().getBuffer();
            }
            return dailyBufferT1;
        } else if (isMonthlyRegister() && isTariff1()) {
            if (monthlyBufferT1 == null) {
                monthlyBufferT1 = getProfileGeneric().getBuffer();
            }
            return monthlyBufferT1;
        } else if (isDailyRegister() && isTariff2()) {
            if (dailyBufferT2 == null) {
                dailyBufferT2 = getProfileGeneric().getBuffer();
            }
            return dailyBufferT2;
        } else if (isMonthlyRegister() && isTariff2()) {
            if (monthlyBufferT2 == null) {
                monthlyBufferT2 = getProfileGeneric().getBuffer();
            }
            return monthlyBufferT2;
        } else {
            throw new NoSuchRegisterException();
        }
    }

    private List<CapturedObject> getCapturedObjects() throws IOException {
        if (isDailyRegister() && isTariff1()) {
            if (dailyCapturedObjectsT1 == null) {
                dailyCapturedObjectsT1 = getProfileGeneric().getCaptureObjects();
            }
            return dailyCapturedObjectsT1;
        } else if (isMonthlyRegister() && isTariff1()) {
            if (dailyCapturedObjectsT2 == null) {
                dailyCapturedObjectsT2 = getProfileGeneric().getCaptureObjects();
            }
            return dailyCapturedObjectsT2;
        } else if (isDailyRegister() && isTariff2()) {
            if (monthlyCapturedObjectsT1 == null) {
                monthlyCapturedObjectsT1 = getProfileGeneric().getCaptureObjects();
            }
            return monthlyCapturedObjectsT1;
        } else if (isMonthlyRegister() && isTariff2()) {
            if (monthlyCapturedObjectsT2 == null) {
                monthlyCapturedObjectsT2 = getProfileGeneric().getCaptureObjects();
            }
            return monthlyCapturedObjectsT2;
        } else {
            throw new NoSuchRegisterException();
        }
    }

    /**
     * E-field equal to 11...16 or 20 is tariff 2
     */
    private boolean isTariff2() {
        return (currentObisCode.getE() > 10);
    }

    /**
     * E-field equal to 1...6 or 10 is tariff 1
     */
    private boolean isTariff1() {
        return (currentObisCode.getE() <= 10);
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        currentObisCode = obisCode;
        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        int channelIndex = checkIfObisCodeIsCaptured(baseObisCode);
        if (isMaxDemandRegister(obisCode)) {
            //Max demand registers indicate the attribute (2: value or 5: captureTime) in the B-field!
            baseObisCode = ProtocolTools.setObisCodeField(baseObisCode, 1, (byte) 0);
        }
        int billingPoint = Math.abs(obisCode.getF()) - (isDailyRegister() ? 12 : 0);
        if (!isValidBillingPoint(billingPoint)) {
            throw new NoSuchRegisterException("Billing point " + billingPoint + " doesn't exist for obiscode " + baseObisCode + ".");
        }
        ProfileData profileData = getProfileData();
        IntervalValue intervalValue = (IntervalValue) profileData.getIntervalData(getReversedBillingPoint(billingPoint)).getIntervalValues().get(channelIndex - 1);
        long value = intervalValue.getNumber().longValue();
        int protocolStatus = intervalValue.getProtocolStatus();     //In case of epoch value, this status is set to indicate that the unit should be seconds!
        Unit unit = protocolStatus == 0 ? getUnit(baseObisCode) : Unit.get(protocolStatus);
        HistoricalRegister cosemValue = new HistoricalRegister();
        cosemValue.setQuantityValue(BigDecimal.valueOf(value), unit);

        Date billingTimeStamp = profileData.getIntervalData(getReversedBillingPoint(billingPoint)).getEndTime();
        Date eventTimeStamp = hasCaptureTimeAttribute(baseObisCode) ? extractEventTime(profileData, channelIndex, billingPoint)
                : billingTimeStamp;
        return new HistoricalValue(cosemValue, billingTimeStamp, eventTimeStamp, 0);
    }

    private Date extractEventTime(ProfileData profileData, int channelIndex, int billingPoint) throws IOException {
        Date eventTime = null;
        IntervalValue intervalValue = (IntervalValue) profileData.getIntervalData(getReversedBillingPoint(billingPoint))
                .getIntervalValues()
                .get(channelIndex);
        if (intervalValue.getNumber() != null) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(intervalValue.getNumber().longValue() * 1000);

            eventTime = calendar.getTime();
        }
        return eventTime;
    }

    private int getReversedBillingPoint(int billingPoint) throws IOException {
        return getBillingPointCounter() - billingPoint - 1;
    }

    /**
     * Used the profile data object so we can sort the interval data (ascending)
     */
    public ProfileData getProfileData() throws IOException {
        ProfileData profileData = new ProfileData();
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        IntervalValue intervalValue;

        DataStructure root = getFullBuffer().getRoot();
        for (int index = 0; index < root.getNrOfElements(); index++) {
            DataStructure structure = root.getStructure(index);
            Date timeStamp = new Date();
            List<IntervalValue> values = new ArrayList<IntervalValue>();
            for (int channel = 0; channel < structure.getNrOfElements(); channel++) {
                if (channel == 0) {
                    timeStamp = structure.getOctetString(0).toDate(protocol.getTimeZone());
                } else {
                    long value;
                    int unit = 0;
                    Object element = structure.getElement(channel);
                    if (element instanceof OctetString) {
                        //Epoch in seconds
                        value = ((OctetString) element).toCalendar(protocol.getTimeZone()).getTimeInMillis() / 1000;
                        unit = BaseUnit.SECOND;
                    } else {
                        //Normal energy consumption value
                        value = structure.getInteger(channel);
                    }
                    intervalValue = new IntervalValue(value, unit, 0);
                    values.add(intervalValue);
                }
            }
            intervalDatas.add(new IntervalData(timeStamp, 0, 0, 0, values));
        }
        profileData.setIntervalDatas(intervalDatas);
        profileData.sort();

        return profileData;
    }

    private Unit getUnit(ObisCode baseObisCode) throws IOException {
        int classID = protocol.getDlmsSession().getMeterConfig().findObject(baseObisCode).getClassID();
        if (classID == DLMSClassId.REGISTER.getClassId()) {
            return getCosemObjectFactory().getRegister(baseObisCode).getScalerUnit().getEisUnit();
        } else if (classID == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            return getCosemObjectFactory().getExtendedRegister(baseObisCode).getScalerUnit().getEisUnit();
        } else {
            throw new NoSuchRegisterException();
        }
    }

    private boolean hasCaptureTimeAttribute(ObisCode baseObisCode) throws IOException {
        int classID = protocol.getDlmsSession().getMeterConfig().findObject(baseObisCode).getClassID();
        if (classID == DLMSClassId.DEMAND_REGISTER.getClassId() || classID == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            return true;
        }
        return false;
    }

    /**
     * Check if the requested register is in the captured_objects of the billing load profile.
     * <p/>
     * In case of max demand registers (extended registers), the B-field indicates the attribute.
     * This can be 2 (value) or 5 (capture_time).
     */
    private int checkIfObisCodeIsCaptured(ObisCode obisCode) throws IOException {
        int attributeNumber = 2;
        if (isMaxDemandRegister(obisCode)) {
            if (obisCode.getB() != 0) {
                attributeNumber = obisCode.getB();
                obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) 0);
            }
        }
        int channelIndex = 0;
        List<CapturedObject> capturedObjects = getCapturedObjects();
        for (CapturedObject capturedObject : capturedObjects) {
            if (capturedObject.getLogicalName().getObisCode().equals(obisCode)) {
                if (capturedObject.getAttributeIndex() == attributeNumber) {
                    return channelIndex;
                }
            }
            channelIndex++;
        }
        throw new NoSuchRegisterException("Obiscode " + obisCode.toString() + " is not stored in the billing profile");
    }

    /**
     * This is a max demand register if the obiscode is 1.B.1.6.E.255
     * B indicates the captured attribute (2 = value, 5 = captureTime (epoch).
     * E indicates the tariff (<= 10: tariff1, >10: tariff2)
     */
    private boolean isMaxDemandRegister(ObisCode obisCode) {
        return obisCode.getA() == 1 && obisCode.getC() == 1 && obisCode.getD() == 6;
    }

    /**
     * Return the proper profile generic.
     * This is the monthly billing load profile if the F-field of the register is between 0 and 12,
     * or the daily billing load profile otherwise.
     * Note that there's 2 monthly and 2 daily billing profiles, 1 for tariff1 and 1 for tariff2.
     * The tariff is chosen in the E-field of the register.
     */
    public ProfileGeneric getProfileGeneric() throws NotInObjectListException {
        int e;
        if (isTariff1()) {
            e = 1;
        } else if (isTariff2()) {
            e = 2;
        } else {
            e = 1; //Total registers (not tariff'ed) are present in both profiles
        }

        if (isMonthlyRegister()) {
            ObisCode obisCode = ProtocolTools.setObisCodeField(MONTHLY_BILLING_PROFILE, 4, (byte) e);
            return getCosemObjectFactory().getProfileGeneric(obisCode);
//            return new ProfileGeneric(protocol, new ObjectReference(obisCode.getLN()));
        } else if (isDailyRegister()) {
            ObisCode obisCode = ProtocolTools.setObisCodeField(DAILY_BILLING_PROFILE, 4, (byte) e);
            return getCosemObjectFactory().getProfileGeneric(obisCode);
//            return new ProfileGeneric(protocol, new ObjectReference(obisCode.getLN()));
        } else {
            return null;
        }
    }

    /**
     * 12 or higher: daily billing register
     */
    private boolean isDailyRegister() {
        return currentObisCode.getF() >= 12;
    }

    /**
     * 0 to 11: monthly billing register
     */
    private boolean isMonthlyRegister() {
        return currentObisCode.getF() < 12 && currentObisCode.getF() >= 0;
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

    private CosemObjectFactory getCosemObjectFactory(){
        return protocol.getDlmsSession().getCosemObjectFactory();
    }
}
