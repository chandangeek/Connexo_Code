/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;

import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FirmwareUpdateMessageBuilderTest {

    private static final String PATH = "path";

    @Test
    public void test() throws IOException, SAXException {
        final FirmwareUpdateMessageBuilder builder = new FirmwareUpdateMessageBuilder();

        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage.getTrackingId()).thenReturn("");

        List<OfflineDeviceMessageAttribute> attributes = new ArrayList<>();
        OfflineDeviceMessageAttribute attribute = mock(OfflineDeviceMessageAttribute.class);
        when(attribute.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateFileAttributeName);
        when(attribute.getDeviceMessageAttributeValue()).thenReturn(PATH);
        attributes.add(attribute);
        when(offlineDeviceMessage.getDeviceMessageAttributes()).thenReturn(attributes);

        MessageEntry messageEntry = new FirmwareUdateWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateFileAttributeName).createMessageEntry(new AS220(null, null, null, null), offlineDeviceMessage);
        builder.initFromXml(messageEntry.getContent());

        assertEquals(PATH, builder.getPath());
    }
}