package com.energyict.protocolimpl.dlms.as220;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ObiscodeMapper;
import com.energyict.protocolimpl.base.RetryHandler;
import com.energyict.protocolimpl.base.SubMessageProtocol;
import com.energyict.protocolimpl.dlms.as220.emeter.AS220Messaging;
import com.energyict.protocolimpl.dlms.as220.emeter.EMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.plc.PLC;
import com.energyict.protocolimpl.dlms.as220.plc.PLCMessaging;
import com.energyict.protocolimpl.dlms.as220.powerquality.PowerQuality;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author kvds, jme
 *
 */
public class AS220 extends DLMSSNAS220 implements RegisterProtocol, MessageProtocol {

    private static final int	PROFILETYPE_EMETER_ONLY	    = 1;
    private static final int	PROFILETYPE_PLC_ONLY	    = 2;
    private static final int    PROFILETYPE_PQ_ONLY         = 4;

	private static final ObisCode	FW_VERSION_ACTIVE_OBISCODE	= ObisCode.fromString("1.0.0.2.0.255");
	private static final ObisCode	FW_VERSION_PASSIVE_OBISCODE	= ObisCode.fromString("1.1.0.2.0.255");

    private int iNROfIntervals = -1;

    private final EMeter eMeter = new EMeter(this);
    private final PLC plc = new PLC(this);
    private final PowerQuality powerQuality = new PowerQuality(this);
    private GMeter gMeter = new GMeter(this);
    private ObiscodeMapper ocm = null;

	private final List<SubMessageProtocol> messagingList;

    private FirmwareVersions activeFirmwareVersion;
    private FirmwareVersions passiveFirmwareVersion;

    public AS220(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor extractor) {
        super(propertySpecService);
    	messagingList = new ArrayList<>();
    	messagingList.add(new AS220Messaging(this, calendarFinder, extractor));
    	messagingList.add(new PLCMessaging(this));
    }

    public EMeter geteMeter() {
		return eMeter;
	}

    public GMeter getgMeter(){
    	return gMeter;
    }

    public void setGMeter(GMeter gMeter){
    	this.gMeter = gMeter;
    }

    public PLC getPlc() {
		return plc;
	}

    public PowerQuality getPowerQuality(){
        return powerQuality;
    }

    @Override
    public void setTime() throws IOException {
    	geteMeter().getClockController().shiftTime();
    }

    @Override
    public Date getTime() throws IOException {
        return geteMeter().getClockController().getTime();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Fri Nov 25 16:23:24 2016 +0100 $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "active_version=" + getActiveFirmwareVersion() + ", " + "passive_version=" + getPassiveFirmwareVersion();
    }

