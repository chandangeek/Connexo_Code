package com.energyict.protocolimpl.dlms.as220.emeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.xml.sax.SAXException;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
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
	public static final String	FORCE_SET_CLOCK					= "ForceSetClock";

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
	public static final String	FORCE_SET_CLOCK_DISPLAY			= "Force set clock";

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
        MessageCategorySpec eMeterCat = new MessageCategorySpec("[01] E-Meter ");
        MessageCategorySpec plcMeterCat = new MessageCategorySpec("[02] PLC related");
        MessageCategorySpec otherMeterCat = new MessageCategorySpec("[03] Other");

        eMeterCat.addMessageSpec(createMessageSpec(DISCONNECT_EMETER_DISPLAY, DISCONNECT_EMETER, false));
        eMeterCat.addMessageSpec(createMessageSpec(ARM_EMETER_DISPLAY, ARM_EMETER, false));
        eMeterCat.addMessageSpec(createMessageSpec(CONNECT_EMETER_DISPLAY, CONNECT_EMETER, false));

        plcMeterCat.addMessageSpec(createMessageSpec(RESCAN_PLCBUS_DISPLAY, RESCAN_PLCBUS, false));
        plcMeterCat.addMessageSpec(createActivePLCChannelMessageSpec(SET_ACTIVE_PLC_CHANNEL_DISPLAY, SET_ACTIVE_PLC_CHANNEL, false));

        otherMeterCat.addMessageSpec(createMessageSpec(FORCE_SET_CLOCK_DISPLAY, FORCE_SET_CLOCK, false));
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
				setActivePLCChannel(messageEntry);
			} else if (isMessageTag(FORCE_SET_CLOCK, messageEntry)) {
				getAs220().geteMeter().getClockController().setTime();
			}else if (isMessageTag("firmwareUpgrade", messageEntry)) {
				upgradeFirmware(messageEntry);    
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

	/**
	 * @param messageEntry
	 * @throws IOException 
	 */
	protected void upgradeFirmware(MessageEntry messageEntry) throws IOException {
//		logger.info("Received a firmware upgrade message, using firmware message builder...");

		final FirmwareUpdateMessageBuilder builder = new FirmwareUpdateMessageBuilder();

		try {
			builder.initFromXml(messageEntry.getContent());
		} catch (final SAXException e) {
		    getAs220().getLogger().log(Level.SEVERE, "Cannot process firmware upgrade message due to an XML parsing error [" + e.getMessage() + "]", e);
		    throw new IOException(e.getMessage());
		} catch (final IOException e) {
			if (getAs220().getLogger().isLoggable(Level.SEVERE)) {
			    getAs220().getLogger().log(Level.SEVERE, "Got an IO error when loading firmware message content [" + e.getMessage() + "]", e);
			}
			 throw new IOException(e.getMessage());
		}

		// We requested an inlined file...
		if (builder.getUserFile() != null) {
		    getAs220().getLogger().info("Pulling out user file and dispatching to the device...");

			final byte[] upgradeFileData = builder.getUserFile().loadFileInByteArray();

			if (upgradeFileData.length > 0) {
				try {
					this.upgradeDevice(builder.getUserFile().loadFileInByteArray());
				} catch (final IOException e) {
				    getAs220().getLogger().log(Level.SEVERE, "Caught an IO error when trying upgrade [" + e.getMessage() + "]", e);
				    throw e;
				}
			} else {
				if (getAs220().getLogger().isLoggable(Level.WARNING)) {
				    getAs220().getLogger().log(Level.WARNING, "Length of the upgrade file is not valid [" + upgradeFileData + " bytes], failing message.");
				}

			}
		} else {
		    getAs220().getLogger().log(Level.WARNING, "The message did not contain a user file to use for the upgrade, message fails...");

		}

		getAs220().getLogger().info("Upgrade message has been processed successfully, marking message as successfully processed...");

	}
	
	/**
	 * Upgrades the remote device using the image specified.
	 *
	 * @param 	image			The new image to push to the remote device.
	 *
	 * @throws	IOException		If an IO error occurs during the upgrade.
	 */
	public final void upgradeDevice(final byte[] image) throws IOException {
	    getAs220().getLogger().info("Upgrading EpIO with new firmware image of size [" + image.length + "] bytes");

		final ImageTransfer imageTransfer = getAs220().getCosemObjectFactory().getImageTransferSN();

		try {
		    getAs220().getLogger().info("Converting received image to binary using a Base64 decoder...");

			getAs220().getLogger().info("Commencing upgrade...");

			imageTransfer.upgrade(image, false);
			imageTransfer.imageActivation();

			getAs220().getLogger().info("Upgrade has finished successfully...");
		} catch (final InterruptedException e) {
		    getAs220().getLogger().log(Level.SEVERE, "Interrupted while uploading firmware image [" + e.getMessage() + "]", e);

			final IOException ioException = new IOException(e.getMessage());
			ioException.initCause(e);

			throw ioException;
		}
	}

	private void setActivePLCChannel(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("SET_ACTIVE_PLC_CHANNEL message received");
		String messageContent = getMessageEntryContent(messageEntry);
		int channel;
		try {
			channel = Integer.valueOf(messageContent).intValue();
		} catch (NumberFormatException e) {
			throw new IOException("Invalid channel value: '" + messageContent + "'");
		}
		setActivePLCChannel(channel);
	}

	private void setActivePLCChannel(int channel) throws IOException {
		if ((channel < 0) || (channel > 6)) {
			throw new IOException("Channel can only be 0-6, but was " + channel);
		}
		getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setActiveChannel(new Unsigned8(channel));
	}

	private void rescanPLCBus() throws IOException {
		getAs220().getLogger().info("RESCAN_PLCBUS message received");
		setActivePLCChannel(0);
	}

	private String getMessageEntryContent(MessageEntry messageEntry) {
		String returnValue = "";
		if ((messageEntry != null) && (messageEntry.getContent() != null)) {
			String content = messageEntry.getContent();
			int firstPos = content.indexOf('>');
			int lastPosPos = content.lastIndexOf('<');
			if ((firstPos != -1) && (lastPosPos != -1) && ((firstPos + 1) < lastPosPos)) {
				returnValue = content.substring(firstPos + 1, lastPosPos);
			}
		}
		return returnValue;
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
		return value.getValue();
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

    private MessageSpec createActivePLCChannelMessageSpec(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);

        MessageValueSpec mvs = new MessageValueSpec();
        tagSpec.add(mvs);

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
