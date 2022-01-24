package com.energyict.protocolimplv2.dlms.landisAndGyr.messages;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Class that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 11:32
 * Author: khe
 */
public class ZMYMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

	private final AbstractMessageExecutor messageExecutor;
	private List<DeviceMessageSpec> supportedMessages = new ArrayList<>();
	protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	private final Converter converter;
	private final NlsService nlsService;
	private final PropertySpecService propertySpecService;

	public ZMYMessaging(AbstractMessageExecutor messageExecutor, Converter converter, NlsService nlsService, PropertySpecService propertySpecService) {
		super(messageExecutor.getProtocol());
		this.messageExecutor = messageExecutor;
		this.converter = converter;
		this.nlsService = nlsService;
		this.propertySpecService = propertySpecService;
	}

	@Override
	public List<DeviceMessageSpec> getSupportedMessages() {
		supportedMessages.add(DeviceActionMessage.BILLING_RESET.get(this.propertySpecService, this.nlsService, this.converter));

		supportedMessages.add(ClockDeviceMessage.EnableOrDisableDST.get(this.propertySpecService, this.nlsService, this.converter));
		supportedMessages.add(ClockDeviceMessage.SetStartOfDST.get(this.propertySpecService, this.nlsService, this.converter));
		supportedMessages.add(ClockDeviceMessage.SetEndOfDST.get(this.propertySpecService, this.nlsService, this.converter));

		supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME.get(this.propertySpecService, this.nlsService, this.converter));

		supportedMessages.add(PowerConfigurationDeviceMessage.SetVoltageRatioNumerator.get(this.propertySpecService, this.nlsService, this.converter));
		supportedMessages.add(PowerConfigurationDeviceMessage.SetCurrentRatioNumerator.get(this.propertySpecService, this.nlsService, this.converter));

		supportedMessages.add(DeviceActionMessage.ReadDLMSAttribute.get(this.propertySpecService, this.nlsService, this.converter));

		return supportedMessages;
	}

	@Override
	public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
		switch (propertySpec.getName()) {
			case DeviceMessageConstants.ConfigurationChangeDate:
				return dateFormat.format((Date) messageAttribute);
			case DeviceMessageConstants.enableDSTAttributeName:
				return ((Boolean) messageAttribute).booleanValue() ? "1" : "0";
			case DeviceMessageConstants.activityCalendarActivationDateAttributeName:
			case DeviceMessageConstants.activityCalendarCodeTableAttributeName:
				return String.valueOf(((Date) messageAttribute).getTime()); //Millis since 1970
			default:
				return messageAttribute.toString();
		}
	}

	@Override
	public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
		return Optional.empty();
	}

	@Override
	public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
		return getMessageExecutor().updateSentMessages(sentMessages);
	}

	@Override
	public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
		return getMessageExecutor().executePendingMessages(pendingMessages);
	}

	protected AbstractMessageExecutor getMessageExecutor() {
		return messageExecutor;
	}
}
