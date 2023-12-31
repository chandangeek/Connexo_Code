/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.itron.em620.messages;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.hs3300.messages.HS3300MessageExecutor;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class EM620Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {
    private EM620MessageExecutor messageExecutor;
    private List<DeviceMessageSpec> supportedMessages = new ArrayList<>();
    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private final Converter converter;
    private final NlsService nlsService;
    private final PropertySpecService propertySpecService;
    private final TariffCalendarExtractor tariffCalendarExtractor;

    public EM620Messaging(AbstractDlmsProtocol protocol, Converter converter, NlsService nlsService,
                          PropertySpecService propertySpecService, TariffCalendarExtractor tariffCalendarExtractor) {
        super(protocol);
        this.converter = converter;
        this.nlsService = nlsService;
        this.propertySpecService = propertySpecService;
        this.tariffCalendarExtractor = tariffCalendarExtractor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        supportedMessages.add(DeviceActionMessage.BILLING_RESET.get(this.propertySpecService, this.nlsService, this.converter));
        supportedMessages.add(ClockDeviceMessage.EnableOrDisableDST.get(this.propertySpecService, this.nlsService, this.converter));
        supportedMessages.add(ClockDeviceMessage.SetStartOfDST.get(this.propertySpecService, this.nlsService, this.converter));
        supportedMessages.add(ClockDeviceMessage.SetEndOfDST.get(this.propertySpecService, this.nlsService, this.converter));
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME.get(this.propertySpecService, this.nlsService, this.converter));
        supportedMessages.add(PowerConfigurationDeviceMessage.SetVoltageAndCurrentParameters.get(this.propertySpecService, this.nlsService, this.converter));
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
                return String.valueOf(((Date) messageAttribute).getTime()); //Millis since 1970
            case DeviceMessageConstants.fullActivityCalendarAttributeName:
                this.tariffCalendarExtractor.threadContext().setDevice(offlineDevice);
                this.tariffCalendarExtractor.threadContext().setMessage(offlineDeviceMessage);
                TariffCalendar calender = (TariffCalendar) messageAttribute;
                //The ID and the XML representation of the code table, separated by a |
                return tariffCalendarExtractor.id(calender) + TimeOfUseMessageEntry.SEPARATOR + convertCodeTableToXML(calender, tariffCalendarExtractor, 0 , "0");
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

    protected EM620MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new EM620MessageExecutor(getProtocol());
        }
        return messageExecutor;
    }
}
