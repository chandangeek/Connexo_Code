package com.energyict.protocolimpl.dlms.JanzC280;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.HistoricalRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public class JanzStoredValues implements com.energyict.dlms.cosem.StoredValues {

    public static final ObisCode OBISCODE_DAILY_HISTORICAL_TOTALIZERS = ObisCode.fromString("1.0.98.2.0.255");
    public static final ObisCode OBISCODE_DAILY_HISTORICAL_MAXIMUM_DEMANDS = ObisCode.fromString("1.0.98.2.1.255");
    public static final ObisCode OBISCODE_DAILY_HISTORICAL_FIRST_TARIFF = ObisCode.fromString("1.0.98.2.2.255");
    public static final ObisCode OBISCODE_DAILY_HISTORICAL_SECOND_TARIFF = ObisCode.fromString("1.0.98.2.3.255");

    public static final ObisCode OBISCODE_CLOSE_BILLING_HISTORICAL_TOTALIZERS = ObisCode.fromString("1.0.98.1.0.255");
    public static final ObisCode OBISCODE_CLOSE_BILLING_HISTORICAL_MAXIMUM_DEMANDS = ObisCode.fromString("1.0.98.1.1.255");
    public static final ObisCode OBISCODE_CLOSE_BILLING_HISTORICAL_FIRST_TARIFF = ObisCode.fromString("1.0.98.1.2.255");
    public static final ObisCode OBISCODE_CLOSE_BILLING_HISTORICAL_SECOND_TARIFF = ObisCode.fromString("1.0.98.1.3.255");

    private static long BASE_NUMBER_OF_SECONDS = (long) 946684800; // = number of seconds 1 Jan 1970 UTC - 1 Jan 2000 UTC

    private final CosemObjectFactory cosemObjectFactory;
    private ProfileGeneric profileGeneric = null;
    private ObisCode profileGenericObisCode = null;
    private ObisCode lastReadProfileGenericObisCode = null;
    private int profileGenericObisCodeIdentifier = 0;
    private DataContainer[][] bufferStorage = new DataContainer[8][45];
    private JanzC280 janzC280;


    public JanzStoredValues(CosemObjectFactory cosemObjectFactory, JanzC280 janzC280) {
        this.cosemObjectFactory = cosemObjectFactory;
        this.janzC280 = janzC280;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public int getBillingPointCounter() throws IOException {
        return getProfileGeneric().getEntriesInUse();
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        // Not used
        return new Date();
    }

    private DataContainer getSelectiveBuffer(ObisCode obisCode) throws IOException {
        // Selective buffer retrieve is used to retrieve only historics for 1 billing period at a time.
        // eg.: getBuffer(1,1,1,0) retrieves the historic values of all registers (11 totalizers/maxima registers or 32 tariff registers) for the oldest billing period

        // All selective buffers retrieved will be stored in a buffer storage
        // This will allow to reuse the selective buffers

        int billingPointIdentifier = obisCode.getF() > 11 ? (obisCode.getF() - 12) : obisCode.getF();
        if (bufferStorage[profileGenericObisCodeIdentifier][billingPointIdentifier] == null) {
            int entry = getReversedBillingPoint(obisCode.getF()) + 1;   // fromEntry & toEntry are 1 based
             bufferStorage[profileGenericObisCodeIdentifier][billingPointIdentifier] = profileGeneric.getBuffer(entry, entry, 1, 0);
        }
            return bufferStorage[profileGenericObisCodeIdentifier][billingPointIdentifier];
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        checkIfObisCodeIsHistoric(obisCode);

        int billingPoint = obisCode.getF();
        if (!isValidBillingPoint(billingPoint)) {
            String msg = "Warning: Billing point " + obisCode.getF() + " doesn't exist for obiscode " + baseObisCode + ".";
            janzC280.getLogger().warning(msg);
            throw new NoSuchRegisterException(msg);
        }

        HistoricalValue historic;
        if (profileGenericObisCode == OBISCODE_DAILY_HISTORICAL_TOTALIZERS || profileGenericObisCode == OBISCODE_DAILY_HISTORICAL_MAXIMUM_DEMANDS ||
                profileGenericObisCode == OBISCODE_CLOSE_BILLING_HISTORICAL_TOTALIZERS || profileGenericObisCode == OBISCODE_CLOSE_BILLING_HISTORICAL_MAXIMUM_DEMANDS) {

            historic = getTotalOrMaximumDemand(obisCode);
        } else {
            historic = getHistoricTariff(obisCode);
        }

        return historic;
    }

    private int getReversedBillingPoint(int point) throws IOException {
        int billingPoint = point > 11 ? (point -12) : point;
        return getBillingPointCounter() - billingPoint - 1;
    }

    private HistoricalValue getTotalOrMaximumDemand(ObisCode obisCode) throws IOException {
        // Which of the 11 registers do we need? (getBuffer() reads out historics for 11 registers).
        int registerIndex = (obisCode.getC() < 11) ? (obisCode.getC() - 1) : 10;

        DataStructure structure = getSelectiveBuffer(obisCode).getRoot().getStructure(0).getStructure(0);
        long timeStamp = getSelectiveBuffer(obisCode).getRoot().getStructure(0).getValue(1);
        Calendar billingCal = getCalendarFromHoraLegal(timeStamp);

        // This structure contains the info of historical of specific register and for the set billingPoint.
        DataStructure registerStructure = structure.getStructure(registerIndex);

        float energy = registerStructure.getFloat(0);
        Unit unit =  Unit.get((int) registerStructure.getValue(2), (int) registerStructure.getValue(1));
        //int stateOrPeriod = (int) registerStructure.getValue(3);
        timeStamp = registerStructure.getValue(4);

        Calendar billingCollectionTimeCal = billingCal;
        if (timeStamp != 0) {
            billingCollectionTimeCal = getCalendarFromHoraLegal(timeStamp);
        }

        HistoricalRegister cosemValue = new HistoricalRegister();
        cosemValue.setQuantityValue(BigDecimal.valueOf(energy), unit);

        // In case of maximum demand, the eventTime contains the time on which the maximum occurred.
        Date eventTime = (profileGenericObisCode == OBISCODE_DAILY_HISTORICAL_MAXIMUM_DEMANDS || profileGenericObisCode == OBISCODE_CLOSE_BILLING_HISTORICAL_MAXIMUM_DEMANDS)
                        ? billingCollectionTimeCal.getTime()
                        : billingCal.getTime();
        return new HistoricalValue(cosemValue, billingCal.getTime(), eventTime, 0);
    }

    private HistoricalValue getHistoricTariff(ObisCode obisCode) throws IOException {
        // Which of the 32 tariff registers do we need?
        int registerIndex = obisCode.getE() - 1;

        DataStructure structure = getSelectiveBuffer(obisCode).getRoot().getStructure(0).getStructure(registerIndex);
        long timeStamp = getSelectiveBuffer(obisCode).getRoot().getStructure(0).getValue(32);
        Calendar billingCal = getCalendarFromHoraLegal(timeStamp);

        float energy = structure.getFloat(0);
        Unit unit =  Unit.get((int) structure.getValue(2), (int) structure.getValue(1));
        int associatedEnergy = (int) structure.getValue(3);
        if (associatedEnergy == 0) {
            throw new NoSuchRegisterException("Register "+ProtocolTools.setObisCodeField(obisCode, 5, (byte) 0xFF).toString() +" is not activated.");
        }

        HistoricalRegister cosemValue = new HistoricalRegister();
        cosemValue.setQuantityValue(BigDecimal.valueOf(energy), unit);
        return new HistoricalValue(cosemValue, billingCal.getTime(), billingCal.getTime(), 0);
    }

    // check if the given obiscode is an historic one and retrieve which profileGeneric is responsible for it
    private void checkIfObisCodeIsHistoric(ObisCode obisCode) throws IOException {

        //1. Energy totalizers
        if (obisCode.getA() == 1 && obisCode.getB() == 0 &&
                obisCode.getD() == 8 && obisCode.getE() == 0 &&
                (obisCode.getC()< 11 || obisCode.getC() == 15)) {
            if (obisCode.getF() < 12) {
                setProfileGenericObisCode(OBISCODE_CLOSE_BILLING_HISTORICAL_TOTALIZERS);
                profileGenericObisCodeIdentifier = 4;
            } else {
                setProfileGenericObisCode(OBISCODE_DAILY_HISTORICAL_TOTALIZERS);
                profileGenericObisCodeIdentifier = 0;
            }
            return;
        }

        //2. Maximum demands
        if (obisCode.getA() == 1 && obisCode.getB() == 0 &&
                obisCode.getD() == 6 && obisCode.getE() == 0 &&
                (obisCode.getC()< 11 || obisCode.getC() == 15)) {
            if (obisCode.getF() < 12) {
                setProfileGenericObisCode(OBISCODE_CLOSE_BILLING_HISTORICAL_MAXIMUM_DEMANDS);
                profileGenericObisCodeIdentifier = 5;
            } else {
                setProfileGenericObisCode(OBISCODE_DAILY_HISTORICAL_MAXIMUM_DEMANDS);
                profileGenericObisCodeIdentifier = 1;
            }
            return;
        }

        //3. First Tariff registers
        if (obisCode.getA() == 1 && obisCode.getB() == 0 &&
                obisCode.getC() == 96 && obisCode.getD() == 51 &&
                obisCode.getE() < 33) {
            if (obisCode.getF() < 12) {
                setProfileGenericObisCode(OBISCODE_CLOSE_BILLING_HISTORICAL_FIRST_TARIFF);
                profileGenericObisCodeIdentifier = 6;
            } else {
                setProfileGenericObisCode(OBISCODE_DAILY_HISTORICAL_FIRST_TARIFF);
                profileGenericObisCodeIdentifier = 2;
            }
            return;
        }

         //4. Second Tariff registers
        if (obisCode.getA() == 1 && obisCode.getB() == 0 &&
                obisCode.getC() == 96 && obisCode.getD() == 52 &&
                obisCode.getE() < 33) {
            if (obisCode.getF() < 12) {
                setProfileGenericObisCode(OBISCODE_CLOSE_BILLING_HISTORICAL_SECOND_TARIFF);
                profileGenericObisCodeIdentifier = 7;
            } else {
                setProfileGenericObisCode(OBISCODE_DAILY_HISTORICAL_SECOND_TARIFF);
                profileGenericObisCodeIdentifier = 3;
            }
            return;
        }

        throw new NoSuchRegisterException("Obiscode " + obisCode.toString() + " is not a historical.");
    }

    /**
     * Convert the Hora Legal timestamp to a Calendar
     *
     * @param horaLegal: Number of seconds [1 Jan 2000 - current UTC time] + time zone offset in seconds + daylight saving offset (0 if in winterTime | 3600 seconds if in summerTime).
     * @return a Calendar containing the date/time.
     */
    private Calendar getCalendarFromHoraLegal(long horaLegal) {
        Calendar gmtCal = ProtocolUtils.getCleanGMTCalendar();
        gmtCal.setTimeInMillis((BASE_NUMBER_OF_SECONDS + horaLegal) * 1000);

        Calendar localCal = Calendar.getInstance(janzC280.getTimeZone());
        localCal.set(Calendar.YEAR, gmtCal.get(Calendar.YEAR));
        localCal.set(Calendar.MONTH, gmtCal.get(Calendar.MONTH));
        localCal.set(Calendar.DAY_OF_MONTH, gmtCal.get(Calendar.DAY_OF_MONTH));
        localCal.set(Calendar.HOUR_OF_DAY, gmtCal.get(Calendar.HOUR_OF_DAY));
        localCal.set(Calendar.MINUTE, gmtCal.get(Calendar.MINUTE));
        localCal.set(Calendar.SECOND, gmtCal.get(Calendar.SECOND));
        localCal.set(Calendar.MILLISECOND, gmtCal.get(Calendar.MILLISECOND));
        return localCal;
    }

    public ProfileGeneric getProfileGeneric() {
        // If this profileGeneric is different from the previous used one, it must be retrieved from the meter.
        // If the same profileGenereic is requested again, the existing one can be returned
        if (profileGeneric == null || lastReadProfileGenericObisCode == null || !(lastReadProfileGenericObisCode.toString().equals(profileGenericObisCode.toString()))) {
            try {
                profileGeneric = getCosemObjectFactory().getProfileGeneric(profileGenericObisCode);
                lastReadProfileGenericObisCode = profileGenericObisCode;
            } catch (IOException e) {
                //Absorb exception
            }
        }
        return profileGeneric;
    }

    public void setProfileGenericObisCode(ObisCode profileGenericObisCode) {
        this.profileGenericObisCode = profileGenericObisCode;
    }

    public void retrieve() throws IOException {
        // Not implemented
    }

    private boolean isValidBillingPoint(int point) {
        try {
            int billingPoint = point > 11 ? (point - 12) : point;   // If point is > 11, then daily historics are requested.

            return (billingPoint >= 0) && (billingPoint < getBillingPointCounter());
        } catch (IOException e) {
            return false;
        }
    }
}