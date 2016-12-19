package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.GroupBuilder;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.LocalizedException;
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
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.ExecutionTimer;

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
import org.mockito.Mock;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
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

public class UsagePointGroupResourceTest extends UsagePointDataRestApplicationJerseyTest {

    private static final String ALL_FILTER = JsonModel.model(ImmutableList.of(ImmutableMap.of(
            "property", "name",
            "value", ImmutableList.of(
                    ImmutableMap.of(
                            "operator", "==",
                            "criteria", "*"
                    )
            )))).toJson();
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private Finder<UsagePoint> finder;
    @Mock
    private SearchBuilder<UsagePoint> searchBuilder;
    @Mock
    private Query<UsagePointGroup> usagePointGroupQuery;
    @Captor
    private ArgumentCaptor<List<SearchablePropertyCondition>> searchablePropertyConditionsCaptor;

    @Test
    public void testGetQueryUsagePointGroup() throws Exception {
        when(meteringGroupsService.getQueryUsagePointGroupQuery()).thenReturn(usagePointGroupQuery);
        QueryUsagePointGroup usagePointGroup = mock(QueryUsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(13L);
        when(usagePointGroup.getName()).thenReturn("South region");
        when(usagePointGroup.getMRID()).thenReturn("LAPOPKLQKS");
        when(usagePointGroup.isDynamic()).thenReturn(true);
        List<UsagePointGroup> usagePointGroups = Collections.singletonList(usagePointGroup);
        when(usagePointGroupQuery.select(anyObject(), anyObject())).thenReturn(usagePointGroups);

        String response = target("/usagepointgroups").queryParam("type", "QueryUsagePointGroup").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.usagePointGroups[0].name")).isEqualTo("South region");
        assertThat(jsonModel.<String>get("$.usagePointGroups[0].mRID")).isEqualTo("LAPOPKLQKS");
        assertThat(jsonModel.<Integer>get("$.usagePointGroups[0].id")).isEqualTo(13);
        assertThat(jsonModel.<Boolean>get("$.usagePointGroups[0].dynamic")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.usagePointGroups[0].filter")).isEqualTo("[]");
    }

    @Test
    public void testGetUsagePointGroupQueryWithFilter() throws Exception {
        when(meteringGroupsService.getUsagePointGroupQuery()).thenReturn(usagePointGroupQuery);
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(13L);
        when(usagePointGroup.getName()).thenReturn("South region");
        List<UsagePointGroup> usagePointGroups = Collections.singletonList(usagePointGroup);
        when(usagePointGroupQuery.select(any(Condition.class), anyInt(), anyInt(), any())).thenReturn(usagePointGroups);

        String response = target("/usagepointgroups").queryParam("filter", ExtjsFilter.filter("name", "South region"))
                .queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        ArgumentCaptor<Condition> captor = ArgumentCaptor.forClass(Condition.class);
        verify(usagePointGroupQuery).select(captor.capture(), eq(1), eq(11), any());
        Condition actual = captor.getValue();
        assertThat(actual).isInstanceOf(Comparison.class);
        assertThat(actual.toString()).isEqualTo(where("name").isEqualTo("South region").toString());
        assertThat(((Comparison) actual).getValues()).containsExactly("South+region");

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.usagePointGroups[0].name")).isEqualTo("South region");
    }

    @Test
    public void testGetUsagePointGroup() throws Exception {
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(13L);
        when(usagePointGroup.getName()).thenReturn("south region");
        when(usagePointGroup.getMRID()).thenReturn("ABC");
        when(usagePointGroup.getVersion()).thenReturn(3L);
        when(usagePointGroup.isDynamic()).thenReturn(false);
        when(meteringGroupsService.findUsagePointGroup(13L)).thenReturn(Optional.of(usagePointGroup));

        String response = target("/usagepointgroups/13").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("south region");
        assertThat(jsonModel.<String>get("$.mRID")).isEqualTo("ABC");
        assertThat(jsonModel.<Boolean>get("$.dynamic")).isEqualTo(false);
        assertThat(jsonModel.<Integer>get("$.version")).isEqualTo(3);
    }

    @Test
    public void testCreateQueryUsagePointGroupWithoutSearchDomain() throws IOException {
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.empty());
        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.name = "NewQueryGroup";
        info.dynamic = true;
        info.filter = "[]";

        Response response = target("/usagepointgroups").request().post(Entity.json(info));
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Usage point search domain is not registered");
    }

    @Test
    public void testCreateQueryUsagePointGroupWithoutFilters() throws IOException {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(UsagePoint.class.getName());
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.of(searchDomain));
        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.name = "NewQueryGroup";
        info.dynamic = true;
        info.filter = "[]";

        Response response = target("/usagepointgroups").request().post(Entity.json(info));
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("At least one search criterion has to be provided");
    }

