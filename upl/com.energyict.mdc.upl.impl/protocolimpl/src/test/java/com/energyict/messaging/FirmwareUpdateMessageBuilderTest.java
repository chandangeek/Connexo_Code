/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.messaging;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirmwareUpdateMessageBuilderTest {

    private static final String PATH = "path";

    @Test
    public void test() throws IOException, SAXException {
        final FirmwareUpdateMessageBuilder builder = new FirmwareUpdateMessageBuilder();

        OfflineDeviceMessage offlineDeviceMessage = Mockito.mock(OfflineDeviceMessage.class);
        Mockito.when(offlineDeviceMessage.getTrackingId()).thenReturn("");

        List<OfflineDeviceMessageAttribute> attributes = new ArrayList<>();
        OfflineDeviceMessageAttribute attribute = Mockito.mock(OfflineDeviceMessageAttribute.class);
        Mockito.when(attribute.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateFileAttributeName);
        Mockito.when(attribute.getValue()).thenReturn(PATH);
        attributes.add(attribute);
        Mockito.doReturn(attributes).when(offlineDeviceMessage).getDeviceMessageAttributes();

        MessageEntry messageEntry = new FirmwareUdateWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateFileAttributeName).createMessageEntry(new AS220(null, null, null), offlineDeviceMessage);
        builder.initFromXml(messageEntry.getContent());

        Assert.assertEquals(PATH, builder.getPath());
    }
}