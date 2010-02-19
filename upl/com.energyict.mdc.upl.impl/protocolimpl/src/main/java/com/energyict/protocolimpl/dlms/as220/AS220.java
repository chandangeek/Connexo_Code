package com.energyict.protocolimpl.dlms.as220;


import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.cbo.BusinessException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.ObiscodeMapper;
import com.energyict.protocolimpl.dlms.as220.emeter.AS220Messaging;
import com.energyict.protocolimpl.dlms.as220.emeter.EMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.plc.PLC;

/**
 * @author kvds, jme
 *
 */
public class AS220 extends DLMSSNAS220 implements RegisterProtocol, MessageProtocol {

	private static final int	PROFILETYPE_PLC_ONLY	= 2;

	private static final int	PROFILETYPE_EMETER_ONLY	= 1;

	private static final int	PROFILETYPE_EMETER_PLC	= 0;

	private static final int SEC_PER_MIN = 60;

	private static final ObisCode	FW_VERSION_ACTIVE_OBISCODE	= ObisCode.fromString("1.0.0.2.0.255");
	private static final ObisCode	FW_VERSION_PASSIVE_OBISCODE	= ObisCode.fromString("1.1.0.2.0.255");

	private int iNROfIntervals=-1;

	private final EMeter			eMeter			= new EMeter(this);
	private final GMeter			gMeter		= new GMeter(this);
	private final MessageProtocol	messaging		= new AS220Messaging(this);
	private final PLC				plc				= new PLC(this);

	private ObiscodeMapper			ocm		= null;

    /**
     * Create a new instance of the {@link AS220} dlms protocol
     */
    public AS220() {

    }

    /**
     * Getter for the E-meter
     * @return
     */
    public EMeter geteMeter() {
		return eMeter;
	}

    /**
     * Getter for the G-Meter
     * @return
     */
    public GMeter getgMeter(){
    	return gMeter;
    }

    public PLC getPlc() {
		return plc;
	}

    public void setTime() throws IOException {
    	geteMeter().getClockController().shiftTime();
    }

    public Date getTime() throws IOException {
        return geteMeter().getClockController().getTime();
    }

    public String getProtocolVersion() {
		String rev = "$Revision: 33703 $" + " - " + "$Date: 2009-06-02 17:34:52 +0200 (di, 02 jun 2009) $";
		String manipulated = "Revision " + rev.substring(rev.indexOf("$Revision: ") + "$Revision: ".length(), rev.indexOf("$ -")) + "at "
				+ rev.substring(rev.indexOf("$Date: ") + "$Date: ".length(), rev.indexOf("$Date: ") + "$Date: ".length() + 19);
    	return manipulated;
    }

    public String getFirmwareVersion() throws IOException,UnsupportedException {
    	StringBuilder sb = new StringBuilder();
    	sb.append("active_version=").append(new FirmwareVersions(FW_VERSION_ACTIVE_OBISCODE, this)).append(", ");
    	sb.append("passive_version=").append(new FirmwareVersions(FW_VERSION_PASSIVE_OBISCODE, this));
        return sb.toString();
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
			return getAs220ObisCodeMapper().getRegisterValue(obisCode);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
		}
    }

	public int getNumberOfChannels() throws IOException {
		switch (getProfileType()) {
			case PROFILETYPE_EMETER_PLC:
				return geteMeter().getNrOfChannels() + getPlc().getNrOfChannels();
			case PROFILETYPE_EMETER_ONLY:
				return geteMeter().getNrOfChannels();
			case PROFILETYPE_PLC_ONLY:
				return getPlc().getNrOfChannels();
			default:
				return 0;
		}
	}

    private ObiscodeMapper getAs220ObisCodeMapper() {
    	if (ocm  == null) {
    		ocm = new As220ObisCodeMapper(this);
    	}
    	return ocm;
	}

	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
    	return getAs220ObisCodeMapper().getRegisterInfo(obisCode);
    }

    /**
     * Getter for the as220Messaging
     * @return the current {@link AS220Messaging} object
     */
    public MessageProtocol getMessaging() {
		return messaging;
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

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		Calendar fromCalendar = ProtocolUtils.getCalendar(getTimeZone());
		fromCalendar.add(Calendar.MINUTE, (-1) * getNROfIntervals() * (getProfileInterval() / SEC_PER_MIN));
		return getProfileData(fromCalendar.getTime(), includeEvents);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getProfileData(lastReading, new Date(), includeEvents);
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {

		ProfileData eMeterProfile;
		ProfileData plcStatistics;

		switch (getProfileType()) {
			case PROFILETYPE_EMETER_PLC:
				eMeterProfile = geteMeter().getProfileData(from, to, includeEvents);
				plcStatistics = getPlc().getStatistics(from, to);
				return ProfileAppender.appendProfiles(eMeterProfile, plcStatistics);
			case PROFILETYPE_EMETER_ONLY:
				eMeterProfile = geteMeter().getProfileData(from, to, includeEvents);
				return eMeterProfile;
			case PROFILETYPE_PLC_ONLY:
				plcStatistics = getPlc().getStatistics(from, to);
				return plcStatistics;
			default : return new ProfileData();
		}

	}

	public List<MessageCategorySpec> getMessageCategories() {
		return getMessaging().getMessageCategories();
	}

	public String writeMessage(Message msg) {
		return getMessaging().writeMessage(msg);
	}

	public String writeTag(MessageTag msgTag) {
        return getMessaging().writeTag(msgTag);
    }

	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}

	public void applyMessages(List messageEntries) throws IOException {
		getMessaging().applyMessages(messageEntries);
	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		return getMessaging().queryMessage(messageEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doConnect() throws BusinessException {

	}

}