    @Test
    public void testCreateQueryUsagePointGroup() throws IOException {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(UsagePoint.class.getName());
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.of(searchDomain));
        SearchableProperty nameSearchableProperty = mock(SearchableProperty.class);
        when(nameSearchableProperty.getName()).thenReturn("name");
        when(searchDomain.getPropertiesValues(any())).thenAnswer(invocationOnMock -> Collections.singletonList(
                ((Function<SearchableProperty, SearchablePropertyValue>) invocationOnMock.getArguments()[0])
                        .apply(nameSearchableProperty)));

        QueryUsagePointGroup group = mock(QueryUsagePointGroup.class);
        when(group.getId()).thenReturn(133L);
        GroupBuilder.QueryGroupBuilder<UsagePoint, QueryUsagePointGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.QueryGroupBuilder.class);
        doReturn(builder).when(meteringGroupsService).createQueryUsagePointGroup(anyVararg());

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.name = "NewQueryGroup";
        info.dynamic = true;
        info.filter = ALL_FILTER;

        String response = target("/usagepointgroups").request().post(Entity.json(info), String.class);

        ArgumentCaptor<SearchablePropertyValue> captor = ArgumentCaptor.forClass(SearchablePropertyValue.class);
        verify(meteringGroupsService).createQueryUsagePointGroup(captor.capture());
        SearchablePropertyValue searchablePropertyValue = captor.getValue();
        assertThat(searchablePropertyValue.getProperty()).isEqualTo(nameSearchableProperty);
        assertThat(searchablePropertyValue.getValueBean().propertyName).isEqualTo("name");
        assertThat(searchablePropertyValue.getValueBean().operator).isEqualTo(SearchablePropertyOperator.EQUAL);
        assertThat(searchablePropertyValue.getValueBean().values).containsExactly("*");
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDM:" + info.name);
        verify(builder).setSearchDomain(searchDomain);
        verify(builder).setQueryProviderName(anyString());
        verify(builder).setLabel("MDM");
        verify(builder).create();

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(133);
    }

    @Test
    public void testCreateEnumeratedUsagePointGroupFromFilter() throws Exception {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(UsagePoint.class.getName());
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.of(searchDomain));
        SearchMonitor dummyMonitor = mock(SearchMonitor.class);
        ExecutionTimer timer = mock(ExecutionTimer.class);
        doAnswer(invocation -> ((Callable) invocation.getArguments()[0]).call()).when(timer).time(any(Callable.class));
        when(dummyMonitor.searchTimer(searchDomain)).thenReturn(timer);

        when(searchService.search(UsagePoint.class)).thenReturn(new SearchBuilderImpl<>(searchDomain, dummyMonitor));
        when(usagePoint.getId()).thenReturn(31L);
        when(meteringService.findUsagePointById(31)).thenReturn(Optional.of(usagePoint));
        SearchableProperty nameSearchableProperty = mock(SearchableProperty.class, Answers.RETURNS_DEEP_STUBS.get());
        when(nameSearchableProperty.getName()).thenReturn("name");
        when(nameSearchableProperty.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.SINGLE);
        when(nameSearchableProperty.getSpecification().getValueFactory()).thenReturn(new StringFactory());
        when(searchDomain.getPropertiesValues(any())).thenAnswer(invocationOnMock -> Collections.singletonList(
                ((Function<SearchableProperty, SearchablePropertyValue>) invocationOnMock.getArguments()[0])
                        .apply(nameSearchableProperty)));
        doReturn(finder).when(searchDomain).finderFor(anyListOf(SearchablePropertyCondition.class));
        when(finder.find()).thenReturn(Collections.singletonList(usagePoint));
        when(finder.stream()).thenReturn(Stream.of(usagePoint));

        EnumeratedUsagePointGroup group = mock(EnumeratedUsagePointGroup.class);
        when(group.getId()).thenReturn(133L);
        GroupBuilder.EnumeratedGroupBuilder<UsagePoint, EnumeratedUsagePointGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.EnumeratedGroupBuilder.class);
        doReturn(builder).when(meteringGroupsService).createEnumeratedUsagePointGroup(anyVararg());

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.name = "NewEnumGroup";
        info.dynamic = false;
        info.filter = ALL_FILTER;

        String response = target("/usagepointgroups").request().post(Entity.json(info), String.class);

        verify(searchDomain).finderFor(searchablePropertyConditionsCaptor.capture());
        List<SearchablePropertyCondition> conditions = searchablePropertyConditionsCaptor.getValue();
        assertThat(conditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = conditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(nameSearchableProperty);
        Condition condition = searchablePropertyCondition.getCondition();
        assertThat(condition).isInstanceOf(Comparison.class);
        assertThat(((Comparison) condition).getOperator()).isEqualTo(Operator.LIKEIGNORECASE);
        assertThat(((Comparison) condition).getValues()).containsExactly("%");

        verify(meteringGroupsService).createEnumeratedUsagePointGroup(usagePoint);
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDM:" + info.name);
        verify(builder).setLabel("MDM");
        verify(builder).create();

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(133);
    }

    @Test
    public void testCreateEnumeratedUsagePointGroupFromList() throws Exception {
        when(usagePoint.getId()).thenReturn(31L);
        when(meteringService.findUsagePointById(31)).thenReturn(Optional.of(usagePoint));

        EnumeratedUsagePointGroup group = mock(EnumeratedUsagePointGroup.class);
        when(group.getId()).thenReturn(133L);
        GroupBuilder.EnumeratedGroupBuilder<UsagePoint, EnumeratedUsagePointGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.EnumeratedGroupBuilder.class);
        doReturn(builder).when(meteringGroupsService).createEnumeratedUsagePointGroup(anyVararg());

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.name = "NewEnumGroup";
        info.dynamic = false;
        info.filter = ALL_FILTER;
        info.usagePoints = Collections.singletonList(31L);

        String response = target("/usagepointgroups").request().post(Entity.json(info), String.class);

        verify(meteringGroupsService).createEnumeratedUsagePointGroup(usagePoint);
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDM:" + info.name);
        verify(builder).setLabel("MDM");
        verify(builder).create();

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(133);
    }

    @Test
    public void testValidateQueryUsagePointGroupCreation() throws Exception {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(UsagePoint.class.getName());
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.of(searchDomain));

        QueryUsagePointGroup group = mock(QueryUsagePointGroup.class);
        GroupBuilder.QueryGroupBuilder<UsagePoint, QueryUsagePointGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.QueryGroupBuilder.class);
        doReturn(builder).when(meteringGroupsService).createQueryUsagePointGroup(anyVararg());

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.name = "NewQueryGroup";
        info.dynamic = true;

        Response response = target("/usagepointgroups").queryParam("validate", true).request().post(Entity.json(info));

        verify(meteringGroupsService).createQueryUsagePointGroup(anyVararg());
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDM:" + info.name);
        verify(builder).setLabel("MDM");
        verify(builder).setSearchDomain(searchDomain);
        verify(builder).setQueryProviderName(anyString());
        verify(builder).validate();
        verify(builder, never()).create();

        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    public void testValidateEnumeratedUsagePointGroupCreation() throws Exception {
        EnumeratedUsagePointGroup group = mock(EnumeratedUsagePointGroup.class);
        GroupBuilder.EnumeratedGroupBuilder<UsagePoint, EnumeratedUsagePointGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.EnumeratedGroupBuilder.class);
        doReturn(builder).when(meteringGroupsService).createEnumeratedUsagePointGroup(anyVararg());

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.name = "NewEnumeratedGroup";
        info.dynamic = false;

        Response response = target("/usagepointgroups").queryParam("validate", true).request().post(Entity.json(info));

        verify(meteringGroupsService).createEnumeratedUsagePointGroup(anyVararg());
        verify(builder).setName(info.name);
        verify(builder).setMRID("MDM:" + info.name);
        verify(builder).setLabel("MDM");
        verify(builder).validate();
        verify(builder, never()).create();

        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    public void testValidationFailsNoName() throws IOException {
        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.dynamic = true;

        Response response = target("/usagepointgroups").queryParam("validate", true).request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<List<String>>get("$.errors[*].id")).containsExactly("name");
        assertThat(jsonModel.<List<String>>get("$.errors[*].msg")).containsExactly("This field is required");
    }

    @Test
    public void testValidationFailsEmptyName() throws IOException {
        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.dynamic = true;
        info.name = "  ";

        Response response = target("/usagepointgroups").queryParam("validate", true).request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<List<String>>get("$.errors[*].id")).containsExactly("name");
        assertThat(jsonModel.<List<String>>get("$.errors[*].msg")).containsExactly("This field is required");
    }

    @Test
    public void testValidationFailsNoSearchDomain() throws IOException {
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.empty());

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.dynamic = true;
        info.name = "NewGroup";

        Response response = target("/usagepointgroups").queryParam("validate", true).request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Usage point search domain is not registered");
    }

    @Test
    public void testValidationFails() throws IOException {
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(UsagePoint.class.getName());
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.of(searchDomain));

        QueryUsagePointGroup group = mock(QueryUsagePointGroup.class);
        GroupBuilder.QueryGroupBuilder<UsagePoint, QueryUsagePointGroup> builder = FakeBuilder
                .initBuilderStub(group, GroupBuilder.QueryGroupBuilder.class);
        doReturn(builder).when(meteringGroupsService).createQueryUsagePointGroup(anyVararg());
        doThrow(new VerboseConstraintViolationException(Collections.emptySet())).when(builder).validate();

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.dynamic = true;
        info.name = "NewGroup";

        Response response = target("/usagepointgroups").queryParam("validate", true).request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
    }

    @Test
    public void testRemoveUsagePointGroup() {
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(111, 1L)).thenReturn(Optional.of(usagePointGroup));
        when(meteringGroupsService.findUsagePointGroup(111)).thenReturn(Optional.of(usagePointGroup));

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.version = 1L;
        info.id = 111L;

        Response response = target("/usagepointgroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(usagePointGroup).delete();
    }

    @Test
    public void testRemoveNonExistingUsagePointGroup() {
        when(meteringGroupsService.findUsagePointGroup(111)).thenReturn(Optional.empty());
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(111, 1L)).thenReturn(Optional.empty());

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.version = 1L;
        info.id = 111L;

        Response response = target("/usagepointgroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testVetoingHandlerPreventsDeletion() {
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(111, 1L)).thenReturn(Optional.of(usagePointGroup));
        when(meteringGroupsService.findUsagePointGroup(111)).thenReturn(Optional.of(usagePointGroup));

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.version = 1L;
        info.id = 111L;

        doThrow(new LocalizedException(thesaurus, MessageSeeds.BAD_REQUEST) {
            // Bogus exception, real exception originates in subscribers
            // of com.elster.jupiter.metering.groups.EventType.USAGEPOINTGROUP_VALIDATE_DELETED
        }).when(usagePointGroup).delete();

        Response response = target("/usagepointgroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
    }

    @Test
    public void testGetMembersOfStaticUsagePointGroup() {
        EnumeratedUsagePointGroup usagePointGroup = mock(EnumeratedUsagePointGroup.class);
        when(meteringGroupsService.findUsagePointGroup(111)).thenReturn(Optional.of(usagePointGroup));
        List<UsagePoint> usagePoints = Arrays.asList(
                mockUsagePoint(1, ServiceKind.ELECTRICITY, null, true, true, ConnectionState.LOGICALLY_DISCONNECTED, "Cosmos"),
                mockUsagePoint(2, ServiceKind.HEAT, "LivingHeatProsumer", true, false, ConnectionState.CONNECTED, "Earth"));
        when(usagePointGroup.getMembers(any(Instant.class))).thenReturn(usagePoints);

        String response = target("/usagepointgroups/111/usagepoints").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Integer>>get("$.usagePoints[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].name")).containsExactly("Name1", "Name2");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayServiceCategory")).containsExactly("Electricity", "Heat");
        assertThat(jsonModel.<String>get("$.usagePoints[0].displayMetrologyConfiguration")).isNull();
        assertThat(jsonModel.<String>get("$.usagePoints[1].displayMetrologyConfiguration")).isEqualTo("LivingHeatProsumer");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayType")).containsExactly("Virtual SDP", "Physical SDP");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayConnectionState")).containsExactly("Logically disconnected", "Connected");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].location")).containsExactly("Cosmos", "Earth");
    }

    @Test
    public void testGetMembersOfDynamicUsagePointGroup() {
        QueryUsagePointGroup queryUsagePointGroup = mock(QueryUsagePointGroup.class);
        when(meteringGroupsService.findUsagePointGroup(111)).thenReturn(Optional.of(queryUsagePointGroup));
        when(queryUsagePointGroup.isDynamic()).thenReturn(true);
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.of(searchDomain));
        doReturn(finder).when(searchDomain).finderFor(any());

        List<UsagePoint> usagePoints = Arrays.asList(
                mockUsagePoint(1, ServiceKind.ELECTRICITY, null, true, true, ConnectionState.LOGICALLY_DISCONNECTED, "Cosmos"),
                mockUsagePoint(2, ServiceKind.HEAT, "LivingHeatProsumer", true, false, ConnectionState.CONNECTED, "Earth"));
        when(finder.from(any())).thenReturn(finder);
        when(finder.stream()).thenReturn(usagePoints.stream());

        String response = target("/usagepointgroups/111/usagepoints").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Integer>>get("$.usagePoints[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].name")).containsExactly("Name1", "Name2");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayServiceCategory")).containsExactly("Electricity", "Heat");
        assertThat(jsonModel.<String>get("$.usagePoints[0].displayMetrologyConfiguration")).isNull();
        assertThat(jsonModel.<String>get("$.usagePoints[1].displayMetrologyConfiguration")).isEqualTo("LivingHeatProsumer");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayType")).containsExactly("Virtual SDP", "Physical SDP");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayConnectionState")).containsExactly("Logically disconnected", "Connected");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].location")).containsExactly("Cosmos", "Earth");
    }

    @Test
    public void testGetUsagePointsCount() {
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(meteringGroupsService.findUsagePointGroup(111)).thenReturn(Optional.of(usagePointGroup));
        when(usagePointGroup.getMemberCount(any(Instant.class))).thenReturn(42L);

        String response = target("/usagepointgroups/111/usagepoints/count").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.numberOfSearchResults")).isEqualTo(42);
    }

    @Test
    public void testEditEnumeratedUsagePointGroup() {
        UsagePoint up_1 = mockUsagePoint(1);
        UsagePoint up_2 = mockUsagePoint(2);
        UsagePoint up_3 = mockUsagePoint(3);

        long USAGEPOINT_GROUP_ID = 13;
        long USAGEPOINT_GROUP_VERSION = 15;
        String USAGEPOINT_GROUP_NAME = "new name";
        EnumeratedUsagePointGroup usagePointGroup = mock(EnumeratedUsagePointGroup.class);
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(USAGEPOINT_GROUP_ID, USAGEPOINT_GROUP_VERSION)).thenReturn(Optional.of(usagePointGroup));
        EnumeratedUsagePointGroup.Entry<UsagePoint> entry = mock(EnumeratedUsagePointGroup.Entry.class);
        when(entry.getMember()).thenReturn(up_1);
        doReturn(Collections.singletonList(entry)).when(usagePointGroup).getEntries();

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.id = USAGEPOINT_GROUP_ID;
        info.version = USAGEPOINT_GROUP_VERSION;
        info.name = USAGEPOINT_GROUP_NAME;
        info.dynamic = false;
        info.usagePoints = Arrays.asList(2L, 3L);

        // Business method
        Response response = target("/usagepointgroups/" + USAGEPOINT_GROUP_ID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(usagePointGroup).setName(USAGEPOINT_GROUP_NAME);
        verify(usagePointGroup).setMRID("MDM:" + USAGEPOINT_GROUP_NAME);
        verify(usagePointGroup).remove(entry);
        verify(usagePointGroup).add(up_2, Range.atLeast(Instant.EPOCH));
        verify(usagePointGroup).add(up_3, Range.atLeast(Instant.EPOCH));
        verify(usagePointGroup).update();
    }

    @Test
    public void testEditQueryUsagePointGroupNoSearchCriterion() throws IOException {
        long USAGEPOINT_GROUP_ID = 13;
        long USAGEPOINT_GROUP_VERSION = 15;
        String USAGEPOINT_GROUP_NAME = "new name";
        QueryUsagePointGroup usagePointGroup = mock(QueryUsagePointGroup.class);
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(USAGEPOINT_GROUP_ID, USAGEPOINT_GROUP_VERSION)).thenReturn(Optional.of(usagePointGroup));
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(UsagePoint.class.getName());
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.of(searchDomain));

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.id = USAGEPOINT_GROUP_ID;
        info.version = USAGEPOINT_GROUP_VERSION;
        info.name = USAGEPOINT_GROUP_NAME;
        info.dynamic = true;
        info.filter = "[]";

        // Business method
        Response response = target("/usagepointgroups/" + USAGEPOINT_GROUP_ID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("At least one search criterion has to be provided");
    }

    @Test
    public void testEditQueryUsagePointGroup() throws IOException {
        long USAGEPOINT_GROUP_ID = 13;
        long USAGEPOINT_GROUP_VERSION = 15;
        String USAGEPOINT_GROUP_NAME = "new name";
        QueryUsagePointGroup usagePointGroup = mock(QueryUsagePointGroup.class);
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(USAGEPOINT_GROUP_ID, USAGEPOINT_GROUP_VERSION)).thenReturn(Optional.of(usagePointGroup));
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(UsagePoint.class.getName());
        when(searchService.findDomain(UsagePoint.class.getName())).thenReturn(Optional.of(searchDomain));

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.id = USAGEPOINT_GROUP_ID;
        info.version = USAGEPOINT_GROUP_VERSION;
        info.name = USAGEPOINT_GROUP_NAME;
        info.dynamic = true;
        info.filter = ALL_FILTER;

        // Business method
        Response response = target("/usagepointgroups/" + USAGEPOINT_GROUP_ID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(usagePointGroup).setName(USAGEPOINT_GROUP_NAME);
        verify(usagePointGroup).setMRID("MDM:" + USAGEPOINT_GROUP_NAME);
        verify(usagePointGroup).setConditions(eq(Collections.emptyList()));
        verify(usagePointGroup).update();
    }

    private UsagePoint mockUsagePoint(long id) {
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(id);
        when(meteringService.findUsagePointById(id)).thenReturn(Optional.of(usagePoint));
        return usagePoint;
    }

    private static UsagePoint mockUsagePoint(long id, ServiceKind serviceKind, String metrologyConfig,
                                             boolean isSdp, boolean isVirtual, ConnectionState connectionState,
                                             String location) {
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(id);
        when(usagePoint.getName()).thenReturn("Name" + id);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getDisplayName()).thenReturn(serviceKind.getDefaultFormat());
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMCOptional = Optional.ofNullable(metrologyConfig)
                .map(UsagePointGroupResourceTest::mockUsagePointMetrologyConfiguration);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration())
                .thenReturn(effectiveMCOptional);
        when(usagePoint.isSdp()).thenReturn(isSdp);
        when(usagePoint.isVirtual()).thenReturn(isVirtual);
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.of(connectionState));
        when(usagePoint.getConnectionStateDisplayName()).thenReturn(connectionState.getDefaultFormat());
        Optional<Location> locationOptional = Optional.ofNullable(location).map(UsagePointGroupResourceTest::mockLocation);
        when(usagePoint.getLocation()).thenReturn(locationOptional);
        when(usagePoint.getSpatialCoordinates()).thenReturn(Optional.empty());
        UsagePointState usagePointState = mock(UsagePointState.class);
        when(usagePoint.getState()).thenReturn(usagePointState);
        return usagePoint;
    }

    private static EffectiveMetrologyConfigurationOnUsagePoint mockUsagePointMetrologyConfiguration(String name) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = mock(
                EffectiveMetrologyConfigurationOnUsagePoint.class,
                Answers.RETURNS_DEEP_STUBS.get());
        when(effectiveMC.getMetrologyConfiguration().getName()).thenReturn(name);
        return effectiveMC;
    }

    private static Location mockLocation(String location) {
        Location mock = mock(Location.class);
        when(mock.toString()).thenReturn(location);
        return mock;
    }
}
