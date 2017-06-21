package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.DeviceMessageQueryFilter;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class DeviceMessageSearchResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Finder<DeviceMessage> finder = mockFinder(Collections.emptyList());
        when(deviceMessageService.findDeviceMessagesByFilter(any(DeviceMessageQueryFilter.class))).thenReturn(finder);
    }

    @Test
    public void searchDeviceMessagesNoFilter() throws Exception {
        String response = target("/devicemessages").request().get(String.class);
        ArgumentCaptor<DeviceMessageQueryFilter> queryFilterArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageQueryFilter.class);
        verify(deviceMessageService).findDeviceMessagesByFilter(queryFilterArgumentCaptor.capture());
        // just assert the resource exists on the URL
    }
    @Test
    public void searchDeviceMessagesFilterDeviceGroups() throws Exception {
        EndDeviceGroup endDeviceGroup11 = mock(EndDeviceGroup.class);
        EndDeviceGroup endDeviceGroup22 = mock(EndDeviceGroup.class);
        when(meteringGroupService.findEndDeviceGroup(anyLong())).thenReturn(Optional.empty());
        when(meteringGroupService.findEndDeviceGroup(11)).thenReturn(Optional.of(endDeviceGroup11));
        when(meteringGroupService.findEndDeviceGroup(22)).thenReturn(Optional.of(endDeviceGroup22));

        String response = target("/devicemessages").queryParam("filter", ExtjsFilter.filter("deviceGroups", Arrays.asList(11,22))).request().get(String.class);
        ArgumentCaptor<DeviceMessageQueryFilter> queryFilterArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageQueryFilter.class);
        verify(deviceMessageService).findDeviceMessagesByFilter(queryFilterArgumentCaptor.capture());
        assertThat(queryFilterArgumentCaptor.getValue().getDeviceGroups()).contains(endDeviceGroup11, endDeviceGroup22);

    }
}
