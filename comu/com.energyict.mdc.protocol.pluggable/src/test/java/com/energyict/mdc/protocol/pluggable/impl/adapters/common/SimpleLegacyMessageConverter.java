package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.pluggable.mocks.DeviceMessageTestSpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Simple test class to correctly perform tests on the adapters
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 9:52
 */
public class SimpleLegacyMessageConverter implements LegacyMessageConverter {

    public static final String codeTableFormattingResult = "ThisIsTheCodeTableFormattingResult";
    public static final String dateFormattingResult = "ThisIsTheDateFormattingResult";

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.<DeviceMessageSpec>asList(
                DeviceMessageTestSpec.extendedSpecs(getCodeFactory()),
                DeviceMessageTestSpec.allSimpleSpecs());
    }

    private IdBusinessObjectFactory getCodeFactory() {
        return (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.CODE.id());
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (Code.class.isAssignableFrom(messageAttribute.getClass())) {
            return codeTableFormattingResult;
        } else if (Date.class.isAssignableFrom(messageAttribute.getClass())) {
            return dateFormattingResult;
        }
        return messageAttribute.toString();
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        return new MessageEntry(offlineDeviceMessage.toString(), "");
    }

    @Override
    public void setMessagingProtocol(Messaging messagingProtocol) {
        // nothing to do
    }
}
