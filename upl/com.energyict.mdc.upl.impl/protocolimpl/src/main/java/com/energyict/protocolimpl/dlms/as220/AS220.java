package com.energyict.protocolimpl.dlms.as220;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.DataAccessResultException.DataAccessResultCode;
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
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.ObiscodeMapper;
import com.energyict.protocolimpl.base.RetryHandler;
import com.energyict.protocolimpl.base.SubMessageProtocol;
import com.energyict.protocolimpl.dlms.as220.emeter.AS220Messaging;
import com.energyict.protocolimpl.dlms.as220.emeter.EMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.plc.PLC;
import com.energyict.protocolimpl.dlms.as220.plc.PLCMessaging;

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

	private final EMeter 	eMeter	= new EMeter(this);
	private final PLC 		plc		= new PLC(this);
	private GMeter 			gMeter	= new GMeter(this);
	private ObiscodeMapper	ocm		= null;

	private final List<SubMessageProtocol> messagingList;

    /**
     * Create a new instance of the {@link AS220} dlms protocol
     */
    public AS220() {
    	messagingList = new ArrayList<SubMessageProtocol>();
    	messagingList.add(new AS220Messaging(this));
    	messagingList.add(new PLCMessaging(this));
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

    /**
     * Setter for the gMeter;
     * @param gMeter
     */
    public void setGMeter(GMeter gMeter){
    	this.gMeter = gMeter;
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
		String rev = "$Revision: 33703 $" + " - " + "$Date$";
		String manipulated = "Revision " + rev.substring(rev.indexOf("$Revision: ") + "$Revision: ".length(), rev.indexOf("$ -")) + "at "
				+ rev.substring(rev.indexOf("$Date: ") + "$Date: ".length(), rev.indexOf("$Date: ") + "$Date: ".length() + 19);
    	return manipulated;
    }

    public String getFirmwareVersion() throws IOException {
    	StringBuilder sb = new StringBuilder();
    	sb.append("active_version=").append(new FirmwareVersions(FW_VERSION_ACTIVE_OBISCODE, this)).append(", ");
    	sb.append("passive_version=").append(new FirmwareVersions(FW_VERSION_PASSIVE_OBISCODE, this));
        return sb.toString();
    }

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		RetryHandler retry = new RetryHandler();
		do {
			try {
				try {
					return getAs220ObisCodeMapper().getRegisterValue(obisCode);
				} catch (DataAccessResultException e) {
					if (e.getCode().equals(DataAccessResultCode.TEMPORARY_FAILURE)) {
						retry.logFailure(e);
					}
				}
			} catch (IOException e) {
				throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
			}
		} while (true);
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

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		return getProfileData(null, includeEvents);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getProfileData(lastReading, null, includeEvents);
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {

		if (from == null) {
			Calendar fromCalendar = ProtocolUtils.getCalendar(getTimeZone());
			fromCalendar.add(Calendar.MINUTE, (-1) * getNROfIntervals() * (getProfileInterval() / SEC_PER_MIN));
			from = fromCalendar.getTime();
		}

		if (to == null) {
			to = ProtocolUtils.getCalendar(getTimeZone()).getTime();
		}

		if(validateFromToDates(from, to)) {
			return new ProfileData();
		}

		System.out.println(from.getTime());
		System.out.println(to.getTime());


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

	/**
	 * @param from
	 * @param to
	 */
	private boolean validateFromToDates(Date from, Date to) {
		long diff = to.getTime() - from.getTime();
		final int minimumDiff = 1 * 60 * 1000;
		if (diff <= minimumDiff) {
			StringBuffer sb = new StringBuffer();
			sb.append("Unable to read profile data, from date is after or to short to the to date! ");
			sb.append("[from=").append(from);
			sb.append(", to=").append(to);
			sb.append(", diff=").append(diff).append(" ms]");
			getLogger().warning(sb.toString());
			return true;
		}
		return false;
	}

	public List<MessageCategorySpec> getMessageCategories() {
		List<MessageCategorySpec> list = new ArrayList<MessageCategorySpec>();
		for (SubMessageProtocol messaging : getMessagingList()) {
			list.addAll(messaging.getMessageCategories());
		}
		return list;
	}

	public String writeMessage(Message msg) {
		for (SubMessageProtocol messaging : getMessagingList()) {
			if (messaging.canHandleMessage(msg.getSpec())) {
				return messaging.writeMessage(msg);
			}
		}
		return "";
	}

	public String writeTag(MessageTag msgTag) {
		for (SubMessageProtocol messaging : getMessagingList()) {
			if (messaging.canHandleMessage(msgTag.getName())) {
				return messaging.writeTag(msgTag);
			}
		}
		return "";
    }

	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}

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

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		for (SubMessageProtocol messaging : getMessagingList()) {
			if (messaging.canHandleMessage(messageEntry)) {
				return messaging.queryMessage(messageEntry);
			}
		}
		return MessageResult.createFailed(messageEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doConnect() throws BusinessException {

	}

	@Override
	public String getRegistersInfo() throws IOException {
		ExtendedLogging el = new ExtendedLogging(this);
		return el.getExtendedLogging();
	}

}
