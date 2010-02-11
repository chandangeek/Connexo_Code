package com.energyict.protocolimpl.dlms.as220.gmeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.energyict.protocolimpl.dlms.as220.GasDevice;

public class GMeterMessaging implements MessageProtocol {

	/**
	 * Message tags
	 */
	public static final String	CONNECT_GMETER					= "ConnectGmeter";
	public static final String	DISCONNECT_GMETER				= "DisconnectGmeter";
	public static final String	ARM_GMETER						= "ArmGmeter";

	/**
	 * Message descriptions
	 */
	private static final String	CONNECT_GMETER_DISPLAY			= "Connect G-Meter Load";
	private static final String	DISCONNECT_GMETER_DISPLAY		= "Disconnect G-Meter Load";
	private static final String	ARM_GMETER_DISPLAY				= "Arm G-Meter";

	private final GasDevice gasDevice;

	public GMeterMessaging(GasDevice gasDevice) {
		this.gasDevice = gasDevice;
	}

	public GasDevice getGasDevice() {
		return gasDevice;
	}

	public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec gMeterCat = new MessageCategorySpec("G-Meter");

        gMeterCat.addMessageSpec(createMessageSpec(DISCONNECT_GMETER_DISPLAY, DISCONNECT_GMETER, false));
        gMeterCat.addMessageSpec(createMessageSpec(ARM_GMETER_DISPLAY, ARM_GMETER, false));
        gMeterCat.addMessageSpec(createMessageSpec(CONNECT_GMETER_DISPLAY, CONNECT_GMETER, false));

        theCategories.add(gMeterCat);
        return theCategories;
	}

	public void applyMessages(List messageEntries) throws IOException {
		// TODO Auto-generated method stub

	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		try {
			if (isMessageTag(DISCONNECT_GMETER, messageEntry)) {
				getGasDevice().getgMeter().getGasValveController().doDisconnect();
			} else if (isMessageTag(CONNECT_GMETER, messageEntry)) {
				getGasDevice().getgMeter().getGasValveController().doConnect();
			} else if (isMessageTag(ARM_GMETER, messageEntry)) {
				getGasDevice().getgMeter().getGasValveController().doArm();
			} else {
				getGasDevice().getLogger().severe("Received unknown message: " + messageEntry);
				return MessageResult.createFailed(messageEntry);
			}
			return MessageResult.createSuccess(messageEntry);
		} catch (IOException e) {
			getGasDevice().getLogger().severe("QueryMessage(), " + e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
	}

	public String writeMessage(Message msg) {
		return msg.write(getGasDevice());
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
