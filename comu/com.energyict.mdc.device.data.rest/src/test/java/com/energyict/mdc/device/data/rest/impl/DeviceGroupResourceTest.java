/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.GroupBuilder;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.rest.util.StatusCode;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.impl.SearchBuilderImpl;
import com.elster.jupiter.search.impl.SearchMonitor;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.ExecutionTimer;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/13/14.
 */
public class DeviceGroupResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final String ALL_FILTER = JsonModel.model(ImmutableList.of(ImmutableMap.of(
            "property", "name",
            "value", ImmutableList.of(
                    ImmutableMap.of(
                            "operator", "==",
                            "criteria", "*"
                    )
            )))).toJson();
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private Device device;
    @Mock
    private Meter meter;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private Finder<Device> finder;
    @Mock
    private SearchBuilder<Device> searchBuilder;
    @Mock
    private Query<EndDeviceGroup> endDeviceGroupQuery;
    @Captor
    private ArgumentCaptor<List<SearchablePropertyCondition>> searchablePropertyConditionsCaptor;

    @Override
    protected void setupTranslations() {
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
    }

    @Test
    public void testGetQueryEndDeviceGroup() throws Exception {
        when(meteringGroupService.getQueryEndDeviceGroupQuery()).thenReturn(endDeviceGroupQuery);
        EndDeviceGroup endDeviceGroup = mock(QueryEndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(13L);
        when(endDeviceGroup.getName()).thenReturn("South region");
        when(endDeviceGroup.getMRID()).thenReturn("LAPOPKLQKS");
        when(endDeviceGroup.isDynamic()).thenReturn(true);
        List<EndDeviceGroup> endDeviceGroups = Collections.singletonList(endDeviceGroup);
        when(endDeviceGroupQuery.select(anyObject(), anyObject())).thenReturn(endDeviceGroups);

        String response = target("/devicegroups").queryParam("type", "QueryEndDeviceGroup").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.devicegroups[0].name")).isEqualTo("South region");
        assertThat(jsonModel.<String>get("$.devicegroups[0].mRID")).isEqualTo("LAPOPKLQKS");
        assertThat(jsonModel.<Integer>get("$.devicegroups[0].id")).isEqualTo(13);
        assertThat(jsonModel.<Boolean>get("$.devicegroups[0].dynamic")).isEqualTo(true);
    }

    @Test
    public void testGetEndDeviceGroupQueryWithFilter() throws Exception {
        when(meteringGroupService.getEndDeviceGroupQuery()).thenReturn(endDeviceGroupQuery);
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(13L);
        when(endDeviceGroup.getName()).thenReturn("South region");
        List<EndDeviceGroup> endDeviceGroups = Collections.singletonList(endDeviceGroup);
        when(endDeviceGroupQuery.select(any(), eq(1), eq(11), any())).thenReturn(endDeviceGroups);

        String response = target("/devicegroups").queryParam("filter", ExtjsFilter.filter("name", "South region"))
                .queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        ArgumentCaptor<Condition> captor = ArgumentCaptor.forClass(Condition.class);
        verify(endDeviceGroupQuery).select(captor.capture(), eq(1), eq(11), any());
        Condition actual = captor.getValue();
        assertThat(actual).isInstanceOf(Comparison.class);
        assertThat(actual.toString()).isEqualTo(where("name").isEqualTo("South region").toString());
        assertThat(((Comparison) actual).getValues()).containsExactly("South+region");

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.devicegroups[0].name")).isEqualTo("South region");
    }

    @Test
    public void testGetEndDeviceGroup() throws Exception {
        when(meteringGroupService.getEndDeviceGroupQuery()).thenReturn(endDeviceGroupQuery);
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(13L);
        when(endDeviceGroup.getName()).thenReturn("south region");
        when(endDeviceGroup.getMRID()).thenReturn("ABC");
        when(endDeviceGroup.getType()).thenReturn("EndDeviceGroup");
        when(endDeviceGroup.isDynamic()).thenReturn(false);
        List<EndDeviceGroup> endDeviceGroups = Collections.singletonList(endDeviceGroup);
        when(endDeviceGroupQuery.select(anyObject(), anyObject())).thenReturn(endDeviceGroups);

        String response = target("/devicegroups").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.devicegroups[0].id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.devicegroups[0].name")).isEqualTo("south region");
        assertThat(jsonModel.<String>get("$.devicegroups[0].mRID")).isEqualTo("ABC");
        assertThat(jsonModel.<Boolean>get("$.devicegroups[0].dynamic")).isEqualTo(false);
        assertThat(jsonModel.<Integer>get("$.devicegroups[0].version")).isEqualTo(0);
    }

    @Test
    public void testCreateQueryEndDeviceGroupWithoutSearchDomain() throws IOException {
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.empty());
        DeviceGroupInfo info = new DeviceGroupInfo();
        info.name = "NewQueryGroup";
        info.dynamic = true;
        info.filter = "[]";

        Response response = target("/devicegroups").request().post(Entity.json(info));
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Device search domain is not registered");
    }

    @Test
    public void testCreateQueryEndDeviceGroupWithoutFilters() throws IOException {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(Device.class.getName());
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));
        DeviceGroupInfo info = new DeviceGroupInfo();
        info.name = "NewQueryGroup";
        info.dynamic = true;
        info.filter = "[]";

        Response response = target("/devicegroups").request().post(Entity.json(info));
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("At least one search criterion has to be provided");
    }

    @Test
    public void testCreateQueryEndDeviceGroup() throws IOException {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(Device.class.getName());
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));
        SearchableProperty nameSearchableProperty = mock(SearchableProperty.class);
        when(nameSearchableProperty.getName()).thenReturn("name");
        when(searchDomain.getPropertiesValues(any())).thenAnswer(invocationOnMock -> Collections.singletonList(
                ((Function<SearchableProperty, SearchablePropertyValue>) invocationOnMock.getArguments()[0])
                        .apply(nameSearchableProperty)));

        QueryEndDeviceGroup group = mock(QueryEndDeviceGroup.class);
        when(group.getId()).thenReturn(133L);
        GroupBuilder.QueryGroupBuilder<EndDevice, QueryEndDeviceGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.QueryGroupBuilder.class);
        doReturn(builder).when(meteringGroupService).createQueryEndDeviceGroup(anyVararg());

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.name = "NewQueryGroup";
        info.dynamic = true;
        info.filter = ALL_FILTER;

        String response = target("/devicegroups").request().post(Entity.json(info), String.class);

        ArgumentCaptor<SearchablePropertyValue> captor = ArgumentCaptor.forClass(SearchablePropertyValue.class);
        verify(meteringGroupService).createQueryEndDeviceGroup(captor.capture());
        SearchablePropertyValue searchablePropertyValue = captor.getValue();
        assertThat(searchablePropertyValue.getProperty()).isEqualTo(nameSearchableProperty);
        assertThat(searchablePropertyValue.getValueBean().propertyName).isEqualTo("name");
        assertThat(searchablePropertyValue.getValueBean().operator).isEqualTo(SearchablePropertyOperator.EQUAL);
        assertThat(searchablePropertyValue.getValueBean().values).containsExactly("*");
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDC:" + info.name);
        verify(builder).setSearchDomain(searchDomain);
        verify(builder).setQueryProviderName(anyString());
        verify(builder).setLabel("MDC");
        verify(builder).create();

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(133);
    }

    @Test
    public void testCreateEnumeratedEndDeviceGroupFromFilter() throws Exception {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(Device.class.getName());
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));
        SearchMonitor dummyMonitor = mock(SearchMonitor.class);
        ExecutionTimer timer = mock(ExecutionTimer.class);
        doAnswer(invocation -> ((Callable) invocation.getArguments()[0]).call()).when(timer).time(any(Callable.class));
        when(dummyMonitor.searchTimer(searchDomain)).thenReturn(timer);

        when(searchService.search(Device.class)).thenReturn(new SearchBuilderImpl<>(searchDomain, dummyMonitor));
        when(device.getId()).thenReturn(31L);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter("31")).thenReturn(Optional.of(meter));
        SearchableProperty nameSearchableProperty = mock(SearchableProperty.class, Answers.RETURNS_DEEP_STUBS.get());
        when(nameSearchableProperty.getName()).thenReturn("name");
        when(nameSearchableProperty.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.SINGLE);
        when(nameSearchableProperty.getSpecification().getValueFactory()).thenReturn(new StringFactory());
        when(searchDomain.getPropertiesValues(any())).thenAnswer(invocationOnMock -> Collections.singletonList(
                ((Function<SearchableProperty, SearchablePropertyValue>) invocationOnMock.getArguments()[0])
                        .apply(nameSearchableProperty)));
        doReturn(finder).when(searchDomain).finderFor(anyListOf(SearchablePropertyCondition.class));
        when(finder.find()).thenReturn(Collections.singletonList(device));
        when(finder.stream()).thenReturn(Stream.of(device));

        EnumeratedEndDeviceGroup group = mock(EnumeratedEndDeviceGroup.class);
        when(group.getId()).thenReturn(133L);
        GroupBuilder.EnumeratedGroupBuilder<EndDevice, EnumeratedEndDeviceGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.EnumeratedGroupBuilder.class);
        doReturn(builder).when(meteringGroupService).createEnumeratedEndDeviceGroup(anyVararg());

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.name = "NewEnumGroup";
        info.dynamic = false;
        info.filter = ALL_FILTER;

        String response = target("/devicegroups").request().post(Entity.json(info), String.class);

        verify(searchDomain).finderFor(searchablePropertyConditionsCaptor.capture());
        List<SearchablePropertyCondition> conditions = searchablePropertyConditionsCaptor.getValue();
        assertThat(conditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = conditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(nameSearchableProperty);
        Condition condition = searchablePropertyCondition.getCondition();
        assertThat(condition).isInstanceOf(Comparison.class);
        assertThat(((Comparison) condition).getOperator()).isEqualTo(Operator.LIKEIGNORECASE);
        assertThat(((Comparison) condition).getValues()).containsExactly("%");

        verify(meteringGroupService).createEnumeratedEndDeviceGroup(meter);
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDC:" + info.name);
        verify(builder).setLabel("MDC");
        verify(builder).create();

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(133);
    }

    @Test
    public void testCreateEnumeratedEndDeviceGroupFromList() throws Exception {
        when(device.getId()).thenReturn(31L);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter("31")).thenReturn(Optional.of(meter));

        EnumeratedEndDeviceGroup group = mock(EnumeratedEndDeviceGroup.class);
        when(group.getId()).thenReturn(133L);
        GroupBuilder.EnumeratedGroupBuilder<EndDevice, EnumeratedEndDeviceGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.EnumeratedGroupBuilder.class);
        doReturn(builder).when(meteringGroupService).createEnumeratedEndDeviceGroup(anyVararg());

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.name = "NewEnumGroup";
        info.dynamic = false;
        info.filter = ALL_FILTER;
        info.devices = Collections.singletonList(31L);

        String response = target("/devicegroups").request().post(Entity.json(info), String.class);

        verify(meteringGroupService).createEnumeratedEndDeviceGroup(meter);
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDC:" + info.name);
        verify(builder).setLabel("MDC");
        verify(builder).create();

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(133);
    }

    @Test
    public void testValidateQueryEndDeviceGroupCreation() throws Exception {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(Device.class.getName());
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));

        QueryEndDeviceGroup group = mock(QueryEndDeviceGroup.class);
        GroupBuilder.QueryGroupBuilder<EndDevice, QueryEndDeviceGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.QueryGroupBuilder.class);
        doReturn(builder).when(meteringGroupService).createQueryEndDeviceGroup(anyVararg());

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.name = "NewQueryGroup";
        info.dynamic = true;

        Response response = target("/devicegroups").queryParam("validate", true).request().post(Entity.json(info));

        verify(meteringGroupService).createQueryEndDeviceGroup(anyVararg());
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDC:" + info.name);
        verify(builder).setLabel("MDC");
        verify(builder).setSearchDomain(searchDomain);
        verify(builder).setQueryProviderName(anyString());
        verify(builder).validate();
        verify(builder, never()).create();

        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    public void testValidateEnumeratedEndDeviceGroupCreation() throws Exception {
        EnumeratedEndDeviceGroup group = mock(EnumeratedEndDeviceGroup.class);
        GroupBuilder.EnumeratedGroupBuilder<EndDevice, EnumeratedEndDeviceGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.EnumeratedGroupBuilder.class);
        doReturn(builder).when(meteringGroupService).createEnumeratedEndDeviceGroup(anyVararg());

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.name = "NewEnumeratedGroup";
        info.dynamic = false;

        Response response = target("/devicegroups").queryParam("validate", true).request().post(Entity.json(info));

        verify(meteringGroupService).createEnumeratedEndDeviceGroup(anyVararg());
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDC:" + info.name);
        verify(builder).setLabel("MDC");
        verify(builder).validate();
        verify(builder, never()).create();

        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    public void testValidationFailsNoName() throws IOException {
        DeviceGroupInfo info = new DeviceGroupInfo();
        info.dynamic = true;

        Response response = target("/devicegroups").queryParam("validate", true).request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<List<String>>get("$.errors[*].id")).containsExactly("name");
        assertThat(jsonModel.<List<String>>get("$.errors[*].msg")).containsExactly("This field is required");
    }

    @Test
    public void testValidationFailsEmptyName() throws IOException {
        DeviceGroupInfo info = new DeviceGroupInfo();
        info.dynamic = true;
        info.name = "  ";

        Response response = target("/devicegroups").queryParam("validate", true).request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<List<String>>get("$.errors[*].id")).containsExactly("name");
        assertThat(jsonModel.<List<String>>get("$.errors[*].msg")).containsExactly("This field is required");
    }

    @Test
    public void testValidationFailsNoSearchDomain() throws IOException {
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.empty());

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.dynamic = true;
        info.name = "NewGroup";

        Response response = target("/devicegroups").queryParam("validate", true).request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Device search domain is not registered");
    }

    @Test
    public void testValidationFails() throws IOException {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(Device.class.getName());
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));

        QueryEndDeviceGroup group = mock(QueryEndDeviceGroup.class);
        GroupBuilder.QueryGroupBuilder<EndDevice, QueryEndDeviceGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.QueryGroupBuilder.class);
        doReturn(builder).when(meteringGroupService).createQueryEndDeviceGroup(anyVararg());
        doThrow(new VerboseConstraintViolationException(Collections.emptySet())).when(builder).validate();

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.dynamic = true;
        info.name = "NewGroup";

        Response response = target("/devicegroups").queryParam("validate", true).request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
    }

    @Test
    public void testRemoveEndDeviceGroup() {
        when(meteringGroupService.findAndLockEndDeviceGroupByIdAndVersion(111, 1L)).thenReturn(Optional.of(endDeviceGroup));
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(endDeviceGroup));

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.version = 1L;
        info.id = 111L;

        Response response = target("/devicegroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(endDeviceGroup).delete();
    }

    @Test
    public void testRemoveNonExistingEndDeviceGroup() {
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.empty());
        when(meteringGroupService.findAndLockEndDeviceGroupByIdAndVersion(111, 1L)).thenReturn(Optional.empty());

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.version = 1L;
        info.id = 111L;

        Response response = target("/devicegroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testVetoingHandlerPreventsDeletion() {
        when(meteringGroupService.findAndLockEndDeviceGroupByIdAndVersion(111, 1L)).thenReturn(Optional.of(endDeviceGroup));
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(endDeviceGroup));

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.version = 1L;
        info.id = 111L;

        doThrow(new LocalizedException(thesaurus, MessageSeeds.NO_SUCH_MESSAGE) { // Bogus exception, real exception originates in DeviceData.impl
        }).when(endDeviceGroup).delete();

        Response response = target("/devicegroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
    }

    @Test
    public void testGetMembersOfStaticDeviceGroup() {
        EnumeratedEndDeviceGroup endDeviceGroup = mock(EnumeratedEndDeviceGroup.class);
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(endDeviceGroup));
        EndDevice endDevice = mock(EndDevice.class);
        when(endDevice.getAmrId()).thenReturn("1");
        when(endDeviceGroup.getMembers(Matchers.any(Instant.class))).thenReturn(Collections.singletonList(endDevice));
        when(deviceService.findAllDevices(Matchers.any())).thenReturn(finder);
        when(finder.sorted("name", true)).thenReturn(finder);
        List<Device> devices = Arrays.asList(
                mockDevice(1, "001", "Elster AS1440", "Default"),
                mockDevice(2, "002", "Iskra 001", "Default"));
        when(finder.find()).thenReturn(devices);
        when(finder.stream()).thenReturn(Stream.of(devices.get(0), devices.get(1)));

        String response = target("/devicegroups/111/devices").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Integer>>get("$.devices[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.devices[*].name")).containsExactly("Name-1", "Name-2");
        assertThat(jsonModel.<List<String>>get("$.devices[*].serialNumber")).containsExactly("001", "002");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceTypeName")).containsExactly("Elster AS1440", "Iskra 001");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceConfigurationName")).containsExactly("Default", "Default");
    }

    @Test
    public void testGetMembersOfDynamicDeviceGroup() {
        QueryEndDeviceGroup queryEndDeviceGroup = mock(QueryEndDeviceGroup.class);
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(queryEndDeviceGroup));
        when(queryEndDeviceGroup.isDynamic()).thenReturn(true);
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));
        doReturn(finder).when(searchDomain).finderFor(Matchers.any());

        List<Device> devices = Arrays.asList(
                mockDevice(1, "001", "Elster AS1440", "Default"),
                mockDevice(2, "002", "Iskra 001", "Default"));
        when(finder.from(Matchers.any())).thenReturn(finder);
        when(finder.find()).thenReturn(devices);
        when(finder.stream()).thenReturn(Stream.of(devices.get(0), devices.get(1)));

        String response = target("/devicegroups/111/devices").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Integer>>get("$.devices[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.devices[*].name")).containsExactly("Name-1", "Name-2");
        assertThat(jsonModel.<List<String>>get("$.devices[*].serialNumber")).containsExactly("001", "002");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceTypeName")).containsExactly("Elster AS1440", "Iskra 001");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceConfigurationName")).containsExactly("Default", "Default");
    }

    @Test
    public void testEditEnumeratedDeviceGroup() {
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        EndDevice ed_1 = mockEndDevice(amrSystem, 1);
        EndDevice ed_2 = mockEndDevice(amrSystem, 2);
        EndDevice ed_3 = mockEndDevice(amrSystem, 3);

        long DEVICE_GROUP_ID = 13;
        long DEVICE_GROUP_VERSION = 15;
        String DEVICE_GROUP_NAME = "new name";
        EnumeratedEndDeviceGroup endDeviceGroup = mock(EnumeratedEndDeviceGroup.class);
        when(meteringGroupService.findAndLockEndDeviceGroupByIdAndVersion(DEVICE_GROUP_ID, DEVICE_GROUP_VERSION)).thenReturn(Optional.of(endDeviceGroup));
        EnumeratedEndDeviceGroup.Entry<EndDevice> entry = mock(EnumeratedEndDeviceGroup.Entry.class);
        when(entry.getMember()).thenReturn(ed_1);
        doReturn(Collections.singletonList(entry)).when(endDeviceGroup).getEntries();

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.id = DEVICE_GROUP_ID;
        info.version = DEVICE_GROUP_VERSION;
        info.name = DEVICE_GROUP_NAME;
        info.dynamic = false;
        info.devices = Arrays.asList(2L, 3L);

        // Business method
        Response response = target("/devicegroups/" + DEVICE_GROUP_ID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(endDeviceGroup).setName(DEVICE_GROUP_NAME);
        verify(endDeviceGroup).setMRID("MDC:" + DEVICE_GROUP_NAME);
        verify(endDeviceGroup).remove(entry);
        verify(endDeviceGroup).add(ed_2, Range.atLeast(Instant.EPOCH));
        verify(endDeviceGroup).add(ed_3, Range.atLeast(Instant.EPOCH));
        verify(endDeviceGroup).update();
    }

    @Test
    public void testEditQueryDeviceGroupNoSearchCriterion() throws IOException {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(Device.class.getName());
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));

        long DEVICE_GROUP_ID = 13;
        long DEVICE_GROUP_VERSION = 15;
        String DEVICE_GROUP_NAME = "new name";
        EnumeratedEndDeviceGroup endDeviceGroup = mock(EnumeratedEndDeviceGroup.class);
        when(meteringGroupService.findAndLockEndDeviceGroupByIdAndVersion(DEVICE_GROUP_ID, DEVICE_GROUP_VERSION)).thenReturn(Optional.of(endDeviceGroup));

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.id = DEVICE_GROUP_ID;
        info.version = DEVICE_GROUP_VERSION;
        info.name = DEVICE_GROUP_NAME;
        info.dynamic = false;
        info.filter = "[]";

        // Business method
        Response response = target("/devicegroups/" + DEVICE_GROUP_ID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("At least one search criterion has to be provided");
    }

    @Test
    public void testEditQueryDeviceGroup() throws IOException {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(Device.class.getName());
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));

        long DEVICE_GROUP_ID = 13;
        long DEVICE_GROUP_VERSION = 15;
        String DEVICE_GROUP_NAME = "new name";
        QueryEndDeviceGroup endDeviceGroup = mock(QueryEndDeviceGroup.class);
        when(meteringGroupService.findAndLockEndDeviceGroupByIdAndVersion(DEVICE_GROUP_ID, DEVICE_GROUP_VERSION)).thenReturn(Optional.of(endDeviceGroup));

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.id = DEVICE_GROUP_ID;
        info.version = DEVICE_GROUP_VERSION;
        info.name = DEVICE_GROUP_NAME;
        info.dynamic = true;
        info.filter = ALL_FILTER;

        // Business method
        Response response = target("/devicegroups/" + DEVICE_GROUP_ID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(endDeviceGroup).setName(DEVICE_GROUP_NAME);
        verify(endDeviceGroup).setMRID("MDC:" + DEVICE_GROUP_NAME);
        verify(endDeviceGroup).setConditions(eq(Collections.emptyList()));
        verify(endDeviceGroup).update();
    }

    private EndDevice mockEndDevice(AmrSystem amrSystem, long id) {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(id);
        when(amrSystem.findMeter("" + id)).thenReturn(Optional.of(meter));
        return meter;
    }

    private Device mockDevice(long id, String serialNumber, String deviceType, String deviceConfig) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(id);
        when(device.getName()).thenReturn("Name-" + id);
        when(device.getSerialNumber()).thenReturn(serialNumber);
        DeviceType type = mock(DeviceType.class);
        when(device.getDeviceType()).thenReturn(type);
        when(type.getName()).thenReturn(deviceType);
        DeviceConfiguration config = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(config);
        when(config.getName()).thenReturn(deviceConfig);
        return device;
    }
}
