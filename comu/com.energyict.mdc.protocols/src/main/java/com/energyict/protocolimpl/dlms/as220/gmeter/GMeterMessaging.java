package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.dlms.as220.GasDevice;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GMeterMessaging implements MessageProtocol {

	/*
	 * Message tags
	 */
	public static final String	CONNECT_GMETER					= "ConnectGmeter";
	public static final String	DISCONNECT_GMETER				= "DisconnectGmeter";
	public static final String 	DECOMISSION						= "Decommission";
	public static final String  ENABLE_ENCRYPTION				= "EnableEncryption";
    public static final String 	DUMMY_MESSAGE				    = "DummyMessage";
    public static final String WRITE_CAPTURE_DEFINITION = "WriteCaptureDefinition";

	/*
	 * Message descriptions
	 */
	private static final String	CONNECT_GMETER_DISPLAY			= "Connect G-Meter Load";
	private static final String	DISCONNECT_GMETER_DISPLAY		= "Disconnect G-Meter Load";
	private static final String DECOMMISSION_DISPLAY			= "Decommission meter";
	private static final String ENABLE_ENCRYPTION_DISPLAY		= "Enable encryption";
    private static final String	DUMMY_MESSAGE_DISPLAY		    = "Dummy message";
    private static final String WRITE_CAPTURE_DEF_DISPLAY = "Configure capture_definition of the MBus client";

	private final GasDevice gasDevice;
    protected static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");

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
        gMeterCat.addMessageSpec(createMessageSpec(CONNECT_GMETER_DISPLAY, CONNECT_GMETER, false));
        gMeterCat.addMessageSpec(createMessageSpec(DECOMMISSION_DISPLAY, DECOMISSION, true));
        gMeterCat.addMessageSpec(createMessageSpec(DUMMY_MESSAGE_DISPLAY, DUMMY_MESSAGE, false));
        gMeterCat.addMessageSpec(createEncryptionMessageSpec(ENABLE_ENCRYPTION_DISPLAY, ENABLE_ENCRYPTION, false));
        gMeterCat.addMessageSpec(addBasicMsgWithAttributes(WRITE_CAPTURE_DEF_DISPLAY, WRITE_CAPTURE_DEFINITION, true, "DIB", "VIB"));

        theCategories.add(gMeterCat);
        return theCategories;
	}

    protected MessageSpec addBasicMsgWithAttributes(final String keyId, final String tagName, final boolean advanced, String... attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            if (attribute.equals("VIB")) {
                MessageAttributeSpec attributeSpec = new MessageAttributeSpec(attribute, false);
                attributeSpec.setValue("");
                tagSpec.add(attributeSpec);
            } else {
                tagSpec.add(new MessageAttributeSpec(attribute, true));
            }
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
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
			} else if (isMessageTag(DECOMISSION, messageEntry)){
				getGasDevice().getgMeter().getGasInstallController().deinstall();
            } else if (isMessageTag(ENABLE_ENCRYPTION, messageEntry)){
                enableEncryption(messageEntry);
            } else if (isMessageTag(WRITE_CAPTURE_DEFINITION, messageEntry)) {
                writeCaptureDefinition(messageEntry);
            } else if (isMessageTag(DUMMY_MESSAGE, messageEntry)){
                getGasDevice().getLogger().info("DUMMY_MESSAGE message received");
			} else {
				getGasDevice().getLogger().severe("Received unknown message: " + messageEntry);
				return MessageResult.createFailed(messageEntry);
			}
			return MessageResult.createSuccess(messageEntry);
        } catch (NumberFormatException e) {
            getGasDevice().getLogger().severe("Error parsing message argument(s): " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        } catch (StringIndexOutOfBoundsException e) {
            getGasDevice().getLogger().severe("Error parsing message argument(s): " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
		} catch (IOException e) {
			getGasDevice().getLogger().severe("QueryMessage(), " + e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
	}

	public String writeMessage(Message msg) {
		return msg.write(this);
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
		return value.getValue();
	}

    /**
     * Generate a {@link MessageSpec}, that can be added to the list of supported messages
     *
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
     * @param keyId    - the ID of the message
     * @param tagName  - the tag of the message
     * @param advanced - indicate whether the message is visible only if the 'advanced' checkbox is checked
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
	 * Functionality to enable the encryption over the P2 port.<br>
	 * <b>Note:</b><br>
	 * The AM500 module needs a specific structure of keys.<br>
	 * <blockquote> <code>
	 * both_keys ::= {@link Structure}			<br>
	 * 		{ 									<br>
	 * 		Open_Key 	:  {@link OctetString}, <br>
	 * 		Transfer_Key:  {@link OctetString}	<br>
	 * 	}
     * </code> </blockquote>
	 *
     * @param messageEntry - the messageContent from EIServer
	 *
	 * @throws IOException
	 *             if something went wrong during setting of one of the keys
	 */
    private void enableEncryption(MessageEntry messageEntry) throws IOException{
    	Structure rawData = new Structure();
    	rawData.addDataType(OctetString.fromByteArray(DLMSUtils.hexStringToByteArray(getMessageValue(messageEntry.getContent(), RtuMessageConstant.MBUS_OPEN_KEY))));
    	rawData.addDataType(OctetString.fromByteArray(DLMSUtils.hexStringToByteArray(getMessageValue(messageEntry.getContent(), RtuMessageConstant.MBUS_TRANSFER_KEY))));
    	getGasDevice().getgMeter().getGasInstallController().setBothKeysAtOnce(rawData.getBEREncodedByteArray());
    }

    private void writeCaptureDefinition(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        byte[] dib1Bytes = ProtocolTools.getBytesFromHexString(parts[1].substring(1).split("\"")[0], "$");
        OctetString dib1 = OctetString.fromByteArray(dib1Bytes, dib1Bytes.length);
        OctetString vib1 = OctetString.fromByteArray(new byte[0], 0);

        Structure element1 = new Structure();
        element1.addDataType(dib1);
        element1.addDataType(vib1);

        Array capture_definition = new Array();
        capture_definition.addDataType(element1);
        getGasDevice().getgMeter().getGasInstallController().writeCaptureDefinition(capture_definition);
    }

    /**
     * Get a value from the messageContent
     *
     * @param elementTag - the startingTag
     * @return the value
     */
    protected String getMessageValue(String content, String elementTag){
    	int startIndex = content.indexOf(elementTag) + elementTag.length() + 2;
    	int endIndex = content.indexOf("\"", startIndex);
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
