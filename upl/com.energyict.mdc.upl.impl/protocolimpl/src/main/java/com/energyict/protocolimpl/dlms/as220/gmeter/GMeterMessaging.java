package com.energyict.protocolimpl.dlms.as220.gmeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

public class GMeterMessaging implements MessageProtocol {

	/*
	 * Message tags
	 */
	public static final String	CONNECT_GMETER					= "ConnectGmeter";
	public static final String	DISCONNECT_GMETER				= "DisconnectGmeter";
	public static final String	ARM_GMETER						= "ArmGmeter";
	public static final String 	COMMISSION						= "Commission";
	public static final String 	DECOMISSION						= "Decommission";
	public static final String  ENABLE_ENCRYPTION				= "EnableEncryption";

	/*
	 * Message descriptions
	 */
	private static final String	CONNECT_GMETER_DISPLAY			= "Connect G-Meter Load";
	private static final String	DISCONNECT_GMETER_DISPLAY		= "Disconnect G-Meter Load";
	private static final String	ARM_GMETER_DISPLAY				= "Arm G-Meter";
	private static final String COMMISSION_DISPLAY				= "Commission meter";
	private static final String DECOMMISSION_DISPLAY			= "Decommission meter";
	private static final String ENABLE_ENCRYPTION_DISPLAY		= "Enable encryption";

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
        gMeterCat.addMessageSpec(createMessageSpec(COMMISSION_DISPLAY, COMMISSION, false));
        gMeterCat.addMessageSpec(createMessageSpec(DECOMMISSION_DISPLAY, DECOMISSION, true));
        gMeterCat.addMessageSpec(createEncryptionMessageSpec(ENABLE_ENCRYPTION_DISPLAY, ENABLE_ENCRYPTION, false));

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
			} else if (isMessageTag(COMMISSION, messageEntry)){
				getGasDevice().getgMeter().getGasInstallController().install();
			} else if (isMessageTag(DECOMISSION, messageEntry)){
				getGasDevice().getgMeter().getGasInstallController().deinstall();
			} else if (isMessageTag(ENABLE_ENCRYPTION, messageEntry)){
				enableEncryption(messageEntry);
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
     * Generate a {@link MessageSpec} for the EncryptionMessage, that can be added to the list of supported messages
     * 
     * @param keyId 
     * 				- the ID of the message
     * @param tagName 
     * 				- the tag of the message
     * @param advanced 
     * 				- indicate whether the message is visible only if the 'advanced' checkbox is checked
     * 
     * @return a generated MessageSpec for the encryption message
     */
    private MessageSpec createEncryptionMessageSpec(String keyId, String tagName, boolean advanced){
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.MBUS_OPEN_KEY, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.MBUS_TRANSFER_KEY, false);
		tagSpec.add(msgAttrSpec);
		tagSpec.add(msgVal);
		msgSpec.add(tagSpec);
		return msgSpec;
    }
    
    /**
     * Functionality to enable the encryption over the P2 port
     * 
     * @param messageEntry 
     * 					- the messageContent from EIServer
     * 
     * @throws IOException if something went wrong during setting of one of the keys
     */
    private void enableEncryption(MessageEntry messageEntry) throws IOException{
    	Structure rawData = new Structure();
    	rawData.addDataType(new OctetString(DLMSUtils.hexStringToByteArray(getMessageValue(messageEntry.getContent(), RtuMessageConstant.MBUS_OPEN_KEY))));
    	rawData.addDataType(new OctetString(DLMSUtils.hexStringToByteArray(getMessageValue(messageEntry.getContent(), RtuMessageConstant.MBUS_TRANSFER_KEY))));
    	getGasDevice().getgMeter().getGasInstallController().setBothKeysAtOnce(rawData.getBEREncodedByteArray());
    }
    
    /**
     * Get a value from the messageContent
     * 
     * @param elementTag 
     * 					- the startingTag
     * @return the value
     */
    private String getMessageValue(String content, String elementTag){
    	int startIndex = content.indexOf(elementTag) + + elementTag.length() + 1;
    	int endIndex = content.indexOf(elementTag, startIndex) -2;
    	return content.substring(startIndex, endIndex);
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
