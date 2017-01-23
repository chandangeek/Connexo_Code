package com.energyict.protocolimpl.dlms.elgama;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.HistoricalRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.RetryHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class G3BStoredValues implements StoredValues {

    public static final ObisCode OBISCODE_BILLING_PROFILE = ObisCode.fromString("1.0.98.1.0.126");

    private final CosemObjectFactory cosemObjectFactory;
    private List<UnitInfo> unitInfos = new ArrayList<UnitInfo>();
    private ProfileGeneric profileGeneric = null;
    private List<ObisCode> capturedCodes = null;
    private Array dataArray = null;
    private boolean hasATimeStamp = false;
    private TimeZone timeZone;

    public G3BStoredValues(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    /**
     * Cached list of obis codes that are stored in the billing profile.
     * @return list
     * @throws IOException
     */
    private List<ObisCode> getCapturedCodes() throws IOException {
        if (capturedCodes == null) {
            this.capturedCodes = new ArrayList<ObisCode>();
            for (CapturedObject co : getProfileGeneric().getCaptureObjects()) {
                if (co.getClassId() != DLMSClassId.CLOCK.getClassId()) {
                    capturedCodes.add(co.getLogicalName().getObisCode());
                }
            }
        }
        return capturedCodes;
    }

    private Array getDataArray() throws IOException {
        if (dataArray == null) {
            dataArray = new Array(getProfileGeneric().getBufferData(), 0, 0);
        }
        return dataArray;
    }

    public int getBillingPointCounter() throws IOException {
        return getDataArray().nrOfDataTypes();
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        if (!isValidBillingPoint(billingPoint)) {
            throw new UnsupportedException("Billing point [" + billingPoint + "] doesn't exist.");
        }
        return getDataArray().getDataType(billingPoint).getStructure().getDataType(0).getOctetString().getDateTime(getTimeZone()).getValue().getTime();
    }

    private TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = getCosemObjectFactory().getProtocolLink().getTimeZone();
        }
        return timeZone;
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);

        int billingPoint = obisCode.getF();
        if (!isValidBillingPoint(billingPoint)) {
            throw new UnsupportedException("Billing point " + billingPoint + " doesn't exist for obiscode " + baseObisCode + ".");
        }

        billingPoint = reverseBillingPointOrder(billingPoint);

        RetryHandler retryHandler = new RetryHandler();
        Date billingPointTimeDate;
        HistoricalRegister historicalRegister;
        do {
            try {
                historicalRegister = new HistoricalRegister();
                AbstractDataType abstractAmount = getAbstractValue(baseObisCode, billingPoint);
                billingPointTimeDate = getBillingPointTimeDate(billingPoint);
                Unit unit = getUnit(baseObisCode);
                ScalerUnit scalerUnit = getScalerUnit(baseObisCode);
                BigDecimal value;

                if (abstractAmount.isInteger64()) {
                        value = new BigDecimal(abstractAmount.getInteger64().getValue());
                    } else if (abstractAmount.isUnsigned32()) {
                        value =  new BigDecimal(abstractAmount.getUnsigned32().getValue());
                    } else if (abstractAmount.isUnsigned8()) {
                        value =  new BigDecimal(abstractAmount.getUnsigned8().getValue());
                    } else  {
                        throw new UnsupportedException("Unrecognized billing data");
                }

                //Fetch the event timestamp (in case of a maximum value entry)
                if (hasATimeStamp()) {
                    OctetString octetString = getEventTimeStamp(baseObisCode, billingPoint).getOctetString();
                    if (isValidOctetString(octetString)) {
                        DateTime dateTime = octetString.getDateTime(getTimeZone());
                        historicalRegister.setEventTime(dateTime.getValue().getTime());
                    }
                }

                historicalRegister.setBillingDate(billingPointTimeDate);
                historicalRegister.setCaptureTime(new Date());
                historicalRegister.setQuantityValue(value, unit);
                historicalRegister.setScalerUnit(scalerUnit);

                return new HistoricalValue(historicalRegister, billingPointTimeDate, historicalRegister.getEventTime(), getProfileGeneric().getResetCounter());

            } catch (DataAccessResultException e) {
                retryHandler.logFailure(e);
                getCosemObjectFactory().getProtocolLink().getLogger().warning("Problem while reading historical value " + obisCode + ", will retry.");
            }
        } while (retryHandler.canRetry());

        throw new IOException("Could not construct a proper historicalValue for ObisCode " + obisCode);
    }

    /**
     * The meter stores the billing data in a different order than conventional in EiServer.
     * billingPoint 0 contains the oldest data while this should be the most recent data!
     * @param billingPoint
     * @return
     * @throws IOException
     */
    private int reverseBillingPointOrder(int billingPoint) throws IOException {
        return getBillingPointCounter() - billingPoint - 1;
    }


    /**
     * An octetString is invalid if the bytes are all 0xFF.
     * @param octetString
     * @return
     */
    private boolean isValidOctetString(OctetString octetString) {
        byte[] octetBytes = octetString.getContentByteArray();
        return (octetBytes[0] != -1) || (octetBytes[1] != -1) || (octetBytes[2] != -1) || (octetBytes[3] != -1) || (octetBytes[4] != -1);
    }

    private ScalerUnit getScalerUnit(ObisCode baseObisCode) throws IOException {
        return getUnitInfo(baseObisCode).scalerUnit;
    }

    private Unit getUnit(ObisCode baseObisCode) throws IOException {
        return getUnitInfo(baseObisCode).unit;
    }

    private List<UnitInfo> getUnitInfos() {
        return unitInfos;
    }

    private UnitInfo getUnitInfo(ObisCode baseObisCode) throws IOException {
        for (UnitInfo info : getUnitInfos()) {
            if (info.obis.equals(baseObisCode)) {
                return info;
            }
        }
        Register reg = getCosemObjectFactory().getRegister(baseObisCode);
        Unit unit = reg.getQuantityValue().getUnit();
        ScalerUnit scalerUnit = reg.getScalerUnit();
        UnitInfo unitInfo = new UnitInfo(baseObisCode, unit, scalerUnit);
        getUnitInfos().add(unitInfo);
        return unitInfo;
    }

    /**
     * Gets a certain amount stored in the billing profile.
     * Timestamps for a maximum value are stored (under the same obiscode) in the next entry (= index2)
     * Flag "hasATimeStamp" is set in that case.
     */
    private AbstractDataType getAbstractValue(ObisCode baseObisCode, int billingPoint) throws IOException {
        int index1 = getCapturedCodes().indexOf(baseObisCode) + 1;   //Offset: The clock register is not stored in the captured codes list but is sent in the billing profile data.
        int index2 = getCapturedCodes().lastIndexOf(baseObisCode) + 1;
        if (index1 == -1) {
            throw new UnsupportedException(baseObisCode + " has no historical values.");
        }
        if (index1 == index2) {
            setHasATimeStamp(false);
        } else {
            setHasATimeStamp(true);
        }
        return getDataArray().getDataType(billingPoint).getStructure().getDataType(index1);
    }

    private AbstractDataType getEventTimeStamp(ObisCode baseObisCode, int billingPoint) throws IOException {
        int index1 = getCapturedCodes().indexOf(baseObisCode) + 2;   //Offset: - The clock register is not stored in the captured codes list but is sent in the billing profile data.
                                                                     //        - The event time stamp is stored one entry after the actual value
        if (index1 == -1) {
            throw new UnsupportedException(baseObisCode + " has no historical values.");
        }
        setHasATimeStamp(false);
        return getDataArray().getDataType(billingPoint).getStructure().getDataType(index1);
    }

    private void setHasATimeStamp(boolean b) {
        hasATimeStamp = b;
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

    public boolean hasATimeStamp() {
        return hasATimeStamp;
    }

    /**
     * This inner class is just a data container to cache the {@link Unit} and
     * {@link com.energyict.dlms.ScalerUnit} of a register.
     *
     * @author jme
     */
    private class UnitInfo {

        private final ObisCode obis;
        private final Unit unit;
        private final ScalerUnit scalerUnit;

        public UnitInfo(ObisCode obisCode, Unit unit, ScalerUnit scalerUnit) {
            this.obis = obisCode;
            this.unit = unit;
            this.scalerUnit = scalerUnit;
        }

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        String crlf = "\r\n";

        int billingPointCount = 0;

        try {
            billingPointCount = getBillingPointCounter();
        } catch (IOException e1) {
            billingPointCount = 0;
        }

        sb.append("G3BStoredValues").append(crlf);
        sb.append(" > getBillingPointCounter = ").append(billingPointCount).append(crlf);

        sb.append(" > obisCodes = ").append(crlf);
        try {
            for (ObisCode oc : getCapturedCodes()) {
                sb.append("     # ").append(oc).append(" - ");
                sb.append(oc.getDescription()).append(crlf);
            }
        } catch (IOException e) {
            sb.append(e.getMessage()).append(crlf);
        }

        sb.append(" > billingPointDates = ").append(crlf);
        for (int i = 0; i < billingPointCount; i++) {
            sb.append("     # ").append(i).append(" = ");
            try {
                sb.append(getBillingPointTimeDate(i)).append(crlf);
            } catch (IOException e) {
                sb.append(e.getMessage()).append(crlf);
            }
        }
        sb.append(crlf);

        return sb.toString();
    }
}