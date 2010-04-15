package com.energyict.protocolimpl.dlms.as220.plc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.ElectricalPhase;
import com.energyict.dlms.cosem.attributeobjects.Frequencies;
import com.energyict.dlms.cosem.attributeobjects.MacAddress;
import com.energyict.dlms.cosem.attributeobjects.Repeater;
import com.energyict.dlms.cosem.attributes.SFSKIec61334LLCSetupAttribute;
import com.energyict.dlms.cosem.attributes.SFSKPhyMacSetupAttribute;
import com.energyict.dlms.cosem.attributes.SFSKSyncTimeoutsAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.base.AbstractSubMessageProtocol;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.utils.MessagingTools;

/**
 * @author jme
 *
 */
public class PLCMessaging extends AbstractSubMessageProtocol {

	private static final int		FREQUENCIES_PER_PAIR				= 2;
	private static final int		NR_OF_CHANNELS						= 6;

	public static final String		RESCAN_PLCBUS						= "RescanPlcBus";
	public static final String		SET_ACTIVE_PLC_CHANNEL				= "SetActivePlcChannel";
	public static final String		SET_PLC_CHANNEL_FREQUENCIES			= "SetPlcChannelFrequencies";
	public static final String		SET_SINGLE_PLC_CHANNEL_FREQUENCY	= "SetSinglePlcChannelFrequency";
	public static final String		SET_SFSK_MAC_TIMEOUTS				= "SetSFSKMacTimeouts";
	public static final String		SET_SFSK_INITIATOR_PHASE			= "SetSFSKInitiatorPhase";
	public static final String		SET_SFSK_GAIN						= "SetSFSKGain";
	public static final String		SET_SFSK_REPEATER					= "SetSFSKRepeater";
	public static final String		SET_SFSK_MAX_FRAME_LENGTH			= "SetSFSKMaxFrameLength";

	private static final String		RESCAN_PLCBUS_DISPLAY				= "Force manual rescan PLC bus";
	private static final String		SET_ACTIVE_PLC_CHANNEL_DISPLAY		= "Set the S-FSK active channel";
	private static final String		SET_PLC_FREQUENCIES_DISPLAY			= "Set the S-FSK channels frequencies";
	private static final String		SET_SINGLE_PLC_FREQUENCY_DISPLAY	= "Set the S-FSK single channel frequency (Only for demo or debug)";
	private static final String		SET_SFSK_MAC_TIMEOUTS_DISPLAY		= "Set the S-FSK Mac timeouts";
	private static final String		SET_SFSK_INITIATOR_PH_DISPLAY		= "Set the S-FSK initiator phase";
	private static final String		SET_SFSK_GAIN_DISPLAY				= "Set the S-FSK gain properties";
	private static final String		SET_SFSK_REPEATER_DISPLAY			= "Set the S-FSK repeater property";
	private static final String		SET_SFSK_MAX_FRAME_LENGTH_DISPLAY	= "Set the S-FSK maximum frame length property";

	private static final String[][]	FREQUENCIES_NAME = new String[][] {
		{"CHANNEL1_FM", "CHANNEL1_FS"},
		{"CHANNEL2_FM", "CHANNEL2_FS"},
		{"CHANNEL3_FM", "CHANNEL3_FS"},
		{"CHANNEL4_FM", "CHANNEL4_FS"},
		{"CHANNEL5_FM", "CHANNEL5_FS"},
		{"CHANNEL6_FM", "CHANNEL6_FS"}
	};

	private final AS220 as220;

	/**
	 * @param as220
	 */
	public PLCMessaging(AS220 as220) {
		this.as220 = as220;
		addSupportedMessageTag(RESCAN_PLCBUS);
		addSupportedMessageTag(SET_ACTIVE_PLC_CHANNEL);
		addSupportedMessageTag(SET_SFSK_MAC_TIMEOUTS);
		addSupportedMessageTag(SET_PLC_CHANNEL_FREQUENCIES);
		addSupportedMessageTag(SET_SINGLE_PLC_CHANNEL_FREQUENCY);
		addSupportedMessageTag(SET_SFSK_INITIATOR_PHASE);
		addSupportedMessageTag(SET_SFSK_GAIN);
		addSupportedMessageTag(SET_SFSK_REPEATER);
		addSupportedMessageTag(SET_SFSK_MAX_FRAME_LENGTH);
	}

