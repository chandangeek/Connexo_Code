/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommand;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandCallbackInfo;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandInfo;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointResourceTest extends PlatformPublicApiJerseyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Finder<UsagePoint> finder = mockFinder(Collections.emptyList());
        when(meteringService.getUsagePoints(any())).thenReturn(finder);
    }

    @Test
    public void testAllGetUsagePointsPaged() throws Exception {
        Response response = target("/usagepoints").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetSingleUsagePointWithFields() throws Exception {
        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        when(usagePoint.getId()).thenReturn(31L);
        when(usagePoint.getName()).thenReturn("usage point");

        // Business method
        Response response = target("/usagepoints/" + MRID).queryParam("fields", "id,name,mrid").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.name")).isEqualTo("usage point");
        assertThat(model.<String>get("$.mrid")).isEqualTo(MRID);
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.readRoute")).isNull();
    }

    @Test
    public void testGetSingleUsagePointDetails() throws Exception {
        mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);

        // Business method
        Response response = target("/usagepoints/" + MRID).queryParam("fields", "detail").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isNull();
        assertThat(model.<String>get("$.name")).isNull();
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.readRoute")).isNull();
        assertThat(model.<String>get("$.detail.link.href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/details/" + clock.millis());
    }

    @Test
    public void testGetSingleUsagePointWithLocation() throws Exception {
        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        LocationMember locationMember = mock(LocationMember.class);
        when(locationMember.getAddressDetail()).thenReturn("address detail");
        when(locationMember.getAdministrativeArea()).thenReturn("administrative area");
        when(locationMember.getCountryCode()).thenReturn("country code");
        when(locationMember.getCountryName()).thenReturn("country name");
        when(locationMember.getEstablishmentName()).thenReturn("establishment name");
        when(locationMember.getEstablishmentNumber()).thenReturn("establishment number");
        when(locationMember.getEstablishmentType()).thenReturn("establishment type");
        when(locationMember.getLocale()).thenReturn("locale");
        when(locationMember.getLocality()).thenReturn("locality");
        when(locationMember.getLocationId()).thenReturn(111L);
        when(locationMember.getStreetName()).thenReturn("street name");
        when(locationMember.getStreetType()).thenReturn("street type");
        when(locationMember.getStreetNumber()).thenReturn("321");
        when(locationMember.getSubLocality()).thenReturn("sub locality");
        when(locationMember.getZipCode()).thenReturn("zip code");
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        Location location = mock(Location.class);
        doReturn(Collections.singletonList(locationMember)).when(location).getMembers();
        when(usagePoint.getLocation()).thenReturn(Optional.of(location));

        // Business method
        Response response = target("/usagepoints/" + MRID).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("$.locations[0].addressDetail")).isEqualTo("address detail");
        assertThat(model.<String>get("$.locations[0].administrativeArea")).isEqualTo("administrative area");
        assertThat(model.<String>get("$.locations[0].countryCode")).isEqualTo("country code");
        assertThat(model.<String>get("$.locations[0].countryName")).isEqualTo("country name");
        assertThat(model.<String>get("$.locations[0].establishmentName")).isEqualTo("establishment name");
        assertThat(model.<String>get("$.locations[0].establishmentNumber")).isEqualTo("establishment number");
        assertThat(model.<String>get("$.locations[0].establishmentType")).isEqualTo("establishment type");
        assertThat(model.<String>get("$.locations[0].locale")).isEqualTo("locale");
        assertThat(model.<String>get("$.locations[0].locality")).isEqualTo("locality");
        assertThat(model.<Integer>get("$.locations[0].locationId")).isEqualTo(111);
        assertThat(model.<String>get("$.locations[0].streetName")).isEqualTo("street name");
        assertThat(model.<String>get("$.locations[0].streetType")).isEqualTo("street type");
        assertThat(model.<String>get("$.locations[0].streetNumber")).isEqualTo("321");
        assertThat(model.<String>get("$.locations[0].subLocality")).isEqualTo("sub locality");
        assertThat(model.<String>get("$.locations[0].zipCode")).isEqualTo("zip code");
    }

    @Test
    public void testUpdateUsagePoint() throws Exception {
        UsagePointInfo info = new UsagePointInfo();
        info.serviceKind = ServiceKind.ELECTRICITY;
        info.aliasName = "alias";
        info.version = 2L;
        info.description = "description";
        info.connectionState = new UsagePointConnectionStateInfo();
        info.connectionState.connectionStateId = ConnectionState.CONNECTED.getId();

        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        // Business method
        Response response = target("/usagepoints/" + MRID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(usagePoint).setConnectionState(ConnectionState.CONNECTED, clock.instant());
    }

    @Test
    public void testNoUpdateMetrologyWithIdenticalIds() throws Exception {
        UsagePointInfo info = new UsagePointInfo();
        info.id = 999L;
        info.version = 2L;
        info.metrologyConfiguration = new LinkInfo<>();
        info.metrologyConfiguration.id = 234L;
        info.serviceKind = ServiceKind.ELECTRICITY;

        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(234L, "metro", 1);
        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(usagePoint.getEffectiveMetrologyConfiguration(any())).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        ElectricityDetail electricityDetail = mock(ElectricityDetail.class);
        ElectricityDetailBuilder electricityDetailBuilder = FakeBuilder.initBuilderStub(electricityDetail, ElectricityDetailBuilder.class);
        when(usagePoint.newElectricityDetailBuilder(any())).thenReturn(electricityDetailBuilder);

        // Business method
        Response response = target("/usagepoints/" + MRID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePoint, never()).removeMetrologyConfiguration(clock.instant());
        verify(usagePoint, never()).apply(metrologyConfiguration, clock.instant());
    }

    @Test
    public void testCreateUsagePointWithoutDetails() throws Exception {
        Instant now = Instant.now(clock);
        UsagePointInfo info = new UsagePointInfo();
        info.aliasName = "alias";
        info.description = "desc";
        info.installationTime = now;
        info.serviceLocation = "here";
        info.name = "naam";
        info.outageRegion = "outage";
        info.serviceDeliveryRemark = "remark";
        info.servicePriority = "prio1";
        info.readRoute = "route";
        info.serviceKind = ServiceKind.GAS;

        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(6L);
        when(usagePoint.getMRID()).thenReturn(MRID);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        UsagePointBuilder usagePointBuilder = FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class);
        when(serviceCategory.newUsagePoint(any(), any())).thenReturn(usagePointBuilder);
        when(meteringService.getServiceCategory(ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));

        // Business method
        Response response = target("/usagepoints").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getLocation()).isEqualTo(new URI("http://localhost:9998/usagepoints/" + MRID));
        verify(serviceCategory).newUsagePoint("naam", now);
        verify(usagePointBuilder).withAliasName("alias");
        verify(usagePointBuilder).withDescription("desc");
        verify(usagePointBuilder).withOutageRegion("outage");
        verify(usagePointBuilder).withServiceDeliveryRemark("remark");
        verify(usagePointBuilder).withServicePriority("prio1");
        verify(usagePointBuilder).withServiceLocationString("here");
        verify(usagePointBuilder).withReadRoute("route");
        verify(usagePointBuilder).create();
    }

    @Test
    public void testUsagePointFields() throws Exception {
        Response response = target("/usagepoints").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(20);
        assertThat(model.<List<String>>get("$")).containsOnly(
                "aliasName",
                "description",
                "id",
                "installationTime",
                "link",
                "locations",
                "meterActivations",
                "metrologyConfiguration",
                "mrid",
                "name",
                "outageRegion",
                "readRoute",
                "serviceDeliveryRemark",
                "serviceLocation",
                "servicePriority",
                "version",
                "serviceKind",
                "connectionState",
                "detail"
        );
    }

    @Test
    public void testUsagePointCommand() throws Exception {
        mockCommands();
        UsagePointCommandInfo info = new UsagePointCommandInfo();
        info.command = UsagePointCommand.CONNECT;
        info.httpCallBack = new UsagePointCommandCallbackInfo();
        info.httpCallBack.method = "POST";
        info.httpCallBack.successURL = "http://success";
        info.httpCallBack.partialSuccessURL = "http://successPartial";
        info.httpCallBack.failureURL = "http://fail";

        mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/commands").request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("status")).isEqualTo("FAILED");
        assertThat(model.<String>get("id")).isEqualTo(MRID);
    }
}
