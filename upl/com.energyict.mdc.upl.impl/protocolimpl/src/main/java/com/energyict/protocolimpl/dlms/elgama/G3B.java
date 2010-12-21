package com.energyict.protocolimpl.dlms.elgama;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.LocalSecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.FraudDetectionLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.PowerFailureLog;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 6-dec-2010
 * Time: 15:29:28
 */
public class G3B extends AbstractDLMSProtocol {

	private static final int CLEAR_LOADPROFILE = 0x4000;
	private static final int CLEAR_LOGBOOK = 0x2000;
	private static final int END_OF_ERROR = 0x0400;
	private static final int BEGIN_OF_ERROR = 0x0200;
	private static final int VARIABLE_SET = 0x0100;
	private static final int POWER_FAILURE = 0x0080;
	private static final int POWER_RECOVERY = 0x0040;
	private static final int DEVICE_CLOCK_SET_INCORRECT = 0x0020;
	private static final int DEVICE_RESET = 0x0010;
	private static final int DISTURBED_MEASURE = 0x0004;
	private static final int RUNNING_RESERVE_EXHAUSTED = 0x0002;
	private static final int FATAL_DEVICE_ERROR = 0x0001;


    private static final int MILLIS_1_DAY = 60 * 60 * 24 * 1000;
    private static final ObisCode OBIS_CODE_PROFILE_1 = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode OBISCODE_ACTIVE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_CLOCK = ObisCode.fromString("0.0.1.0.0.255");

    /**
     * Getter for the type of reference (0 = LN, 1 = SN)
     * @return the type of reference
     */
    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

    public boolean isRequestTimeZone() {
        return false;
    }

    public int getRoundTripCorrection() {
        return 0;
    }

    public StoredValues getStoredValues() {
        return null;
    }

    /**
     * Getter for the protocol version
     * @return the protocol version (being the date of the latest commit)
     */
    public String getProtocolVersion() {
        return "$Date$";
    }

    /**
     * Requests the meter it's firmware version via DLMS.
     * @return the meter it's firmware version
     * @throws IOException when there's a problem communicating with the meter.
     */
	public final String getFirmwareVersion() throws IOException {
		if (this.firmwareVersion == null) {
			this.firmwareVersion = AXDRDecoder.decode(this.getCosemObjectFactory().getData(OBISCODE_ACTIVE_FIRMWARE).getData()).getOctetString().stringValue();
		}
		return this.firmwareVersion;
    }

    /**
     * Requests the meter's profile data
     * @param includeEvents: enable or disable tht reading of meterevents
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
     * @param includeEvents: enable or disable tht reading of meterevents
     * @param lastReading: the from date to start reading at
     * @return the profile data
     * @throws IOException when there's a problem communicating with the meter.
     */
    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    /**
     * Requests the meter's profile data
     * @param from: the from date to start reading at
     * @param to request the to date, to stop reading at
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
        return getProfileData(calFrom, calTo, includeEvents);
    }

    /**
     * Set the meter's clock.
     * Use a time shift when the shift is smaller than +/- 15 minutes
     * Else, use a write time (= force clock).
     * @throws IOException when there's a problem communicating with the meter.
     */
    public final void setTime() throws IOException {
		logger.info("Setting the time of the remote device, first requesting the device's time.");
		final Clock clock = this.getCosemObjectFactory().getClock();
		boolean timeAdjusted = false;
		int currentTry = 1;

		while (!timeAdjusted && (currentTry <= this.numberOfClocksetTries)) {
			logger.info("Requesting clock for adjustment");
			final long startTime = System.currentTimeMillis();
			final Date deviceTime = clock.getDateTime();
			final long endTime = System.currentTimeMillis();
			if (endTime - startTime <= this.clockSetRoundtripTreshold) {
				final long roundtripCorrection = (endTime - startTime) / 2;
				final long timeDifference = System.currentTimeMillis() - (deviceTime.getTime() + roundtripCorrection);
				logger.info("Time difference is [" + timeDifference + "] miliseconds (using roundtrip time of [" + roundtripCorrection + "] milliseconds)");

				if (Math.abs(timeDifference / 1000) <= Clock.MAX_TIME_SHIFT_SECONDS) {
					logger.info("Time difference can be corrected using a time shift, invoking.");
					clock.shiftTime((int) (timeDifference / 1000));
				} else {
					logger.info("Time difference is too big to be corrected using a time shift, setting absolute date and time.");
					final Date date = new Date(System.currentTimeMillis() + roundtripCorrection);
					final Calendar newTimeToSet = Calendar.getInstance();
					newTimeToSet.setTime(date);
					this.setDeviceTime(newTimeToSet);         //Write time, instead of shifting it. = force clock
				}
				timeAdjusted = true;
			} else {
				logger.info("Roundtrip to the device took [" + (endTime - startTime) + "] milliseconds, which exceeds the treshold of [" + this.clockSetRoundtripTreshold + "] milliseconds, retrying");
			}
			currentTry++;
		} 
		if (!timeAdjusted) {
			logger.log(Level.WARNING, "Cannot set time, did not have a roundtrip that took shorter than [" + this.clockSetRoundtripTreshold + "] milliseconds. Not setting clock.");
		}
	}