	/**
	 * @return
	 */
	public AS220 getAs220() {
		return as220;
	}

	public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec plcMeterCat = new MessageCategorySpec("[02] PLC related");

        plcMeterCat.addMessageSpec(createMessageSpec(RESCAN_PLCBUS_DISPLAY, RESCAN_PLCBUS, false));
        plcMeterCat.addMessageSpec(createActivePLCChannelMessageSpec(SET_ACTIVE_PLC_CHANNEL_DISPLAY, SET_ACTIVE_PLC_CHANNEL, false));
        plcMeterCat.addMessageSpec(createSetMacTimeoutsMessageSpec(SET_SFSK_MAC_TIMEOUTS_DISPLAY, SET_SFSK_MAC_TIMEOUTS, false));
        plcMeterCat.addMessageSpec(createSetFrequenciesMessageSpec(SET_PLC_FREQUENCIES_DISPLAY, SET_PLC_CHANNEL_FREQUENCIES, false));
        plcMeterCat.addMessageSpec(createSetSingleFrequencyMessageSpec(SET_SINGLE_PLC_FREQUENCY_DISPLAY, SET_SINGLE_PLC_CHANNEL_FREQUENCY, true));
        plcMeterCat.addMessageSpec(createSetInitiatorPhaseMessageSpec(SET_SFSK_INITIATOR_PH_DISPLAY, SET_SFSK_INITIATOR_PHASE, false));
        plcMeterCat.addMessageSpec(createSetGainMessageSpec(SET_SFSK_GAIN_DISPLAY, SET_SFSK_GAIN, false));
        plcMeterCat.addMessageSpec(createSetRepeaterMessageSpec(SET_SFSK_REPEATER_DISPLAY, SET_SFSK_REPEATER, false));
        plcMeterCat.addMessageSpec(createSetMaxFrameLengthMessageSpec(SET_SFSK_MAX_FRAME_LENGTH_DISPLAY, SET_SFSK_MAX_FRAME_LENGTH, false));

        categories.add(plcMeterCat);
		return categories;
	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		MessageResult result;

