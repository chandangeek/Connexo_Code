package com.energyict.protocolimpl.dlms.elgama;

import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 6-dec-2010
 * Time: 15:29:28
 */
public class G3B extends AbstractDLMSProtocol {

    @Override
    public String getProtocolDescription() {
        return "Elgama G3B DLMS";
    }

    private G3BStoredValues storedValuesImpl = null;
    private static final int MILLIS_1_DAY = 60 * 60 * 24 * 1000;
    private static final int MAX_TIME_SHIFT_SECONDS = 59;
    private static final ObisCode OBISCODE_LOAD_PROFILE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode OBISCODE_ACTIVE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_SYNCHRONIZATION = ObisCode.fromString("1.0.96.130.3.255");
    private ProfileGeneric loadProfile;
    private ProfileChannel profileChannel;

    @Inject
    public G3B(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService, ormClient);
    }

    /**
     * G3B adds the storedvalue impl to the init method. (via override)
     */
    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        iConfigProgramChange = -1;
        cosemObjectFactory = new CosemObjectFactory(this);
        dlmsMeterConfig = DLMSMeterConfig.getInstance(manufacturer);
        storedValuesImpl = new G3BStoredValues(getCosemObjectFactory());
        initDLMSConnection(inputStream, outputStream);
    }

    /**
     * Getter for the type of reference (0 = LN, 1 = SN)
     *
     * @return the type of reference
     */
    public int getReference() {
        return ProtocolLink.SN_REFERENCE;
    }

    public boolean isRequestTimeZone() {
        return false;
    }

    /**
     * Requests the meter's monthly billing profile data, stores it into a register in EiServer.
     */
    public StoredValues getStoredValues() {
        return storedValuesImpl;
    }

    /**
     * Getter for the profile data interval.
     *
     * @return the profile data interval, eg. 900 seconds
     * @throws IOException when there's a problem communicating with the meter.
     */
    public final int getProfileInterval() throws IOException {
        if (profileInterval == -1) {
            logger.info("Requesting the profile interval from the meter...");
            profileInterval = getLoadProfile().getCapturePeriod();
            logger.info("Profile interval is [" + profileInterval + "]");
        }
        return this.profileInterval;
    }

    public ProfileGeneric getLoadProfile() throws IOException {
        if (loadProfile == null) {
            loadProfile = getCosemObjectFactory().getProfileGeneric(OBISCODE_LOAD_PROFILE);
        }
        return loadProfile;
    }

    public int getRoundTripCorrection() {
        return 0;
    }

    public int getNumberOfChannels() throws IOException {
        return getProfileChannel().getNumberOfChannels();
    }

    /**
     * Getter for the protocol version
     *
     * @return the protocol version (being the date of the latest commit)
     */
    public String getProtocolVersion() {
        return "$Date: 2014-09-30 17:16:32 +0200 (Tue, 30 Sep 2014) $";
    }

    /**
     * Requests the meter it's firmware version via DLMS.
     *
     * @return the meter it's firmware version
     * @throws IOException when there's a problem communicating with the meter.
     */
    public final String getFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            Data data = getCosemObjectFactory().getData(OBISCODE_ACTIVE_FIRMWARE);
            firmwareVersion = AXDRDecoder.decode(data.getRawValueAttr()).getVisibleString().getStr();
        }
        return firmwareVersion;
    }

    /**
     * Requests the meter's profile data
     *
     * @param includeEvents: enable or disable the reading of meterevents
     * @return the profile data
     * @throws IOException when there's a problem communicating with the meter.
     */
    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Date lastReading = new Date(new Date().getTime() - MILLIS_1_DAY);
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    /**
     * Requests the meter's profile data
     *
     * @param includeEvents: enable or disable the reading of meterevents
     * @param lastReading:   the from date to start reading at
     * @return the profile data
     * @throws IOException when there's a problem communicating with the meter.
     */
    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    /**
     * Requests the meter's profile data
     *
     * @param from:         the from date to start reading at
     * @param to            request the to date, to stop reading at
     * @param includeEvents eneble or disable requesting of meterevents
     * @return the profile data
     * @throws IOException when there's a problem communicating with the meter.
     */
    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar calFrom = new GregorianCalendar();
        Calendar calTo = new GregorianCalendar();
        calFrom.setTime(from);
        calTo.setTime(to);
        return getProfileChannel().getProfileData(calFrom, calTo, includeEvents);
    }

    public ProfileChannel getProfileChannel() throws IOException {
        if (profileChannel == null) {
            profileChannel = new ProfileChannel(getLogger(), getProfileInterval(), getLoadProfile(), getTimeZone(), getCosemObjectFactory(), getMeterConfig());
        }
        return profileChannel;
    }

    /**
     * Set the meter's clock.
     * Use a time shift when the shift is smaller than +/- 15 minutes
     * Else, use a write time (= force clock).
     *
     * @throws IOException when there's a problem communicating with the meter.
     */
    public final void setTime() throws IOException {
        logger.info("Setting the time of the remote device, first requesting the device's time.");
        final Clock clock = getCosemObjectFactory().getClock();
        boolean timeAdjusted = false;
        int currentTry = 1;

        while (!timeAdjusted && (currentTry <= numberOfClocksetTries)) {
            logger.info("Requesting clock for adjustment");
            final long startTime = System.currentTimeMillis();
            final Date deviceTime = clock.getDateTime();
            final long endTime = System.currentTimeMillis();
            if (endTime - startTime <= clockSetRoundtripTreshold) {
                final long roundtripCorrection = (endTime - startTime) / 2;
                final long timeDifference = System.currentTimeMillis() - (deviceTime.getTime() + roundtripCorrection);
                logger.info("Time difference is [" + timeDifference + "] miliseconds (using roundtrip time of [" + roundtripCorrection + "] milliseconds)");
                int timeShift = (int) (timeDifference / 1000);
                if (Math.abs(timeShift) < 1) {
                    logger.info("Time difference [" + timeDifference + " ms] is too small to be corrected.");
                } else {
                    if (Math.abs(timeShift) <= MAX_TIME_SHIFT_SECONDS) {
                        logger.info("Time difference [" + timeShift + "] can be corrected using a time shift, invoking.");
                        Data synchronizationObject = getCosemObjectFactory().getData(OBISCODE_SYNCHRONIZATION);
                        synchronizationObject.setValueAttr(new VisibleString(getHexStringFromTimeShift(timeShift)));
                    } else {
                        logger.info("Time difference is too big to be corrected using a time shift, setting absolute date and time.");
                        final Date date = new Date(System.currentTimeMillis() + roundtripCorrection);
                        final Calendar newTimeToSet = Calendar.getInstance(getTimeZone());
                        newTimeToSet.setTime(date);
                        getCosemObjectFactory().getClock().setTimeAttr(new DateTime(newTimeToSet));
                    }
                }
                timeAdjusted = true;
            } else {
                logger.info("Roundtrip to the device took [" + (endTime - startTime) + "] milliseconds, which exceeds the treshold of [" + clockSetRoundtripTreshold + "] milliseconds, retrying");
            }
            currentTry++;
        }
        if (!timeAdjusted) {
            logger.log(Level.WARNING, "Cannot set time, did not have a roundtrip that took shorter than [" + clockSetRoundtripTreshold + "] milliseconds. Not setting clock.");
        }
    }

    /**
     * Converts the number of seconds (that need to be shifted) into a fitting hex string,
     * following the meter's conventions.
     */
    public String getHexStringFromTimeShift(int timeShift) throws IOException {
        if (timeShift < 0) {
            return ProtocolTools.getHexStringFromBytes(new byte[]{(byte) timeShift}, "");
        } else if (timeShift > 0) {
            return ProtocolTools.getHexStringFromBytes(new byte[]{(byte) (timeShift + 1 + 255)}, "");
        } else {
            return "";
        }
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    /**
     * Getter for the meter time
     *
     * @return the meter time
     * @throws IOException when there's a problem communicating with the meter.
     */
    public Date getTime() throws IOException {
        return getCosemObjectFactory().getClock().getDateTime();
    }

    /**
     * Getter for the list of optional keys
     *
     * @return the list of optional keys
     */
    @Override
    public List getOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        optionalKeys.add("ForceDelay");
        optionalKeys.add("TimeOut");
        optionalKeys.add("Retries");
        optionalKeys.add("Connection");
        optionalKeys.add("SecurityLevel");
        optionalKeys.add("ClientMacAddress");
        optionalKeys.add("ServerUpperMacAddress");
        optionalKeys.add("ServerLowerMacAddress");
        optionalKeys.add("InformationFieldSize");
        optionalKeys.add("LoadProfileId");
        optionalKeys.add("AddressingMode");
        optionalKeys.add("MaxMbusDevices");
        optionalKeys.add("IIAPInvokeId");
        optionalKeys.add("IIAPPriority");
        optionalKeys.add("IIAPServiceClass");
        optionalKeys.add("Manufacturer");
        optionalKeys.add("InformationFieldSize");
        optionalKeys.add("RoundTripCorrection");
        optionalKeys.add("IpPortNumber");
        optionalKeys.add("WakeUp");
        optionalKeys.add("CipheringType");
        optionalKeys.add(LocalSecurityProvider.DATATRANSPORTKEY);
        optionalKeys.add(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        optionalKeys.add(LocalSecurityProvider.MASTERKEY);
        return optionalKeys;
    }

    /**
     * Requests the register values from the meter
     *
     * @param obisCode obiscode mapped register to request from the meter
     * @return the register values
     * @throws IOException when there's a problem communicating with the meter.
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {

            //Billing profile data, store it in a register
            if (obisCode.getF() != 255) {
                CosemObject cosemObject = getCosemObjectFactory().getCosemObject(obisCode);
                if (cosemObject instanceof HistoricalValue) {
                    HistoricalValue historicalValue = (HistoricalValue) cosemObject;
                    return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
                }
            }

            //Regular register data
            final UniversalObject uo = getMeterConfig().findObject(obisCode);
            if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                final Register register = getCosemObjectFactory().getRegister(obisCode);
                return new RegisterValue(obisCode, register.getQuantityValue());
            } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                final DemandRegister register = getCosemObjectFactory().getDemandRegister(obisCode);
                return new RegisterValue(obisCode, register.getQuantityValue());
            } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                final ExtendedRegister register = getCosemObjectFactory().getExtendedRegister(obisCode);
                return new RegisterValue(obisCode, register.getQuantityValue());
            } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                final Disconnector register = getCosemObjectFactory().getDisconnector(obisCode);
                return new RegisterValue(obisCode, "" + register.getState());
            } else {
                throw new NoSuchRegisterException();
            }
        } catch (final Exception e) {
            throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
        }
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new OpticalHHUConnection(commChannel);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, "");
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void validateSerialNumber() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}