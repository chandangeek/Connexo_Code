package com.energyict.protocolimpl.dlms.as220.emeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.xml.sax.SAXException;

import sun.misc.BASE64Decoder;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocolimpl.base.AbstractSubMessageProtocol;
import com.energyict.protocolimpl.dlms.as220.AS220;

public class AS220Messaging extends AbstractSubMessageProtocol {

	/**
	 * Message tags
	 */
	public static final String	CONNECT_EMETER				= "ConnectEmeter";
	public static final String	DISCONNECT_EMETER			= "DisconnectEmeter";
	public static final String	ARM_EMETER					= "ArmEmeter";

	public static final String	FORCE_SET_CLOCK				= "ForceSetClock";

	public static final String 	FIRMWARE_UPDATE				= "FirmwareUpdate";


	/**
	 * Message descriptions
	 */
	private static final String	CONNECT_EMETER_DISPLAY			= "Remote connect";
	private static final String	DISCONNECT_EMETER_DISPLAY		= "Remote disconnect";
	//private static final String	ARM_EMETER_DISPLAY				= "Arm E-Meter";

	public static final String	FORCE_SET_CLOCK_DISPLAY			= "Force set clock";

	private final AS220 as220;

	public AS220Messaging(AS220 as220) {
		this.as220 = as220;
		addSupportedMessageTag(CONNECT_EMETER);
		addSupportedMessageTag(DISCONNECT_EMETER);
		addSupportedMessageTag(ARM_EMETER);
		addSupportedMessageTag(FORCE_SET_CLOCK);
		addSupportedMessageTag(FIRMWARE_UPDATE);
	}

	public AS220 getAs220() {
		return as220;
	}

	public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec eMeterCat = new MessageCategorySpec("[01] E-Meter ");
        MessageCategorySpec otherMeterCat = new MessageCategorySpec("[03] Other");

        eMeterCat.addMessageSpec(createMessageSpec(DISCONNECT_EMETER_DISPLAY, DISCONNECT_EMETER, false));
        //eMeterCat.addMessageSpec(createMessageSpec(ARM_EMETER_DISPLAY, ARM_EMETER, false));
        eMeterCat.addMessageSpec(createMessageSpec(CONNECT_EMETER_DISPLAY, CONNECT_EMETER, false));

        otherMeterCat.addMessageSpec(createMessageSpec(FORCE_SET_CLOCK_DISPLAY, FORCE_SET_CLOCK, false));

        categories.add(eMeterCat);
        categories.add(otherMeterCat);
        return categories;
	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		MessageResult result;

		try {

			if (isMessageTag(DISCONNECT_EMETER, messageEntry)) {
				getAs220().geteMeter().getContactorController().doDisconnect();
			} else if (isMessageTag(CONNECT_EMETER, messageEntry)) {
				getAs220().geteMeter().getContactorController().doConnect();
			} else if (isMessageTag(ARM_EMETER, messageEntry)) {
				getAs220().geteMeter().getContactorController().doArm();
			} else if (isMessageTag(FORCE_SET_CLOCK, messageEntry)) {
				getAs220().getLogger().info("FORCE_SET_CLOCK message received");
				getAs220().geteMeter().getClockController().setTime();
			} else if (isMessageTag(FIRMWARE_UPDATE, messageEntry)) {
				AS220ImageTransfer imageTransfer = new AS220ImageTransfer(this, messageEntry);
				imageTransfer.initiate();
				imageTransfer.upgrade();
				imageTransfer.activate();
//				upgradeFirmware(messageEntry);
			} else {
				throw new IOException("Received unknown message: " + messageEntry);
			}

			result = MessageResult.createSuccess(messageEntry);

		} catch (IOException e) {
			getAs220().getLogger().severe("QueryMessage(), FAILED: " + e.getMessage());
			result = MessageResult.createFailed(messageEntry);
		}

		return result;
	}

	/**
	 * @param messageEntry
	 * @throws IOException
	 */
	protected void upgradeFirmware(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("Received a firmware upgrade message, using firmware message builder...");
		String errorMessage = "";
		final FirmwareUpdateMessageBuilder builder = new FirmwareUpdateMessageBuilder();

		try {
		    builder.initFromXml(messageEntry.getContent());
		} catch (final IOException e) {
		    errorMessage = "Got an IO error when loading firmware message content [" + e.getMessage() + "]";
			if (getAs220().getLogger().isLoggable(Level.SEVERE)) {
			    getAs220().getLogger().log(Level.SEVERE, errorMessage, e);
			}
			 throw new IOException(errorMessage + e.getMessage());
		} catch (SAXException e) {
		    errorMessage = "Cannot process firmware upgrade message due to an XML parsing error [" + e.getMessage() + "]";
		    getAs220().getLogger().log(Level.SEVERE, errorMessage, e);
		    throw new IOException(errorMessage + e.getMessage());
		}

		// We requested an inlined file...
		if (builder.getUserFile() != null) {
			getAs220().getLogger().info("Pulling out user file and dispatching to the device...");

			final byte[] upgradeFileData = builder.getUserFile().loadFileInByteArray();

			if (upgradeFileData.length > 0) {
				try {
					this.upgradeDevice(builder.getUserFile().loadFileInByteArray());
				} catch (final IOException e) {
				    errorMessage = "Caught an IO error when trying upgrade [" + e.getMessage() + "]";
					if (getAs220().getLogger().isLoggable(Level.SEVERE)) {
					    getAs220().getLogger().log(Level.SEVERE, errorMessage, e);
					}
					throw new IOException(errorMessage);
				}
			} else {
			    errorMessage = "Length of the upgrade file is not valid [" + upgradeFileData + " bytes], failing message.";
				if (getAs220().getLogger().isLoggable(Level.WARNING)) {
				    getAs220().getLogger().log(Level.WARNING, errorMessage);
				}
				throw new IOException(errorMessage);

			}
		} else {
		    errorMessage = "The message did not contain a user file to use for the upgrade, message fails...";
		    getAs220().getLogger().log(Level.WARNING, errorMessage);

		    throw new IOException(errorMessage);
		}
	}

	/**
	 * Upgrades the remote device using the image specified.
	 *
	 * @param 	image			The new image to push to the remote device.
	 *
	 * @throws	IOException		If an IO error occurs during the upgrade.
	 */
	public final void upgradeDevice(final byte[] image) throws IOException {
	    getAs220().getLogger().info("Upgrading AM500 module with new firmware image of size [" + image.length + "] bytes");

		final ImageTransfer imageTransfer = getAs220().getCosemObjectFactory().getImageTransferSN();

		try {
			getAs220().getLogger().info("Converting received image to binary using a Base64 decoder...");

			final BASE64Decoder decoder = new BASE64Decoder();
			final byte[] binaryImage = decoder.decodeBuffer(new String(image));

			getAs220().getLogger().info("Commencing upgrade...");

			imageTransfer.upgrade(binaryImage, false);
			imageTransfer.imageActivation();

			getAs220().getLogger().info("Upgrade has finished successfully...");
		} catch (final InterruptedException e) {
		    getAs220().getLogger().log(Level.SEVERE, "Interrupted while uploading firmware image [" + e.getMessage() + "]", e);

			final IOException ioException = new IOException(e.getMessage());
			ioException.initCause(e);

			throw ioException;
		}
	}

}
