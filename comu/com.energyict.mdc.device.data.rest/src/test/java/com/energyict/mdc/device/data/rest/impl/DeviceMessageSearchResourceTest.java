package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceMessageQueryFilter;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import com.jayway.jsonpath.JsonModel;

import java.io.InputStream;
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
import static org.mockito.Mockito.times;
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
    public void searchDeviceMessageResultFields() throws Exception {
        DeviceMessage deviceMessage = mock(DeviceMessage.class);

        Finder<DeviceMessage> finder = mockFinder(Collections.singletonList(deviceMessage));
        when(deviceMessageService.findDeviceMessagesByFilter(any(DeviceMessageQueryFilter.class))).thenReturn(finder);
        when(deviceMessage.getId()).thenReturn(1L);
        com.energyict.mdc.device.data.Device device = mock(com.energyict.mdc.device.data.Device.class);
        DeviceConfiguration configuration = mock(DeviceConfiguration.class);
        when(configuration.getId()).thenReturn(100L);
        when(configuration.getName()).thenReturn("config");
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(200L);
        when(deviceType.getName()).thenReturn("type");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceConfiguration()).thenReturn(configuration);
        when(deviceMessage.getDevice()).thenReturn(device);
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec.getId()).thenReturn(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE);
        when(deviceMessageSpec.getName()).thenReturn("calendar");
        DeviceMessageCategory category = mock(DeviceMessageCategory.class);
        when(deviceMessageSpec.getCategory()).thenReturn(category);
        when(deviceMessage.getSpecification()).thenReturn(deviceMessageSpec);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(deviceMessage.getSentDate()).thenReturn(Optional.empty());

        javax.ws.rs.core.Response response = target("/devicemessages").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.deviceMessages[0].id")).isEqualTo(1);
    }
    @Test
    public void searchDeviceMessagesCached() throws Exception {
        DeviceMessage deviceMessage1 = mockDeviceMessage(1L, 100L, 200L, DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE);
        DeviceMessage deviceMessage2 = mockDeviceMessage(2L, 100L, 200L, DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE);

        Finder<DeviceMessage> finder = mockFinder(Arrays.asList(deviceMessage1, deviceMessage2));
        when(deviceMessageService.findDeviceMessagesByFilter(any(DeviceMessageQueryFilter.class))).thenReturn(finder);
        javax.ws.rs.core.Response response = target("/devicemessages").request().get();

        verify(deviceMessageService, times(1)).canUserAdministrateDeviceMessage(any(DeviceConfiguration.class), any(DeviceMessageId.class));
    }

    private DeviceMessage mockDeviceMessage(long id, long deviceConfigId, long deviceTypeId, DeviceMessageId deviceMessageId) {
        DeviceMessage deviceMessage1 = mock(DeviceMessage.class);
        when(deviceMessage1.getId()).thenReturn(id);
        com.energyict.mdc.device.data.Device device = mock(com.energyict.mdc.device.data.Device.class);
        DeviceConfiguration configuration = mock(DeviceConfiguration.class);
        when(configuration.getId()).thenReturn(deviceConfigId);
        when(configuration.getName()).thenReturn("config");
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(deviceTypeId);
        when(deviceType.getName()).thenReturn("type");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceConfiguration()).thenReturn(configuration);
        when(deviceMessage1.getDevice()).thenReturn(device);
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec.getId()).thenReturn(deviceMessageId);
        when(deviceMessageSpec.getName()).thenReturn("calendar");
        DeviceMessageCategory category = mock(DeviceMessageCategory.class);
        when(deviceMessageSpec.getCategory()).thenReturn(category);
        when(deviceMessage1.getSpecification()).thenReturn(deviceMessageSpec);
        when(deviceMessage1.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(deviceMessage1.getSentDate()).thenReturn(Optional.empty());
        when(deviceMessage1.getReleaseDate()).thenReturn(Instant.now());
        return deviceMessage1;
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