    /**
     * Used to force the clock.
     * Prepares a fitting byte array that represents a DLMS command for the meter
     * @param newTime the time to be set
     * @throws IOException when there's a problem communicating with the meter.
     */
    private void setDeviceTime(final Calendar newTime) throws IOException {
		newTime.add(Calendar.MILLISECOND, roundtripCorrection);
		final byte[] byteTimeBuffer = new byte[14];
		byteTimeBuffer[0] = DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING;
		byteTimeBuffer[1] = 12; // length   TODO: LENGTH IS 13 ??
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
     * Getter for the profile data interval.
     * @return the profile data interval, eg. 900 seconds
     * @throws IOException when there's a problem communicating with the meter.
     */
	public final int getProfileInterval() throws IOException {
		if (this.profileInterval == -1) {
			logger.info("Requesting the profile interval from the meter...");
			final ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(OBIS_CODE_PROFILE_1);
			profileInterval = profileGeneric.getCapturePeriod();
			logger.info("Profile interval is [" + profileInterval + "]");
		}
		return this.profileInterval;
	}

    /**
     * Getter for the meter time
     * @return the meter time
     * @throws IOException when there's a problem communicating with the meter.
     */
    public Date getTime() throws IOException {
        return getCosemObjectFactory().getClock().getDateTime();
    }

    /**
     * Getter for the number of channels
     * @return the number of channels
     * @throws IOException when there's a problem communicating with the meter.
     */
    public final int getNumberOfChannels() throws IOException {
		if (this.numberOfChannels == -1) {
			logger.info("Loading the number of channels, looping over the captured objects...");
			numberOfChannels = getCapturedObjectsHelper().getNrOfchannels();
			logger.info("Got [" + this.numberOfChannels + "] actual channels in load profile (out of [" + this.capturedObjectsHelper.getNrOfCapturedObjects() + "] captured objects)");
		}
		return this.numberOfChannels;
	}

    /**
     * Helper object for the captured objects
     * @return the helper object
     * @throws IOException when there's a problem communicating with the meter.
     */
    private CapturedObjectsHelper getCapturedObjectsHelper() throws IOException {
		if (this.capturedObjectsHelper == null) {
			logger.info("Initializing the CapturedObjectsHelper using the generic profile, profile OBIS code is [" + OBIS_CODE_PROFILE_1.toString() + "]");
			final ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(OBIS_CODE_PROFILE_1);
			capturedObjectsHelper = profileGeneric.getCaptureObjectsHelper();
			logger.info("Done, load profile [" + OBIS_CODE_PROFILE_1 + "] has [" + capturedObjectsHelper.getNrOfCapturedObjects() + "] captured objects...");
		}
		return this.capturedObjectsHelper;
	}

    /**
     * Getter for the list of optional keys
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
     *
     * @param obisCode: obiscode of the register to lookup
     * @return the matching register info
     * @throws IOException  when there's a problem communicating with the meter.
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    /**
     * Getter for the scaler unit
     * @param channelId: the channel id for the scaler unit
     * @return the scaler unit
     * @throws IOException when there's a problem communicating with the meter.
     */
    private ScalerUnit getRegisterScalerUnit(final int channelId) throws IOException {
		ScalerUnit unit;
		if (getCapturedObjectsHelper().getProfileDataChannelCapturedObject(channelId).getClassId() == DLMSClassId.REGISTER.getClassId()) {
			unit = this.getCosemObjectFactory().getRegister(getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId)).getScalerUnit();
		} else if (getCapturedObjectsHelper().getProfileDataChannelCapturedObject(channelId).getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
			unit = this.getCosemObjectFactory().getDemandRegister(getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId)).getScalerUnit();
		} else if (getCapturedObjectsHelper().getProfileDataChannelCapturedObject(channelId).getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
			unit = this.getCosemObjectFactory().getExtendedRegister(getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId)).getScalerUnit();
		} else {
			throw new IllegalArgumentException("G3B, getRegisterScalerUnit(), invalid channelId, " + channelId);
		}
		if (unit.getUnitCode() == 0) {
			logger.info("Channel [" + channelId + "] has a scaler unit with unit code [0], using a unitless scalerunit.");
			unit = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
		}
		return unit;
	}

    /**
     * Maps the interval end time to a calendar
     * @param cal the calendar object
     * @param intervalData the intervaldata
     * @param btype: a flag containing info about the minutes
     * @return the calendar object
     * @throws IOException when there's a problem communicating with the meter.
     */
    private Calendar mapIntervalEndTimeToCalendar(final Calendar cal, final DataStructure intervalData, final byte btype) throws IOException {
        final Calendar calendar = (Calendar) cal.clone();

        if (intervalData.getOctetString(0).getArray()[0] != -1) {
            calendar.set(Calendar.YEAR, ((intervalData.getOctetString(0).getArray()[0] & 0xff) << 8) | ((intervalData.getOctetString(0).getArray()[1] & 0xff)));
        }

        if (intervalData.getOctetString(0).getArray()[2] != -1) {
            calendar.set(Calendar.MONTH, (intervalData.getOctetString(0).getArray()[2] & 0xff) - 1);
        }

        if (intervalData.getOctetString(0).getArray()[3] != -1) {
            calendar.set(Calendar.DAY_OF_MONTH, (intervalData.getOctetString(0).getArray()[3] & 0xff));
        }

        if (intervalData.getOctetString(0).getArray()[5] != -1) {
            calendar.set(Calendar.HOUR_OF_DAY, (intervalData.getOctetString(0).getArray()[5] & 0xff));
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
        }

        if (btype == 0) {
            if (intervalData.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, ((intervalData.getOctetString(0).getArray()[6] & 0xff) / (getProfileInterval() / 60)) * (getProfileInterval() / 60));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            calendar.set(Calendar.SECOND, 0);
        } else {
            if (intervalData.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, (intervalData.getOctetString(0).getArray()[6] & 0xff));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            if (intervalData.getOctetString(0).getArray()[7] != -1) {
                calendar.set(Calendar.SECOND, (intervalData.getOctetString(0).getArray()[7] & 0xff));
            } else {
                calendar.set(Calendar.SECOND, 0);
            }
        }

        // if DST, add 1 hour
        if (intervalData.getOctetString(0).getArray()[11] != -1) {
            if ((intervalData.getOctetString(0).getArray()[11] & (byte) 0x80) == 0x80) {
                calendar.add(Calendar.HOUR_OF_DAY, -1);
            }
        }

        return calendar;
    }

    /**
     * Maps the interval state bits to a status for EiServer
     * @param protocolStatus: the interval state bits
     * @return the EiServer status
     */
    private int map2IntervalStateBits(final int protocolStatus) {
        int eiStatus = 0;

        if ((protocolStatus & CLEAR_LOADPROFILE) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & CLEAR_LOGBOOK) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & END_OF_ERROR) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & BEGIN_OF_ERROR) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & VARIABLE_SET) != 0) {
            eiStatus |= IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if ((protocolStatus & DEVICE_CLOCK_SET_INCORRECT) != 0) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatus & FATAL_DEVICE_ERROR) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & DISTURBED_MEASURE) != 0) {
            eiStatus |= IntervalStateBits.CORRUPTED;
        }
        if ((protocolStatus & POWER_FAILURE) != 0) {
            eiStatus |= IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatus & POWER_RECOVERY) != 0) {
            eiStatus |= IntervalStateBits.POWERUP;
        }
        if ((protocolStatus & DEVICE_RESET) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & RUNNING_RESERVE_EXHAUSTED) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }

        return eiStatus;
    }

    /**
     * Requests the profile data from the meter
     * @param from: the from date
     * @param to: the to date
     * @param includeEvents: boolean, whether or not to include the events in the profile 
     * @return the profile data
     * @throws IOException when there's a problem communicating with the meter.
     */
    private ProfileData getProfileData(final Calendar from, final Calendar to, final boolean includeEvents) throws IOException {
        logger.info("Loading profile data starting at [" + from.getTime().toString() + "], ending at [" + to.getTime().toString() + "], " + (includeEvents ? "" : "not") + " including events");
        final ProfileData profileData = new ProfileData();
        final ProfileGeneric profileGeneric = this.getCosemObjectFactory().getProfileGeneric(OBIS_CODE_PROFILE_1);
        final DataContainer datacontainer = profileGeneric.getBuffer(from, to);

        logger.info("Building channel information...");
        for (int i = 0; i < this.getNumberOfChannels(); i++) {
            final ScalerUnit scalerUnit = this.getRegisterScalerUnit(i);
            logger.info("Scaler unit for channel [" + i + "] is [" + scalerUnit + "]");
            final ChannelInfo channelInfo = new ChannelInfo(i, "G3B_" + i, scalerUnit.getUnit());
            final CapturedObject channelCapturedObject = getCapturedObjectsHelper().getProfileDataChannelCapturedObject(i);
            if(ParseUtils.isObisCodeCumulative(channelCapturedObject.getLogicalName().getObisCode())) {
                logger.info("Indicating that channel [" + i + "] is cumulative...");
                channelInfo.setCumulative();
            }
            profileData.addChannel(channelInfo);                                                         
        }

        logger.info("Building profile data...");
        final Object[] loadProfileEntries = datacontainer.getRoot().getElements();
        if (loadProfileEntries.length == 0) {
            logger.log(Level.INFO, "There are no entries in the load profile, nothing to build...");
        } else {
            logger.log(Level.INFO, "Got [" + datacontainer.getRoot().element.length + "] entries in the load profile, building profile data...");

            for (int i = 0; i < loadProfileEntries.length; i++) {
                logger.info("Processing interval [" + i + "]");
                final DataStructure intervalData = datacontainer.getRoot().getStructure(i);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Mapping interval end time...");
                }
                Calendar calendar = ProtocolUtils.initCalendar(false, this.timeZone);
                calendar = this.mapIntervalEndTimeToCalendar(calendar, intervalData, (byte) 0);
                final int eiStatus = this.map2IntervalStateBits(intervalData.getInteger(1));
                final int protocolStatus = intervalData.getInteger(1);
                final IntervalData data = new IntervalData(new Date(((Calendar) calendar.clone()).getTime().getTime()), eiStatus, protocolStatus);
                logger.info("Adding channel data.");
                for (int j = 0; j < getCapturedObjectsHelper().getNrOfCapturedObjects(); j++) {
                    if (getCapturedObjectsHelper().isChannelData(j)) {
                        data.addValue(intervalData.getInteger(j));
                    }
                }
                profileData.addInterval(data);
            }
        }
        if (includeEvents) {
            logger.info("Requested to include meter events, loading...");
            profileData.setMeterEvents(this.getMeterEvents(from, to));
        }
        return profileData;
    }

    /**
     * Getter for the meter events. Used while requesting the profile data (if the boolean includeEvents is true)
     * @param from: the from date
     * @param to: the to date
     * @return a list of meter events
     * @throws IOException when there's a problem communicating with the meter.
     */
    private List<MeterEvent> getMeterEvents(final Calendar from, final Calendar to) throws IOException {
		logger.info("Fetching meter events from [" + (from != null ? from.getTime() : "Not specified") + "] to [" + (to != null ? to.getTime() : "Not specified") + "]");
		final List<MeterEvent> events = new ArrayList<MeterEvent>();

        final DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode()).getBuffer(from, to);
		final DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode()).getBuffer(from, to);

        final PowerFailureLog powerFailure = new PowerFailureLog(getTimeZone(), dcPowerFailure);
		final FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(getTimeZone(), dcFraudDetection);

		events.addAll(fraudDetectionEvents.getMeterEvents());
		events.addAll(powerFailure.getMeterEvents());

		return events;
	}

    /**
     * Requests the register values from the meter
     * @param obisCode obiscode mapped register to request from the meter
     * @return the register values
     * @throws IOException when there's a problem communicating with the meter.
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
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