		try {

			if (isMessageTag(RESCAN_PLCBUS, messageEntry)) {
				rescanPLCBus();
			} else if (isMessageTag(SET_ACTIVE_PLC_CHANNEL, messageEntry)) {
				setActivePLCChannel(messageEntry);
			} else if (isMessageTag(SET_SFSK_MAC_TIMEOUTS, messageEntry)) {
				setPLCTimeouts(messageEntry);
			} else if (isMessageTag(SET_PLC_CHANNEL_FREQUENCIES, messageEntry)) {
				setPLCFrequencies(messageEntry);
			} else if (isMessageTag(SET_SINGLE_PLC_CHANNEL_FREQUENCY, messageEntry)) {
				setSinglePLCFrequency(messageEntry);
			} else if (isMessageTag(SET_SFSK_INITIATOR_PHASE, messageEntry)) {
				setInitiatorPhase(messageEntry);
			} else if (isMessageTag(SET_SFSK_GAIN, messageEntry)) {
				setGain(messageEntry);
			} else if (isMessageTag(SET_SFSK_REPEATER, messageEntry)) {
				setRepeater(messageEntry);
			} else if (isMessageTag(SET_SFSK_MAX_FRAME_LENGTH, messageEntry)) {
				setMaxFrameLength(messageEntry);
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
	private void setMaxFrameLength(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("SET_SFSK_MAX_FRAME_LENGTH message received");

		int maxFrameLength = getAttributeAsInteger(messageEntry, SFSKIec61334LLCSetupAttribute.MAX_FRAME_LENGTH.name());

		String attributeName = SFSKIec61334LLCSetupAttribute.MAX_FRAME_LENGTH.name();
		if (maxFrameLength != -1) {
			getAs220().getCosemObjectFactory().getSFSKIec61334LLCSetup().setMaxFrameLength(maxFrameLength);
			final int value = getAs220().getCosemObjectFactory().getSFSKIec61334LLCSetup().getMaxFrameLength().getValue();
			readAfterWriteCheck(value, maxFrameLength, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_MAX_FRAME_LENGTH message: skipping write to " + attributeName + ".");
		}

	}

	/**
	 * @param messageEntry
	 * @throws IOException
	 */
	private void setInitiatorPhase(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("SET_SFSK_INITIATOR_PHASE message received");

		int initiatorPhase = getAttributeAsInteger(messageEntry, SFSKPhyMacSetupAttribute.INITIATOR_ELECTRICAL_PHASE.name());

		String attributeName = SFSKPhyMacSetupAttribute.INITIATOR_ELECTRICAL_PHASE.name();
		if (initiatorPhase != -1) {
			getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setInitiatorElectricalPhase(new ElectricalPhase(initiatorPhase));
			final int value = getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().getInitiatorElectricalPhase().getValue();
			readAfterWriteCheck(value, initiatorPhase, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_INITIATOR_PHASE message: skipping write to " + attributeName + ".");
		}

	}

	/**
	 * @param messageEntry
	 * @throws IOException
	 */
	private void setActivePLCChannel(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("SET_ACTIVE_PLC_CHANNEL message received");
		int channel = getAttributeAsInteger(messageEntry, SFSKPhyMacSetupAttribute.ACTIVE_CHANNEL.name());
		setActivePLCChannel(channel);
	}

	/**
	 * @param messageEntry
	 * @throws IOException
	 */
	private void setRepeater(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("SET_SFSK_REPEATER message received");

		int repeater = getAttributeAsInteger(messageEntry, SFSKPhyMacSetupAttribute.REPEATER.name());

		String attributeName = SFSKPhyMacSetupAttribute.REPEATER.name();
		if (repeater != -1) {
			getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setRepeater(new Repeater(repeater));
			final int value = getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().getRepeater().getValue();
			readAfterWriteCheck(value, repeater, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_REPEATER message: skipping write to " + attributeName + ".");
		}

	}

	/**
	 * @param messageEntry
	 * @throws IOException
	 */
	private void setGain(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("SET_SFSK_GAIN message received");

		String attributeName;

		int maxRxGain = getAttributeAsInteger(messageEntry, SFSKPhyMacSetupAttribute.MAX_RECEIVING_GAIN.name());
		int maxTxGain = getAttributeAsInteger(messageEntry, SFSKPhyMacSetupAttribute.MAX_TRANSMITTING_GAIN.name());
		int searchInitGain = getAttributeAsInteger(messageEntry, SFSKPhyMacSetupAttribute.SEARCH_INITIATOR_GAIN.name());

		attributeName = SFSKPhyMacSetupAttribute.MAX_RECEIVING_GAIN.name();
		if (maxRxGain != -1) {
			getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setMaxReceivingGain(maxRxGain);
			final int value = getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().getMaxReceivingGain().getValue();
			readAfterWriteCheck(value, maxRxGain, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_GAIN message: skipping write to " + attributeName + ".");
		}

		attributeName = SFSKPhyMacSetupAttribute.MAX_TRANSMITTING_GAIN.name();
		if (maxTxGain != -1) {
			getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setMaxTransmittingGain(maxTxGain);
			final int value = getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().getMaxTransmittingGain().getValue();
			readAfterWriteCheck(value, maxTxGain, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_GAIN message: skipping write to " + attributeName + ".");
		}

		attributeName = SFSKPhyMacSetupAttribute.SEARCH_INITIATOR_GAIN.name();
		if (searchInitGain != -1) {
			getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setSearchInitiatorGain(searchInitGain);
			final int value = getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().getSearchInitiatorGain().getValue();
			readAfterWriteCheck(value, searchInitGain, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_GAIN message: skipping write to " + attributeName + ".");
		}

	}

	/**
	 * @param messageEntry
	 * @throws IOException
	 */
	private void setPLCFrequencies(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("SET_PLC_CHANNEL_FREQUENCIES message received");

		long[][] frequencies = new long[NR_OF_CHANNELS][FREQUENCIES_PER_PAIR];
		for (int channel = 0; channel < NR_OF_CHANNELS; channel++) {
			for (int freqType = 0; freqType < FREQUENCIES_PER_PAIR; freqType++) {
				frequencies[channel][freqType] = getAttributeAsLong(messageEntry, FREQUENCIES_NAME[channel][freqType]);
				if (frequencies[channel][freqType] == -1) {
					throw new IOException("Invalid or no value given for the " + FREQUENCIES_NAME[channel][freqType] + " field.");
				}
			}
		}

		Frequencies write = Frequencies.fromLongArray(frequencies);
		getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setFrequencies(frequencies);
		Frequencies now = getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().getFrequencies();

		if (!write.equals(now)) {
			throw new IOException("Read after write check failed for attribute FREQUENCIES: '" + now + "'!='" + write + "'");
		} else {
			getAs220().getLogger().info("SET_PLC_CHANNEL_FREQUENCIES message: Write '" + write + "' to FREQUENCIES success.");
		}

	}

	/**
	 * @param messageEntry
	 * @throws IOException
	 */
	private void setSinglePLCFrequency(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("SET_SINGLE_PLC_CHANNEL_FREQUENCY message received");

		long[][] frequencies = new long[1][FREQUENCIES_PER_PAIR];
		for (int freqType = 0; freqType < FREQUENCIES_PER_PAIR; freqType++) {
			frequencies[0][freqType] = getAttributeAsLong(messageEntry, FREQUENCIES_NAME[0][freqType]);
			if (frequencies[0][freqType] == -1) {
				throw new IOException("Invalid or no value given for the " + FREQUENCIES_NAME[0][freqType] + " field.");
			}
		}

		Frequencies write = Frequencies.fromLongArray(frequencies);
		getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setFrequencies(frequencies);
		Frequencies now = getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().getFrequencies();

		if (!write.equals(now)) {
			throw new IOException("Read after write check failed for attribute FREQUENCIES: '" + now + "'!='" + write + "'");
		} else {
			getAs220().getLogger().info("SET_PLC_CHANNEL_FREQUENCIES message: Write '" + write + "' to FREQUENCIES success.");
		}

	}

	/**
	 * @param messageEntry
	 * @throws IOException
	 */
	private void setPLCTimeouts(MessageEntry messageEntry) throws IOException {
		getAs220().getLogger().info("SET_SFSK_MAC_TIMEOUTS message received");

		String attributeName;

		int searchInitiator = getAttributeAsInteger(messageEntry, SFSKSyncTimeoutsAttribute.SEARCH_INITIATOR_TIMEOUT.name());
		int syncConfirm = getAttributeAsInteger(messageEntry, SFSKSyncTimeoutsAttribute.SYNCHRONIZATION_CONFIRMATION_TIMEOUT.name());
		int notAddressed = getAttributeAsInteger(messageEntry, SFSKSyncTimeoutsAttribute.TIME_OUT_NOT_ADDRESSED.name());
		int frNotOk = getAttributeAsInteger(messageEntry, SFSKSyncTimeoutsAttribute.TIME_OUT_FRAME_NOT_OK.name());

		attributeName = SFSKSyncTimeoutsAttribute.SEARCH_INITIATOR_TIMEOUT.name();
		if (searchInitiator != -1) {
			getAs220().getCosemObjectFactory().getSFSKSyncTimeouts().setSearchInitiatorTimeout(searchInitiator);
			final int value = getAs220().getCosemObjectFactory().getSFSKSyncTimeouts().getSearchInitiatorTimeout().getValue();
			readAfterWriteCheck(value, searchInitiator, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_MAC_TIMEOUTS message: skipping write to " + attributeName + ".");
		}

		attributeName = SFSKSyncTimeoutsAttribute.SYNCHRONIZATION_CONFIRMATION_TIMEOUT.name();
		if (syncConfirm != -1) {
			getAs220().getCosemObjectFactory().getSFSKSyncTimeouts().setSyncConfirmTimeout(syncConfirm);
			final int value = getAs220().getCosemObjectFactory().getSFSKSyncTimeouts().getSyncConfirmTimeout().getValue();
			readAfterWriteCheck(value, syncConfirm, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_MAC_TIMEOUTS message: skipping write to " + attributeName + ".");
		}

		attributeName = SFSKSyncTimeoutsAttribute.TIME_OUT_NOT_ADDRESSED.name();
		if (notAddressed != -1) {
			getAs220().getCosemObjectFactory().getSFSKSyncTimeouts().setTimeoutNotAddressed(notAddressed);
			final int value = getAs220().getCosemObjectFactory().getSFSKSyncTimeouts().getTimeoutNotAddressed().getValue();
			readAfterWriteCheck(value, notAddressed, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_MAC_TIMEOUTS message: skipping write to " + attributeName + ".");
		}

		attributeName = SFSKSyncTimeoutsAttribute.TIME_OUT_FRAME_NOT_OK.name();
		if (frNotOk != -1) {
			getAs220().getCosemObjectFactory().getSFSKSyncTimeouts().setTimeoutFrameNotOk(frNotOk);
			final int value = getAs220().getCosemObjectFactory().getSFSKSyncTimeouts().getTimeoutFrameNotOk().getValue();
			readAfterWriteCheck(value, frNotOk, attributeName);
		} else {
			getAs220().getLogger().info("SET_SFSK_MAC_TIMEOUTS message: skipping write to " + attributeName + ".");
		}

	}

	/**
	 * @param attributeName
	 * @param newValue
	 * @throws IOException
	 */
	private void readAfterWriteCheck(int deviceValue, int writeValue, String attributeName) throws IOException {
		if (writeValue != deviceValue) {
			throw new IOException("Read after write check failed for attribute " + attributeName + ": '" + deviceValue + "'!='" + writeValue + "'");
		}
		getAs220().getLogger().info("Read after write check: Write '" + writeValue + "' to " + attributeName + " success.");
	}

	/**
	 * <b>Also used by the MeterTool </b>
	 * @throws IOException
	 */
	public void rescanPLCBus() throws IOException {
		getAs220().getLogger().info("RESCAN_PLCBUS message received");
		setActivePLCChannel(0);
	}

	/**
	 * <b>Also used by the MeterTool </b>
	 * @param channel
	 * @throws IOException
	 */
	public void setActivePLCChannel(int channel) throws IOException {
		if ((channel < 0) || (channel > NR_OF_CHANNELS)) {
			throw new IOException("Channel can only be 0-6, but was " + channel);
		}

		String attributeName = SFSKPhyMacSetupAttribute.ACTIVE_CHANNEL.name();
		if (channel != -1) {
			getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().setActiveChannel(new Unsigned8(channel));
			final int value = getAs220().getCosemObjectFactory().getSFSKPhyMacSetup().getActiveChannel().getValue();
			readAfterWriteCheck(value, channel, attributeName);
		} else {
			getAs220().getLogger().info("Skipping write to " + attributeName + ".");
		}

		getAs220().getCosemObjectFactory().getSFSKActiveInitiator().doResetNewNotSynchronized(new MacAddress(0));
	}

	/**
	 * @param messageEntry
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	private int getAttributeAsInteger(MessageEntry messageEntry, String attribute) throws IOException {
		String stringValue = MessagingTools.getContentOfAttribute(messageEntry, attribute);
		if ((stringValue == null) || (stringValue.length() == 0) || (stringValue.equals("-"))) {
			return -1;
		} else {
			try {
				return Integer.parseInt(stringValue);
			} catch (NumberFormatException e) {
				throw new IOException("Message attribute contains an invalid value: '" + stringValue + "': " + e.getMessage());
			}
		}
	}

	/**
	 * @param messageEntry
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	private long getAttributeAsLong(MessageEntry messageEntry, String attribute) throws IOException {
		String stringValue = MessagingTools.getContentOfAttribute(messageEntry, attribute);
		if ((stringValue == null) || (stringValue.length() == 0) || (stringValue.equals("-"))) {
			return -1;
		} else {
			try {
				return Long.parseLong(stringValue);
			} catch (NumberFormatException e) {
				throw new IOException("Message attribute contains an invalid value: '" + stringValue + "': " + e.getMessage());
			}
		}
	}

	/**
     * @param keyId
     * @param tagName
     * @param advanced
     * @return
     */
    private MessageSpec createActivePLCChannelMessageSpec(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);

		// We should add this messageSpec, otherwise the other attributeSpecs wont show up in eiserver. Bug??
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);

		tagSpec.add(new MessageAttributeSpec(SFSKPhyMacSetupAttribute.ACTIVE_CHANNEL.name(), false));

		msgSpec.add(tagSpec);
		return msgSpec;
	}

	/**
	 * @param keyId
	 * @param tagName
	 * @param advanced
	 * @return
	 */
	private MessageSpec createSetMacTimeoutsMessageSpec(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);

		// We should add this messageSpec, otherwise the other attributeSpecs wont show up in eiserver. Bug??
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);

		tagSpec.add(new MessageAttributeSpec(SFSKSyncTimeoutsAttribute.SEARCH_INITIATOR_TIMEOUT.name(), false));
		tagSpec.add(new MessageAttributeSpec(SFSKSyncTimeoutsAttribute.SYNCHRONIZATION_CONFIRMATION_TIMEOUT.name(), false));
		tagSpec.add(new MessageAttributeSpec(SFSKSyncTimeoutsAttribute.TIME_OUT_NOT_ADDRESSED.name(), false));
		tagSpec.add(new MessageAttributeSpec(SFSKSyncTimeoutsAttribute.TIME_OUT_FRAME_NOT_OK.name(), false));

		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec createSetFrequenciesMessageSpec(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);

		// We should add this messageSpec, otherwise the other attributeSpecs wont show up in eiserver. Bug??
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);

		for (int channel = 0; channel < NR_OF_CHANNELS; channel++) {
			for (int freqType = 0; freqType < FREQUENCIES_PER_PAIR; freqType++) {
				tagSpec.add(new MessageAttributeSpec(FREQUENCIES_NAME[channel][freqType], false));
			}
		}

		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec createSetSingleFrequencyMessageSpec(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);

		// We should add this messageSpec, otherwise the other attributeSpecs wont show up in eiserver. Bug??
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);

		for (int freqType = 0; freqType < FREQUENCIES_PER_PAIR; freqType++) {
			tagSpec.add(new MessageAttributeSpec(FREQUENCIES_NAME[0][freqType], false));
		}

		msgSpec.add(tagSpec);
		return msgSpec;
	}


	private MessageSpec createSetGainMessageSpec(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);

		// We should add this messageSpec, otherwise the other attributeSpecs wont show up in eiserver. Bug??
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);

		tagSpec.add(new MessageAttributeSpec(SFSKPhyMacSetupAttribute.MAX_RECEIVING_GAIN.name(), false));
		tagSpec.add(new MessageAttributeSpec(SFSKPhyMacSetupAttribute.MAX_TRANSMITTING_GAIN.name(), false));
		tagSpec.add(new MessageAttributeSpec(SFSKPhyMacSetupAttribute.SEARCH_INITIATOR_GAIN.name(), false));

		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec createSetInitiatorPhaseMessageSpec(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);

		// We should add this messageSpec, otherwise the other attributeSpecs wont show up in eiserver. Bug??
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);

		tagSpec.add(new MessageAttributeSpec(SFSKPhyMacSetupAttribute.INITIATOR_ELECTRICAL_PHASE.name(), false));

		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec createSetRepeaterMessageSpec(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);

		// We should add this messageSpec, otherwise the other attributeSpecs wont show up in eiserver. Bug??
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);

		tagSpec.add(new MessageAttributeSpec(SFSKPhyMacSetupAttribute.REPEATER.name(), false));

		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec createSetMaxFrameLengthMessageSpec(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);

		// We should add this messageSpec, otherwise the other attributeSpecs wont show up in eiserver. Bug??
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);

		tagSpec.add(new MessageAttributeSpec(SFSKIec61334LLCSetupAttribute.MAX_FRAME_LENGTH.name(), false));

		msgSpec.add(tagSpec);
		return msgSpec;
	}

}
