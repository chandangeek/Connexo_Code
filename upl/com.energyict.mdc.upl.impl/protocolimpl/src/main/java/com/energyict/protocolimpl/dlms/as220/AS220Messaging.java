package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.dlms.axrdencoding.TypeEnum;
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

public class AS220Messaging implements MessageProtocol {

	private static final String			CONNECT									= "ConnectLoad";
	private static final String			DISCONNECT								= "DisconnectLoad";
	private static final String			ARM										= "ArmMeter";
	private static final String			TARIFF_OPTION_SWITCH_BASE				= "TariffOptionSwitchBase";
	private static final String			TARIFF_OPTION_SWITCH_DAYNIGHT			= "TariffOptionSwitchDayNight";

	private static final String			CONNECT_DISPLAY							= "Connect Load";
	private static final String			DISCONNECT_DISPLAY						= "Disconnect Load";
	private static final String			ARM_DISPLAY								= "Arm Meter";
	private static final String			TARIFF_OPTION_SWITCH_BASE_DISPLAY		= "Switch tariff option BASE";
	private static final String			TARIFF_OPTION_SWITCH_DAYNIGHT_DISPLAY	= "Switch tariff option DAY/NIGHT";


	private final AS220 as220;

	public AS220Messaging(AS220 as220) {
		this.as220 = as220;
	}

	public AS220 getAs220() {
		return as220;
	}

	public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");

        MessageSpec msgSpec = addBasicMsg(DISCONNECT_DISPLAY, DISCONNECT, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(ARM_DISPLAY, ARM, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(CONNECT_DISPLAY, CONNECT, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(TARIFF_OPTION_SWITCH_BASE_DISPLAY, TARIFF_OPTION_SWITCH_BASE, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(TARIFF_OPTION_SWITCH_DAYNIGHT_DISPLAY, TARIFF_OPTION_SWITCH_DAYNIGHT, false);
        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;
	}

	public void applyMessages(List messageEntries) throws IOException {
		// TODO Auto-generated method stub

	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		try {
			if (messageEntry.getContent().indexOf("<" + DISCONNECT) >= 0) {
				getAs220().getContactorController().doDisconnect();
			} else if (messageEntry.getContent().indexOf("<" + CONNECT) >= 0) {
				getAs220().getContactorController().doConnect();
			} else if (messageEntry.getContent().indexOf("<" + ARM) >= 0) {
				getAs220().getContactorController().doArm();
			} else if (messageEntry.getContent().indexOf("<" + TARIFF_OPTION_SWITCH_BASE) >= 0) {
				getAs220().getLogger().info("TARIFF_OPTION_SWITCH_BASE message received");
				getAs220().getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.0.255")).setValueAttr(new TypeEnum(0));
			} else if (messageEntry.getContent().indexOf("<" + TARIFF_OPTION_SWITCH_DAYNIGHT) >= 0) {
				getAs220().getLogger().info("TARIFF_OPTION_SWITCH_DAYNIGHT message received");
				getAs220().getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.0.255")).setValueAttr(new TypeEnum(1));
			}
			return MessageResult.createSuccess(messageEntry);
		} catch (IOException e) {
			getAs220().getLogger().severe("QueryMessage(), " + e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
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

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
