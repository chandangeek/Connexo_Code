package com.energyict.genericprotocolimpl.webrtuz3.historical;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 4-jun-2010
 * Time: 11:14:19
 */
public class HistoricalRegisterReadings {

    private static final ObisCode DATE_OBIS_CODE = ObisCode.fromString("0.0.1.0.0.255");

    private final CosemObjectFactory cosemObjectFactory;
    private final ObisCode dailyObisCode;
    private final ObisCode monthlyObisCode;

    private ProfileGeneric dailyProfile = null;
    private ProfileGeneric monthlyProfile = null;
    private Logger logger = null;

    /**
     * @param cosemObjectFactory
     * @param dailyObisCode
     * @param monthlyObisCode
     */
    public HistoricalRegisterReadings(CosemObjectFactory cosemObjectFactory, ObisCode dailyObisCode, ObisCode monthlyObisCode, Logger logger) {
        this.cosemObjectFactory = cosemObjectFactory;
        this.dailyObisCode = dailyObisCode;
        this.monthlyObisCode = monthlyObisCode;
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {

        }
        return logger;
    }

    /**
     * @param obisCode
     * @return
     * @throws NoSuchRegisterException
     */
    public RegisterValue readHistoricalMonthlyRegister(ObisCode obisCode) throws NoSuchRegisterException {
        try {
            return readAsRegisterValue(getMonthlyProfile(), obisCode);
        } catch (NoSuchRegisterException e) {
            throw e;
        } catch (IOException e) {
            throw new NoSuchRegisterException(e.getMessage());
        }
    }

    /**
     * @param obisCode
     * @return
     * @throws NoSuchRegisterException
     */
    public RegisterValue readHistoricalDailyRegister(ObisCode obisCode) throws NoSuchRegisterException {
        try {
            return readAsRegisterValue(getDailyProfile(), obisCode);
        } catch (NoSuchRegisterException e) {
            throw e;
        } catch (IOException e) {
            throw new NoSuchRegisterException(e.getMessage());
        }
    }

    /**
     * Getter for the CosemObjectFactory
     *
     * @return
     */
    private CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    /**
     * @return
     * @throws IOException
     */
    private ProfileGeneric getDailyProfile() throws IOException {
        if (dailyProfile == null) {
            dailyProfile = getCosemObjectFactory().getProfileGeneric(dailyObisCode);
        }
        return dailyProfile;
    }

    /**
     * @return
     * @throws IOException
     */
    private ProfileGeneric getMonthlyProfile() throws IOException {
        if (monthlyProfile == null) {
            monthlyProfile = getCosemObjectFactory().getProfileGeneric(monthlyObisCode);
        }
        return monthlyProfile;
    }

    /**
     * @param profileGeneric
     * @param obisCode
     * @return
     * @throws IOException
     */
    private RegisterValue readAsRegisterValue(ProfileGeneric profileGeneric, ObisCode obisCode) throws IOException {
        int billingIndex = obisCode.getF();
        int dateIndex = getClockIndex(profileGeneric);
        int valueIndex = getChannelIndex(profileGeneric, obisCode);

        Structure profileEntry = getProfileEntry(profileGeneric, billingIndex);
        Calendar eventDate = getEventDate(profileEntry, dateIndex);
        Unit unit = getChannelUnit(obisCode);
        BigDecimal value = getChannelValue(profileEntry, valueIndex);
        Quantity quantity = new Quantity(value, unit);
        RegisterValue registerValue = new RegisterValue(obisCode, quantity, eventDate.getTime());

        return registerValue;
    }

    /**
     * @param profileEntry
     * @param valueIndex
     * @return
     */
    private BigDecimal getChannelValue(Structure profileEntry, int valueIndex) {
        long profileValue = profileEntry.getDataType(valueIndex).longValue();
        BigDecimal value = BigDecimal.valueOf(profileValue);
        return value;
    }

    /**
     * @param obisCode
     * @return
     */
    private Unit getChannelUnit(ObisCode obisCode) {
        ObisCode cosemObjectObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        try {
            CosemObject cosemObject = getCosemObjectFactory().getCosemObject(cosemObjectObisCode);
            if (cosemObject == null) {
                throw new IOException("Unable to get cosemObject with obisCode " + cosemObjectObisCode.toString());
            }
            ScalerUnit scalerUnit = cosemObject.getScalerUnit();
            if (scalerUnit == null) {
                throw new IOException("Scaler unit was 'null'");
            }
            return scalerUnit.getUnit();
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to get the unit and the scaler for profile channel with obisCode [");
            sb.append(cosemObjectObisCode.toString()).append("] while reading billing data: ").append(e.getMessage() + " ");
            sb.append("The capturedObjectList should refer to an existing CosemObject of type REGISTER!");
            getLogger().log(Level.WARNING, sb.toString());
            return Unit.getUndefined();
        }
    }

    /**
     * @param profileGeneric
     * @param billingIndex
     * @return
     * @throws IOException
     */
    private Structure getProfileEntry(ProfileGeneric profileGeneric, int billingIndex) throws IOException {
        byte[] profile = profileGeneric.getBufferData();
        AbstractDataType cosemObject = AXDRDecoder.decode(profile);
        if (!(cosemObject instanceof Array)) {
            throw new IOException("The profileData should have a dataType of Array but was " + cosemObject.getClass().getSimpleName());
        }

        Array profileData = (Array) cosemObject;
        if (billingIndex >= profileData.nrOfDataTypes()) {
            throw new NoSuchRegisterException("No historical data available for billing point " + billingIndex + " at the moment.");
        }
        Structure profileEntry = profileData.getDataType(billingIndex).getStructure();
        return profileEntry;
    }

    /**
     * @param profileEntry
     * @param dateIndex
     * @return
     * @throws IOException
     */
    private Calendar getEventDate(Structure profileEntry, int dateIndex) throws IOException {
        AbstractDataType eventDateData = profileEntry.getDataType(dateIndex);
        if (!(eventDateData instanceof OctetString)) {
            throw new IOException("The captured timeStamp should be of type OctetStrinig but was " + eventDateData.getClass().getSimpleName());
        }
        Calendar eventDate = new AXDRDateTime(eventDateData.getOctetString()).getValue();
        return eventDate;
    }

    /**
     * @param profileGeneric
     * @return
     * @throws IOException
     */
    private int getClockIndex(ProfileGeneric profileGeneric) throws IOException {
        try {
            return getChannelIndex(profileGeneric, DATE_OBIS_CODE);
        } catch (IOException e) {
            throw new IOException("Could not find timeStamp: " + e.getMessage());
        }
    }

    /**
     * @param profileGeneric
     * @param obisCode
     * @return
     * @throws IOException
     */
    private int getChannelIndex(ProfileGeneric profileGeneric, ObisCode obisCode) throws IOException {
        ObisCode capturedOC = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        List<CapturedObject> captureObjects = profileGeneric.getCaptureObjects();
        for (int i = 0; i < captureObjects.size(); i++) {
            CapturedObject capturedObject = captureObjects.get(i);
            if (capturedObject.getLogicalName().getObisCode().equals(capturedOC)) {
                return i;
            }
        }
        throw new NoSuchRegisterException("ObisCode " + capturedOC.toString() + " is not found in CapturedObjects of profile.");
    }

}
