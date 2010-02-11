package com.energyict.protocolimpl.dlms.as220.emeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.MacAddress;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.dlms.as220.AS220;

public class AS220Messaging implements MessageProtocol {

	/**
	 * Message tags
	 */
	public static final String	CONNECT_EMETER					= "ConnectEmeter";
	public static final String	DISCONNECT_EMETER				= "DisconnectEmeter";
	public static final String	ARM_EMETER						= "ArmEmeter";

	public static final String	TOPT_SWITCH_BASE				= "TariffOptionSwitchBase";
	public static final String	TOPT_SWITCH_DAYNIGHT			= "TariffOptionSwitchDayNight";

	public static final String	RESCAN_PLCBUS					= "RescanPlcBus";
	public static final String	SET_ACTIVE_PLC_CHANNEL			= "SetActivePlcChannel";

	/**
	 * Message descriptions
	 */
	private static final String	CONNECT_EMETER_DISPLAY			= "Connect E-Meter Load";
	private static final String	DISCONNECT_EMETER_DISPLAY		= "Disconnect E-Meter Load";
	private static final String	ARM_EMETER_DISPLAY				= "Arm E-Meter";

	private static final String	TOPT_SWITCH_BASE_DISPLAY		= "Switch tariff option BASE";
	private static final String	TOPT_SWITCH_DAYNIGHT_DISPLAY	= "Switch tariff option DAY/NIGHT";

	private static final String	RESCAN_PLCBUS_DISPLAY			= "Force manual rescan PLC bus";
	private static final String	SET_ACTIVE_PLC_CHANNEL_DISPLAY	= "Set active PLC channel";

	private final AS220 as220;

	public AS220Messaging(AS220 as220) {
		this.as220 = as220;
	}

	public AS220 getAs220() {
		return as220;
	}

	public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec eMeterCat = new MessageCategorySpec("E-Meter ");
        MessageCategorySpec plcMeterCat = new MessageCategorySpec("PLC related");
        MessageCategorySpec otherMeterCat = new MessageCategorySpec("zzz_Other");

        eMeterCat.addMessageSpec(createMessageSpec(DISCONNECT_EMETER_DISPLAY, DISCONNECT_EMETER, false));
        eMeterCat.addMessageSpec(createMessageSpec(ARM_EMETER_DISPLAY, ARM_EMETER, false));
        eMeterCat.addMessageSpec(createMessageSpec(CONNECT_EMETER_DISPLAY, CONNECT_EMETER, false));

        plcMeterCat.addMessageSpec(createMessageSpec(RESCAN_PLCBUS_DISPLAY, RESCAN_PLCBUS, false));
        plcMeterCat.addMessageSpec(createMessageSpec(SET_ACTIVE_PLC_CHANNEL_DISPLAY, SET_ACTIVE_PLC_CHANNEL, false));

        otherMeterCat.addMessageSpec(createMessageSpec(TOPT_SWITCH_BASE_DISPLAY, TOPT_SWITCH_BASE, false));
        otherMeterCat.addMessageSpec(createMessageSpec(TOPT_SWITCH_DAYNIGHT_DISPLAY, TOPT_SWITCH_DAYNIGHT, false));

        theCategories.add(eMeterCat);
        theCategories.add(plcMeterCat);
        theCategories.add(otherMeterCat);
        return theCategories;
	}

	public void applyMessages(List messageEntries) throws IOException {
		// TODO Auto-generated method stub

	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		try {
			if (isMessageTag(DISCONNECT_EMETER, messageEntry)) {
				getAs220().geteMeter().getContactorController().doDisconnect();
			} else if (isMessageTag(CONNECT_EMETER, messageEntry)) {
				getAs220().geteMeter().getContactorController().doConnect();
			} else if (isMessageTag(ARM_EMETER, messageEntry)) {
				getAs220().geteMeter().getContactorController().doArm();
			} else if (isMessageTag(TOPT_SWITCH_BASE, messageEntry)) {
				getAs220().getLogger().info("TARIFF_OPTION_SWITCH_BASE message received");
				getAs220().getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.0.255")).setValueAttr(new TypeEnum(0));
			} else if (isMessageTag(TOPT_SWITCH_DAYNIGHT, messageEntry)) {
				getAs220().getLogger().info("TARIFF_OPTION_SWITCH_DAYNIGHT message received");
				getAs220().getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.0.255")).setValueAttr(new TypeEnum(1));
			} else if (isMessageTag(RESCAN_PLCBUS, messageEntry)) {
				rescanPLCBus();
			} else if (isMessageTag(SET_ACTIVE_PLC_CHANNEL, messageEntry)) {
				setActivePLCChannel(0);
			} else {
				getAs220().getLogger().severe("Received unknown message: " + messageEntry);
				return MessageResult.createFailed(messageEntry);
			}
			return MessageResult.createSuccess(messageEntry);
		} catch (IOException e) {
			getAs220().getLogger().severe("QueryMessage(), " + e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
	}

	private void setActivePLCChannel(int channelNr) throws IOException {
		getAs220().getLogger().info("SET_ACTIVE_PLC_CHANNEL message received");
		getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setActiveChannel(new Unsigned8(channelNr));
	}

	private void rescanPLCBus() throws IOException {
		getAs220().getLogger().info("RESCAN_PLCBUS message received");
		getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setActiveChannel(new Unsigned8(0));
		getAs220().getCosemObjectFactory().getSFSKActiveInitiator().doResetNewNotSynchronized(new MacAddress(0));
	}

	public String writeMessage(Message msg) {
		return msg.write(getAs220());
	}

	public String writeTag(MessageTag tag) {
    	StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append( tag.getName() );

        // b. Attributes
        for (Iterator<MessageAttribute> it = tag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = it.next();
            if ((att.getValue()==null) || (att.getValue().length()==0)) {
				continue;
			}
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Iterator<MessageElement> it = tag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = it.next();
            if (elt.isTag()) {
				buf.append( writeTag((MessageTag)elt) );
			} else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if ((value==null) || (value.length()==0)) {
					return "";
				}
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append( tag.getName() );
        buf.append(">");

        return buf.toString();
	}

	public String writeValue(MessageValue value) {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Generate a {@link MessageSpec}, that can be added to the list of supported messages
     * @param keyId
     * @param tagName
     * @param advanced
     * @return
     */
    private MessageSpec createMessageSpec(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

	/**
	 * @param tag
	 * @param messageEntry
	 * @return
	 */
	private boolean isMessageTag(String tag, MessageEntry messageEntry) {
		return (messageEntry.getContent().indexOf("<" + tag) >= 0);
	}

}
