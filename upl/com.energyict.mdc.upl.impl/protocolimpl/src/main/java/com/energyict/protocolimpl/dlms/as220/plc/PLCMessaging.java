package com.energyict.protocolimpl.dlms.as220.plc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.energyict.dlms.axrdencoding.Unsigned8;
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

public class PLCMessaging extends AbstractSubMessageProtocol {

	public static final String	RESCAN_PLCBUS					= "RescanPlcBus";
	public static final String	SET_ACTIVE_PLC_CHANNEL			= "SetActivePlcChannel";
	public static final String	SET_SFSK_MAC_TIMEOUTS			= "SetSFSKMacTimeouts";

	private static final String	RESCAN_PLCBUS_DISPLAY			= "Force manual rescan PLC bus";
	private static final String	SET_ACTIVE_PLC_CHANNEL_DISPLAY	= "Set active PLC channel";
	private static final String	SET_SFSK_MAC_TIMEOUTS_DISPLAY	= "Set the S-FSK Mac timeouts";

	private final AS220 as220;

	public PLCMessaging(AS220 as220) {
		this.as220 = as220;
		addSupportedMessageTag(RESCAN_PLCBUS);
		addSupportedMessageTag(SET_ACTIVE_PLC_CHANNEL);
		addSupportedMessageTag(SET_SFSK_MAC_TIMEOUTS);
	}

	public AS220 getAs220() {
		return as220;
	}

	public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec plcMeterCat = new MessageCategorySpec("[02] PLC related");

        plcMeterCat.addMessageSpec(createMessageSpec(RESCAN_PLCBUS_DISPLAY, RESCAN_PLCBUS, false));
        plcMeterCat.addMessageSpec(createActivePLCChannelMessageSpec(SET_ACTIVE_PLC_CHANNEL_DISPLAY, SET_ACTIVE_PLC_CHANNEL, false));
        plcMeterCat.addMessageSpec(createSetMacTimeoutsMessageSpec(SET_SFSK_MAC_TIMEOUTS_DISPLAY, SET_SFSK_MAC_TIMEOUTS, false));

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

	private void rescanPLCBus() throws IOException {
		getAs220().getLogger().info("RESCAN_PLCBUS message received");
		setActivePLCChannel(0);
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

    private MessageSpec createActivePLCChannelMessageSpec(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);

        MessageValueSpec mvs = new MessageValueSpec();
        tagSpec.add(mvs);

        msgSpec.add(tagSpec);
        return msgSpec;
	}

	private MessageSpec createSetMacTimeoutsMessageSpec(String keyId, String tagName, boolean advanced) {
		System.out.println("createSetMacTimeoutsMessageSpec");
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

}
