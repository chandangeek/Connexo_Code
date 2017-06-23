package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.DeviceMessageQueryFilter;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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

    @Test
    public void searchDeviceMessagesFilterMessageCategories() throws Exception {
        DeviceMessageCategory deviceCategory101 = mock(DeviceMessageCategory.class);
        DeviceMessageCategory deviceCategory102 = mock(DeviceMessageCategory.class);
        when(deviceMessageSpecificationService.findCategoryById(anyInt())).thenReturn(Optional.empty());
        when(deviceMessageSpecificationService.findCategoryById(101)).thenReturn(Optional.of(deviceCategory101));
        when(deviceMessageSpecificationService.findCategoryById(102)).thenReturn(Optional.of(deviceCategory102));

        String response = target("/devicemessages").queryParam("filter", ExtjsFilter.filter("messageCategories", Arrays.asList(101, 102))).request().get(String.class);
        ArgumentCaptor<DeviceMessageQueryFilter> queryFilterArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageQueryFilter.class);
        verify(deviceMessageService).findDeviceMessagesByFilter(queryFilterArgumentCaptor.capture());
        assertThat(queryFilterArgumentCaptor.getValue().getMessageCategories()).containsOnly(deviceCategory101, deviceCategory102);
    }

    @Test
    public void searchDeviceMessagesFilterMessageCategoriesAndDeviceCommands() throws Exception {
        DeviceMessageCategory deviceCategory101 = mock(DeviceMessageCategory.class);
        DeviceMessageCategory deviceCategory102 = mock(DeviceMessageCategory.class);
        when(deviceMessageSpecificationService.findCategoryById(anyInt())).thenReturn(Optional.empty());
        when(deviceMessageSpecificationService.findCategoryById(101)).thenReturn(Optional.of(deviceCategory101));
        when(deviceMessageSpecificationService.findCategoryById(102)).thenReturn(Optional.of(deviceCategory102));

        String extjsFilter = ExtjsFilter.filter()
                .property("messageCategories", Arrays.asList(101, 102))
                .property("deviceMessageIds", Collections.singletonList("CLOCK_SET_TIME"))
                .create();
        String response = target("/devicemessages").queryParam("filter", extjsFilter).request().get(String.class);
        ArgumentCaptor<DeviceMessageQueryFilter> queryFilterArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageQueryFilter.class);
        verify(deviceMessageService).findDeviceMessagesByFilter(queryFilterArgumentCaptor.capture());
        assertThat(queryFilterArgumentCaptor.getValue().getMessageCategories()).containsOnly(deviceCategory101, deviceCategory102);
        assertThat(queryFilterArgumentCaptor.getValue().getDeviceMessages()).containsOnly(DeviceMessageId.CLOCK_SET_TIME);
    }

    @Test
    public void searchDeviceMessagesFilterDeviceMessageStatus() throws Exception {

        String extjsFilter = ExtjsFilter.filter()
                .property("statuses", Arrays.asList("CANCELED", "WAITING"))
                .create();
        String response = target("/devicemessages").queryParam("filter", extjsFilter).request().get(String.class);
        ArgumentCaptor<DeviceMessageQueryFilter> queryFilterArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageQueryFilter.class);
        verify(deviceMessageService).findDeviceMessagesByFilter(queryFilterArgumentCaptor.capture());
        assertThat(queryFilterArgumentCaptor.getValue().getStatuses()).containsOnly(DeviceMessageStatus.CANCELED, DeviceMessageStatus.WAITING);
    }

    @Test
    public void searchDeviceMessagesFilterReleaseDates() throws Exception {

        Instant start = Instant.now();
        Instant end = start.plusMillis(1000);
        String extjsFilter = ExtjsFilter.filter()
                .property("releaseDateStart", start.toEpochMilli())
                .property("releaseDateEnd", end.toEpochMilli())
                .create();
        String response = target("/devicemessages").queryParam("filter", extjsFilter).request().get(String.class);
        ArgumentCaptor<DeviceMessageQueryFilter> queryFilterArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageQueryFilter.class);
        verify(deviceMessageService).findDeviceMessagesByFilter(queryFilterArgumentCaptor.capture());
        assertThat(queryFilterArgumentCaptor.getValue().getReleaseDateStart()).isPresent();
        assertThat(queryFilterArgumentCaptor.getValue().getReleaseDateStart().get()).isEqualTo(start);
        assertThat(queryFilterArgumentCaptor.getValue().getReleaseDateEnd()).isPresent();
        assertThat(queryFilterArgumentCaptor.getValue().getReleaseDateEnd().get()).isEqualTo(end);
    }

    @Test
    public void searchDeviceMessagesFilterSentDates() throws Exception {

        Instant start = Instant.now();
        Instant end = start.plusMillis(1000);
        String extjsFilter = ExtjsFilter.filter()
                .property("sentDateStart", start.toEpochMilli())
                .property("sentDateEnd", end.toEpochMilli())
                .create();
        String response = target("/devicemessages").queryParam("filter", extjsFilter).request().get(String.class);
        ArgumentCaptor<DeviceMessageQueryFilter> queryFilterArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageQueryFilter.class);
        verify(deviceMessageService).findDeviceMessagesByFilter(queryFilterArgumentCaptor.capture());
        assertThat(queryFilterArgumentCaptor.getValue().getSentDateStart()).isPresent();
        assertThat(queryFilterArgumentCaptor.getValue().getSentDateStart().get()).isEqualTo(start);
        assertThat(queryFilterArgumentCaptor.getValue().getSentDateEnd()).isPresent();
        assertThat(queryFilterArgumentCaptor.getValue().getSentDateEnd().get()).isEqualTo(end);
    }

    @Test
    public void searchDeviceMessagesFilterCreationDates() throws Exception {

        Instant start = Instant.now();
        Instant end = start.plusMillis(1000);
        String extjsFilter = ExtjsFilter.filter()
                .property("creationDateStart", start.toEpochMilli())
                .property("creationDateEnd", end.toEpochMilli())
                .create();
        String response = target("/devicemessages").queryParam("filter", extjsFilter).request().get(String.class);
        ArgumentCaptor<DeviceMessageQueryFilter> queryFilterArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageQueryFilter.class);
        verify(deviceMessageService).findDeviceMessagesByFilter(queryFilterArgumentCaptor.capture());
        assertThat(queryFilterArgumentCaptor.getValue().getCreationDateStart()).isPresent();
        assertThat(queryFilterArgumentCaptor.getValue().getCreationDateStart().get()).isEqualTo(start);
        assertThat(queryFilterArgumentCaptor.getValue().getCreationDateEnd()).isPresent();
        assertThat(queryFilterArgumentCaptor.getValue().getCreationDateEnd().get()).isEqualTo(end);
    }

}
