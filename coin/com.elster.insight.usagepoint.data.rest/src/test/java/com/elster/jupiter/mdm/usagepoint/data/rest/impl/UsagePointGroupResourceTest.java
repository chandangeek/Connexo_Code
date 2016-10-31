package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.rest.util.StatusCode;
import com.elster.jupiter.search.SearchDomain;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointGroupResourceTest extends UsagePointDataRestApplicationJerseyTest {

    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private Finder<UsagePoint> finder;
    @Mock
    private Query<UsagePointGroup> usagePointGroupQuery;

    @Test
    public void testGetQueryUsagePointGroup() throws Exception {
        when(meteringGroupsService.getQueryUsagePointGroupQuery()).thenReturn(usagePointGroupQuery);
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(13L);
        when(usagePointGroup.getName()).thenReturn("South region");
        when(usagePointGroup.getMRID()).thenReturn("LAPOPKLQKS");
        when(usagePointGroup.isDynamic()).thenReturn(false);
        List<UsagePointGroup> usagePointGroups = Collections.singletonList(usagePointGroup);
        when(usagePointGroupQuery.select(anyObject(), anyObject())).thenReturn(usagePointGroups);

        String response = target("/usagepointgroups").queryParam("type", "QueryUsagePointGroup").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.usagepointgroups[0].name")).isEqualTo("South region");
        assertThat(jsonModel.<String>get("$.usagepointgroups[0].mRID")).isEqualTo("LAPOPKLQKS");
        assertThat(jsonModel.<Integer>get("$.usagepointgroups[0].id")).isEqualTo(13);
    }

    @Test
    public void testGetUsagePointGroupQueryWithFilter() throws Exception {
        when(meteringGroupsService.getUsagePointGroupQuery()).thenReturn(usagePointGroupQuery);
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(13L);
        when(usagePointGroup.getName()).thenReturn("South region");
        List<UsagePointGroup> usagePointGroups = Collections.singletonList(usagePointGroup);
        when(usagePointGroupQuery.select(any(), eq(1), eq(11), any())).thenReturn(usagePointGroups);

        String response = target("/usagepointgroups").queryParam("filter", ExtjsFilter.filter("name", "South region"))
                .queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.usagepointgroups[0].name")).isEqualTo("South region");
    }

    @Test
    public void testGetUsagePointGroup() throws Exception {
        when(meteringGroupsService.getUsagePointGroupQuery()).thenReturn(usagePointGroupQuery);
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(13L);
        when(usagePointGroup.getName()).thenReturn("south region");
        when(usagePointGroup.getMRID()).thenReturn("ABC");
        when(usagePointGroup.getType()).thenReturn("UsagePointGroup");
        when(usagePointGroup.isDynamic()).thenReturn(false);
        List<UsagePointGroup> usagePointGroups = Collections.singletonList(usagePointGroup);
        when(usagePointGroupQuery.select(anyObject(), anyObject())).thenReturn(usagePointGroups);

        String response = target("/usagepointgroups").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
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
                mockUsagePoint(1, ServiceKind.ELECTRICITY, null, true, true, ConnectionState.UNDER_CONSTRUCTION, "Cosmos"),
                mockUsagePoint(2, ServiceKind.HEAT, "LivingHeatProsumer", true, false, ConnectionState.CONNECTED, "Earth"));
        when(usagePointGroup.getMembers(any(Instant.class))).thenReturn(usagePoints);

        String response = target("/usagepointgroups/111/usagepoints").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Integer>>get("$.usagePoints[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].mRID")).containsExactly("MRID1", "MRID2");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayServiceCategory")).containsExactly("Electricity", "Heat");
        assertThat(jsonModel.<String>get("$.usagePoints[0].displayMetrologyConfiguration")).isNull();
        assertThat(jsonModel.<String>get("$.usagePoints[1].displayMetrologyConfiguration")).isEqualTo("LivingHeatProsumer");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayType")).containsExactly("Unmeasured SDP", "Measured SDP");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayConnectionState")).containsExactly("Under construction", "Connected");
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
                mockUsagePoint(1, ServiceKind.ELECTRICITY, null, true, true, ConnectionState.UNDER_CONSTRUCTION, "Cosmos"),
                mockUsagePoint(2, ServiceKind.HEAT, "LivingHeatProsumer", true, false, ConnectionState.CONNECTED, "Earth"));
        when(finder.from(any())).thenReturn(finder);
        when(finder.stream()).thenReturn(usagePoints.stream());

        String response = target("/usagepointgroups/111/usagepoints").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Integer>>get("$.usagePoints[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].mRID")).containsExactly("MRID1", "MRID2");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayServiceCategory")).containsExactly("Electricity", "Heat");
        assertThat(jsonModel.<String>get("$.usagePoints[0].displayMetrologyConfiguration")).isNull();
        assertThat(jsonModel.<String>get("$.usagePoints[1].displayMetrologyConfiguration")).isEqualTo("LivingHeatProsumer");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayType")).containsExactly("Unmeasured SDP", "Measured SDP");
        assertThat(jsonModel.<List<String>>get("$.usagePoints[*].displayConnectionState")).containsExactly("Under construction", "Connected");
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

    private static UsagePoint mockUsagePoint(long id, ServiceKind serviceKind, String metrologyConfig,
                                      boolean isSdp, boolean isVirtual, ConnectionState connectionState,
                                      String location) {
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(id);
        when(usagePoint.getMRID()).thenReturn("MRID" + id);
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
        when(usagePoint.getConnectionStateDisplayName()).thenReturn(connectionState.getDefaultFormat());
        Optional<Location> locationOptional = Optional.ofNullable(location).map(UsagePointGroupResourceTest::mockLocation);
        when(usagePoint.getLocation()).thenReturn(locationOptional);
        when(usagePoint.getSpatialCoordinates()).thenReturn(Optional.empty());
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
