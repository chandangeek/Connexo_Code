package com.energyict.protocolimpl.dlms.elgama;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.LocalSecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 6-dec-2010
 * Time: 15:29:28
 */
public class G3B extends AbstractDLMSProtocol {

    private G3BStoredValues storedValuesImpl = null;
    private static final int MILLIS_1_DAY = 60 * 60 * 24 * 1000;
    private static final int MAX_TIME_SHIFT_SECONDS = 59;
    private static final ObisCode OBISCODE_LOAD_PROFILE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode OBISCODE_ACTIVE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_CLOCK = ObisCode.fromString("0.0.1.0.0.255");
    private static final ObisCode OBISCODE_SYNCHRONIZATION = ObisCode.fromString("1.0.96.130.5.255"
    );
    private ProfileGeneric loadProfile;
    private ProfileChannel profileChannel;

    /**
     * G3B adds the storedvalue impl to the init method.
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
        return "$Date$";
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
            firmwareVersion = AXDRDecoder.decode(data.getData()).getVisibleString().getStr();
		}
		return firmwareVersion;
    }

    /**
     * Requests the meter's profile data
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

                if (Math.abs(timeDifference / 1000) <= MAX_TIME_SHIFT_SECONDS) {
                    logger.info("Time difference [" + (timeDifference / 1000) + "]can be corrected using a time shift, invoking.");
                    clock.shiftTime((int) (timeDifference / 1000));    // TODO: object unavailable??? wrong obis code?
                    //Data syncData = getCosemObjectFactory().getData(OBISCODE_SYNCHRONIZATION);
                    //System.out.println(syncData.getValue());
                    //syncData.setValueAttr(new VisibleString("01"));
                    //System.out.println(syncData.getValue());

                } else {
                    logger.info("Time difference is too big to be corrected using a time shift, setting absolute date and time.");
                    final Date date = new Date(System.currentTimeMillis() + roundtripCorrection);
                    final Calendar newTimeToSet = Calendar.getInstance();
                    newTimeToSet.setTime(date);
                    setDeviceTime(newTimeToSet);         //Write time, instead of shifting it. = force clock
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
     * Used to force the clock.
     * Prepares a fitting byte array that represents a DLMS command for the meter
     *
     * @param newTime the time to be set
     * @throws IOException when there's a problem communicating with the meter.
     */
    private void setDeviceTime(final Calendar newTime) throws IOException {
        newTime.add(Calendar.MILLISECOND, 0);
        final byte[] byteTimeBuffer = new byte[14];
        byteTimeBuffer[0] = DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING;
        byteTimeBuffer[1] = 12; // length
        byteTimeBuffer[2] = (byte) (newTime.get(Calendar.YEAR) >> 8);
        byteTimeBuffer[3] = (byte) newTime.get(Calendar.YEAR);
        byteTimeBuffer[4] = (byte) (newTime.get(Calendar.MONTH) + 1);
        byteTimeBuffer[5] = (byte) newTime.get(Calendar.DAY_OF_MONTH);
        byte bDOW = (byte) newTime.get(Calendar.DAY_OF_WEEK);
        byteTimeBuffer[6] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteTimeBuffer[7] = (byte) newTime.get(Calendar.HOUR_OF_DAY);
        byteTimeBuffer[8] = (byte) newTime.get(Calendar.MINUTE);
        byteTimeBuffer[9] = (byte) newTime.get(Calendar.SECOND);
        byteTimeBuffer[10] = (byte) 0xFF;
        byteTimeBuffer[11] = (byte) 0x80;
        byteTimeBuffer[12] = (byte) 0x00;
        if (timeZone.inDaylightTime(newTime.getTime())) {
            byteTimeBuffer[13] = (byte) 0x80;
        } else {
            byteTimeBuffer[13] = (byte) 0x00;
        }
        getCosemObjectFactory().writeObject(OBISCODE_CLOCK, 8, 2, byteTimeBuffer);
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
        optionalKeys.add(LocalSecurityProvider.NEW_GLOBAL_KEY);
        optionalKeys.add(LocalSecurityProvider.NEW_AUTHENTICATION_KEY);
        optionalKeys.add(LocalSecurityProvider.NEW_HLS_SECRET);
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

            //Make the register ask for billing profile data.. temporary code.
            obisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 0);

            //TODO: EVENT TIME SEEMS TO BE 1 HOUR TO EARLY
            //Billing profile data, store it in a register
            if (obisCode.getF() != 255) {
                CosemObject cosemObject = getCosemObjectFactory().getCosemObject(obisCode);
                if (cosemObject instanceof HistoricalValue) {
                    HistoricalValue historicalValue = (HistoricalValue) cosemObject;
                    RegisterValue value = new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
                    System.out.println(value);
                    return value;
                }
            }

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
        HHUSignOn hhuSignOn = (HHUSignOn) new OpticalHHUConnection(commChannel);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, "");
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }
}