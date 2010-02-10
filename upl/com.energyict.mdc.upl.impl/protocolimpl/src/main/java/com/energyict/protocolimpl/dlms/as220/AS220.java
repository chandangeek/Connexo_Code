package com.energyict.protocolimpl.dlms.as220;


import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
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
import com.energyict.protocolimpl.dlms.as220.emeter.EMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.plc.PLC;

/**
 * @author kvds, jme
 *
 */
public class AS220 extends DLMSSNAS220 implements RegisterProtocol, MessageProtocol {

	private static final int SEC_PER_MIN = 60;

	private int iNROfIntervals=-1;

	private final GMeter			gMeter		= new GMeter(this);
	private final EMeter			eMeter		= new EMeter(this);
	private final MessageProtocol	messaging	= new AS220Messaging(this);
	private final PLC				plc			= new PLC(this);

	private ObiscodeMapper			ocm			= null;

    /**
     * Create a new instance of the {@link AS220} dlms protocol
     */
    public AS220() {

    }

    public GMeter getgMeter() {
		return gMeter;
	}

    public EMeter geteMeter() {
		return eMeter;
	}

    public PLC getPlc() {
		return plc;
	}
    
    public void setTime() throws IOException {
    	geteMeter().getClockController().setTime();
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
        StringBuffer strBuff = new StringBuffer();
    	UniversalObject uo = getMeterConfig().getVersionObject();
        byte[] responsedata = getCosemObjectFactory().getGenericRead(uo.getBaseName(),uo.getValueAttributeOffset()).getResponseData();

        Array array = AXDRDecoder.decode(responsedata).getArray();
        Structure structure = array.getDataType(0).getStructure();
        strBuff.append(ProtocolUtils.outputHexString(structure.getNextDataType().getOctetString().getOctetStr()));
        strBuff.append(", "+structure.getNextDataType().intValue());
        strBuff.append(", "+structure.getNextDataType().intValue());
        strBuff.append(", "+structure.getNextDataType().intValue());
        strBuff.append(", "+structure.getNextDataType().longValue());
        return strBuff.toString();
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
			return getAs220ObisCodeMapper().getRegisterValue(obisCode);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
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
		ProfileData eMeterProfile = geteMeter().getProfileData(from, to, includeEvents);
		return eMeterProfile;
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

}
