/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignItem;

import java.time.Instant;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInCampaignInfoFactoryTest {

    private static TimeOfUseCampaignItem timeOfUseItem = mock(TimeOfUseCampaignItem.class);
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static ServiceCall retrySC = mock(ServiceCall.class);
    private static ServiceCall cancelSC = mock(ServiceCall.class);
    private static Device device = mock(Device.class);
    private static DeviceInCampaignInfoFactory deviceInCampaignInfoFactory;

    @BeforeClass
    public static void setUp() {
        when(timeOfUseItem.retry()).thenReturn(retrySC);
        when(timeOfUseItem.cancel()).thenReturn(cancelSC);
        when(timeOfUseItem.getDevice()).thenReturn(device);
        when(device.getId()).thenReturn(1L);
        when(device.getName()).thenReturn("TestDevice");
        when(retrySC.getState()).thenReturn(DefaultState.PENDING);
        when(cancelSC.getState()).thenReturn(DefaultState.CANCELLED);
        when(cancelSC.getLastModificationTime()).thenReturn(Instant.ofEpochSecond(8000));
        when(retrySC.getCreationTime()).thenReturn(Instant.ofEpochSecond(3000));
        when(cancelSC.getCreationTime()).thenReturn(Instant.ofEpochSecond(5000));
        deviceInCampaignInfoFactory = new DeviceInCampaignInfoFactory(thesaurus);

    }

    @Test
    public void retryTest() {
        DeviceInCampaignInfo deviceInCampaignInfo = deviceInCampaignInfoFactory.create(device, timeOfUseItem.retry());
        assertEquals(deviceInCampaignInfo.device.name, "TestDevice");
        assertEquals(deviceInCampaignInfo.device.id, 1L);
        assertEquals(deviceInCampaignInfo.status, "Pending");
        assertEquals(deviceInCampaignInfo.startedOn, Instant.ofEpochSecond(3000));
        assertNull(deviceInCampaignInfo.finishedOn);
    }

    @Test
    public void cancelTest() {
        DeviceInCampaignInfo deviceInCampaignInfo = deviceInCampaignInfoFactory.create(device, timeOfUseItem.cancel());
        assertEquals(deviceInCampaignInfo.device.name, "TestDevice");
        assertEquals(deviceInCampaignInfo.device.id, 1L);
        assertEquals(deviceInCampaignInfo.status, "Cancelled");
        assertEquals(deviceInCampaignInfo.startedOn, Instant.ofEpochSecond(5000));
        assertEquals(deviceInCampaignInfo.finishedOn, Instant.ofEpochSecond(8000));
    }

}
