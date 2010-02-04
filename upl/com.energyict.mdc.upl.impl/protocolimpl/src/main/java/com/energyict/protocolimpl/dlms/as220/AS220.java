/**
 * @version  2.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the Siemens ZMD DLMS profile implementation
 * <BR>
 * <B>@beginchanges</B><BR>
KV|08042003|Initial version
KV|08102003|Set default of RequestTimeZone to 0
KV|10102003|generate OTHER MeterEvent when statusbit is not supported
KV|27102003|changed code for correct dst transition S->W
KV|20082004|Extended with obiscode mapping for register reading
KV|17032005|improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Improved registerreading, configuration data
KV|31032005|Handle DataContainerException
KV|15072005|applyEvents() done AFTER getting the logbook!
KV|10102006|extension to support cumulative values in load profile
KV|10102006|fix to support 64 bit values in load profile
KV|29042009|as220

 * @endchanges
 */

package com.energyict.protocolimpl.dlms.as220;


import java.io.IOException;
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

public class AS220 extends DLMSSNAS220 implements RegisterProtocol, MessageProtocol {

	private final GMeter			gMeter		= new GMeter(this);
	private final EMeter			eMeter		= new EMeter(this);
	private final MessageProtocol	messaging	= new AS220Messaging(this);
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
    		ocm = new As220ObisCodeMapper(getCosemObjectFactory());
    	}
    	return ocm;
	}

	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
    	System.out.println(obisCode.toString());
    	ObiscodeMapper ocm = new As220ObisCodeMapper(getCosemObjectFactory());
    	return ocm.getRegisterInfo(obisCode);
    }

    /**
     * Getter for the as220Messaging
     * @return the current {@link AS220Messaging} object
     */
    public MessageProtocol getMessaging() {
		return messaging;
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