    @Override
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		RetryHandler retry = new RetryHandler();
		do {
			try {
				try {
					return getAs220ObisCodeMapper().getRegisterValue(obisCode);
				} catch (DataAccessResultException e) {
					retry.logFailure(e);
				}
			} catch (IOException e) {
				throw new IOException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
			}
		} while (retry.canRetry());
        throw new IOException("Problems while reading register with obiscode " + obisCode.toString());
	}

    @Override
	public int getNumberOfChannels() throws IOException {
		switch (getProfileType()) {

            // three separate Profiles
            case PROFILETYPE_EMETER_ONLY:
				return geteMeter().getNrOfChannels();
			case PROFILETYPE_PLC_ONLY:
				return getPlc().getNrOfChannels();
            case PROFILETYPE_PQ_ONLY:
                return getPowerQuality().getNrOfChannels();

            // combination of two
            case PROFILETYPE_EMETER_ONLY + PROFILETYPE_PLC_ONLY: //Emeter and PLC meter
                return geteMeter().getNrOfChannels() + getPlc().getNrOfChannels();
            case PROFILETYPE_EMETER_ONLY + PROFILETYPE_PQ_ONLY: // Emeter and PQ
                return geteMeter().getNrOfChannels() + getPowerQuality().getNrOfChannels();
            case PROFILETYPE_PLC_ONLY + PROFILETYPE_PQ_ONLY: // PLC and PQ
                return getPlc().getNrOfChannels() + getPowerQuality().getNrOfChannels();

            // combination of three
            case PROFILETYPE_EMETER_ONLY + PROFILETYPE_PLC_ONLY + PROFILETYPE_PQ_ONLY: // Eprofile + PLC profile + PQ profile
                return geteMeter().getNrOfChannels() + getPlc().getNrOfChannels() + getPowerQuality().getNrOfChannels();

			default:
				return 0;
		}
	}

    protected ObiscodeMapper getAs220ObisCodeMapper() {
    	if (ocm  == null) {
    		ocm = new As220ObisCodeMapper(this);
    	}
    	return ocm;
	}

	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
    	return getAs220ObisCodeMapper().getRegisterInfo(obisCode);
    }

    /**
     * Getter for the list of {@link SubMessageProtocol}
     * @return the current {@link SubMessageProtocol} object
     */
    public List<SubMessageProtocol> getMessagingList() {
		return messagingList;
	}

	/**
	 * This method requests for the NR of intervals that can be stored in the
	 * memory of the remote meter.
	 *
	 * @return NR of intervals that can be stored in the memory of the remote meter.
	 * @exception IOException
	 */
    private int getNROfIntervals() throws IOException {
        if (iNROfIntervals == -1) {
            iNROfIntervals = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getProfileEntries();
        }
        return iNROfIntervals;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.add(Calendar.MONTH,-2);
        return getProfileData(calendar.getTime(),includeEvents);
    }

    @Override
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getProfileData(lastReading, null, includeEvents);
	}

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if (to == null) {
            to = ProtocolUtils.getCalendar(getTimeZone()).getTime();
            getLogger().info("getProfileData: toDate was 'null'. Changing toDate to: " + to);
        }

        // Read the profile data, and take the limitMaxNrOfDays property in account.
        ProfileData profileData = getProfileWithLimiter(new ProfileLimiter(from, to, getLimitMaxNrOfDays()), includeEvents);

        // If there are no intervals in the profile, read the profile data again, but now with the limitMaxNrOfDays property disabled
        // This way we can prevent the profile to be stuck an a certain date if there is a gap in the profile bigger than the limitMaxNrOfDays.
        if ((profileData.getIntervalDatas().isEmpty()) && (getLimitMaxNrOfDays() > 0)) {
            profileData = getProfileWithLimiter(new ProfileLimiter(from, to, 0), includeEvents);
        }
        return profileData;

    }

    private ProfileData getProfileWithLimiter(ProfileLimiter limiter, boolean includeEvents) throws IOException {
        Date from = limiter.getFromDate();
        Date to = limiter.getToDate();

        if (validateFromToDates(from, to)) {
            return new ProfileData();
        }

        getLogger().info("Starting to read profileData [from=" + from + ", to=" + to + ", includeEvents=" + includeEvents + "]");
        ProfileData eMeterProfile;
        ProfileData plcStatistics;
        ProfileData powerQualitiesProfile;
        switch (getProfileType()) {

            // three separate Profiles
            case PROFILETYPE_EMETER_ONLY:
                return geteMeter().getProfileData(from, to, includeEvents);
            case PROFILETYPE_PLC_ONLY:
                return getPlc().getStatistics(from, to);
            case PROFILETYPE_PQ_ONLY:
                return getPowerQuality().getPowerQualities(from, to);

            // combination of two
            case PROFILETYPE_EMETER_ONLY + PROFILETYPE_PLC_ONLY: //Emeter and PLC meter
                eMeterProfile = geteMeter().getProfileData(from, to, includeEvents);
                plcStatistics = getPlc().getStatistics(from, to);
                return ProfileAppender.appendProfiles(eMeterProfile, plcStatistics);
            case PROFILETYPE_EMETER_ONLY + PROFILETYPE_PQ_ONLY: // Emeter and PQ
                eMeterProfile = geteMeter().getProfileData(from, to, includeEvents);
                powerQualitiesProfile = getPowerQuality().getPowerQualities(from, to);
                return ProfileAppender.appendProfiles(eMeterProfile, powerQualitiesProfile);
            case PROFILETYPE_PLC_ONLY + PROFILETYPE_PQ_ONLY: // PLC and PQ
                plcStatistics = getPlc().getStatistics(from, to);
                powerQualitiesProfile = getPowerQuality().getPowerQualities(from, to);
                return ProfileAppender.appendProfiles(plcStatistics, powerQualitiesProfile);

            // combination of three
            case PROFILETYPE_EMETER_ONLY + PROFILETYPE_PLC_ONLY + PROFILETYPE_PQ_ONLY: // Eprofile + PLC profile + PQ profile
                eMeterProfile = geteMeter().getProfileData(from, to, includeEvents);
                plcStatistics = getPlc().getStatistics(from, to);
                powerQualitiesProfile = getPowerQuality().getPowerQualities(from, to);
                ProfileData tempProfile = ProfileAppender.appendProfiles(eMeterProfile, plcStatistics);
                return ProfileAppender.appendProfiles(tempProfile, powerQualitiesProfile);

            default:
                getLogger().warning("Unknown value for ProfileType! [" + getProfileType() + "]");
                return new ProfileData();
        }
    }

    protected boolean validateFromToDates(Date from, Date to) {
		long diff = to.getTime() - from.getTime();
		final int minimumDiff = 1 * 60 * 1000;
		if (diff <= minimumDiff) {
            String sb = "Unable to read profile data, from date is after or to short to the to date! " +
                    "[from=" + from +
                    ", to=" + to +
                    ", diff=" + diff + " ms]";
            getLogger().warning(sb);
			return true;
		}
		return false;
	}

    @Override
	public List<MessageCategorySpec> getMessageCategories() {
		List<MessageCategorySpec> list = new ArrayList<>();
		for (SubMessageProtocol messaging : getMessagingList()) {
			list.addAll(messaging.getMessageCategories());
		}
		return list;
	}

    @Override
	public String writeMessage(Message msg) {
		for (SubMessageProtocol messaging : getMessagingList()) {
			if (messaging.canHandleMessage(msg.getSpec())) {
				return messaging.writeMessage(msg);
			}
		}
		return "";
	}

    @Override
	public String writeTag(MessageTag msgTag) {
		for (SubMessageProtocol messaging : getMessagingList()) {
			if (messaging.canHandleMessage(msgTag.getName())) {
				return messaging.writeTag(msgTag);
			}
		}
		return "";
    }

    @Override
	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}

    @Override
	public void applyMessages(List messageEntries) throws IOException {
		for (SubMessageProtocol messaging : getMessagingList()) {
			for (Iterator iterator = messageEntries.iterator(); iterator.hasNext();) {
				MessageEntry messageEntry = (MessageEntry) iterator.next();
				if (messaging.canHandleMessage(messageEntry)) {
					messaging.applyMessages(messageEntries);
				}
			}
		}
	}

    @Override
	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		for (SubMessageProtocol messaging : getMessagingList()) {
			if (messaging.canHandleMessage(messageEntry)) {
				return messaging.queryMessage(messageEntry);
			}
		}
		return MessageResult.createFailed(messageEntry);
	}

	@Override
	protected void doConnect() {
	}

    public FirmwareVersions getActiveFirmwareVersion() throws IOException {
        if (activeFirmwareVersion == null) {
            activeFirmwareVersion = new FirmwareVersions(FW_VERSION_ACTIVE_OBISCODE, this);
        }
        return activeFirmwareVersion;
    }

    public FirmwareVersions getPassiveFirmwareVersion() throws IOException {
        if (passiveFirmwareVersion == null) {
            passiveFirmwareVersion = new FirmwareVersions(FW_VERSION_PASSIVE_OBISCODE, this);
        }
        return passiveFirmwareVersion;
    }

	@Override
	public String getRegistersInfo() throws IOException {
		ExtendedLogging el = new ExtendedLogging(this);
		return el.getExtendedLogging();
	}

    @Override
    public int getProfileInterval() throws IOException {
        if (iInterval == -1) {
            switch (getProfileType()) {
                // three separate Profiles
                case PROFILETYPE_EMETER_ONLY:
                    iInterval = geteMeter().getProfileInterval();break;
                case PROFILETYPE_PLC_ONLY:
                    iInterval = getPlc().getProfileInterval();break;
                case PROFILETYPE_PQ_ONLY:
                    iInterval = getPowerQuality().getProfileInterval();break;
                default: // Default we use the profile of the Emeter
                    iInterval = geteMeter().getProfileInterval();break;
}
        }
        return iInterval;
    }

}