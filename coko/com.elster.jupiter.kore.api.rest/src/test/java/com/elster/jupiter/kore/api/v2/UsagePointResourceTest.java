package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommand;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandCallbackInfo;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandInfo;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValueInfo;
import com.elster.jupiter.rest.api.util.v1.properties.impl.DefaultPropertyValueConverter;
import com.elster.jupiter.util.geo.Elevation;
import com.elster.jupiter.util.geo.Latitude;
import com.elster.jupiter.util.geo.Longitude;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
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
        // Business method
        Response response = target("/usagepoints").queryParam("start", 0).queryParam("limit", 10).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }


    @Test
    public void testUsagePointFields() throws Exception {
        Response response = target("/usagepoints").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(23);
        assertThat(model.<List<String>>get("$")).containsOnly(
                "aliasName",
                "description",
                "id",
                "installationTime",
                "link",
                "location",
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
                "detail",
                "isVirtual",
                "isSdp",
                "coordinates"
        );
    }

    @Test
    public void testGetSingleUsagePointWithFields() throws Exception {
        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        when(usagePoint.getId()).thenReturn(31L);
        when(usagePoint.getName()).thenReturn("usage point");
        when(usagePoint.isVirtual()).thenReturn(true);
        when(usagePoint.isSdp()).thenReturn(false);
        SpatialCoordinates coordinates = mockSpatialCoordinates(10.0, 11.1, 12.2);
        when(usagePoint.getSpatialCoordinates()).thenReturn(Optional.of(coordinates));

        // Business method
        Response response = target("/usagepoints/" + MRID).queryParam("fields", "id,name,mrid,isSdp,isVirtual,coordinates").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.name")).isEqualTo("usage point");
        assertThat(model.<String>get("$.mrid")).isEqualTo(MRID);
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.readRoute")).isNull();
        assertThat(model.<Boolean>get("$.isSdp")).isFalse();
        assertThat(model.<Boolean>get("$.isVirtual")).isTrue();
        assertThat(model.<Number>get("$.coordinates.latitude")).isEqualTo(10.0);
        assertThat(model.<Number>get("$.coordinates.longitude")).isEqualTo(11.1);
        assertThat(model.<Number>get("$.coordinates.elevation")).isEqualTo(12.2);
    }

    private SpatialCoordinates mockSpatialCoordinates(double latitude, double longitude, double elevation) {
        SpatialCoordinates coordinates = new SpatialCoordinates();
        coordinates.setLatitude(new Latitude(BigDecimal.valueOf(latitude)));
        coordinates.setLongitude(new Longitude(BigDecimal.valueOf(longitude)));
        coordinates.setElevation(new Elevation(BigDecimal.valueOf(elevation)));
        return coordinates;
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
        when(locationMember.getStreetName()).thenReturn("street name");
        when(locationMember.getStreetType()).thenReturn("street type");
        when(locationMember.getStreetNumber()).thenReturn("321");
        when(locationMember.getSubLocality()).thenReturn("sub locality");
        when(locationMember.getZipCode()).thenReturn("zip code");
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        Location location = mock(Location.class);
        when(location.getId()).thenReturn(111L);
        when(location.getMember(Locale.US.toString())).thenReturn(Optional.of(locationMember));
        when(usagePoint.getLocation()).thenReturn(Optional.of(location));
        when(threadPrincipalService.getLocale()).thenReturn(Locale.US);

        // Business method
        Response response = target("/usagepoints/" + MRID).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("$.location.addressDetail")).isEqualTo("address detail");
        assertThat(model.<String>get("$.location.administrativeArea")).isEqualTo("administrative area");
        assertThat(model.<String>get("$.location.countryCode")).isEqualTo("country code");
        assertThat(model.<String>get("$.location.countryName")).isEqualTo("country name");
        assertThat(model.<String>get("$.location.establishmentName")).isEqualTo("establishment name");
        assertThat(model.<String>get("$.location.establishmentNumber")).isEqualTo("establishment number");
        assertThat(model.<String>get("$.location.establishmentType")).isEqualTo("establishment type");
        assertThat(model.<String>get("$.location.locale")).isEqualTo("locale");
        assertThat(model.<String>get("$.location.locality")).isEqualTo("locality");
        assertThat(model.<Number>get("$.location.locationId")).isEqualTo(111);
        assertThat(model.<String>get("$.location.streetName")).isEqualTo("street name");
        assertThat(model.<String>get("$.location.streetType")).isEqualTo("street type");
        assertThat(model.<String>get("$.location.streetNumber")).isEqualTo("321");
        assertThat(model.<String>get("$.location.subLocality")).isEqualTo("sub locality");
        assertThat(model.<String>get("$.location.zipCode")).isEqualTo("zip code");
    }

    @Test
    public void testCreateUsagePoint() throws Exception {
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
        info.isSdp = true;
        info.isVirtual = false;
        info.location = new LocationInfo();
        info.location.locationId = 13L;
        info.coordinates = new CoordinatesInfo();
        info.coordinates.latitude = BigDecimal.valueOf(10.1);
        info.coordinates.longitude = BigDecimal.valueOf(11.1);
        info.coordinates.elevation = BigDecimal.valueOf(12.1);

        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(6L);
        when(usagePoint.getMRID()).thenReturn(MRID);
        GasDetailBuilder gasDetailBuilder = FakeBuilder.initBuilderStub(null, GasDetailBuilder.class);
        when(usagePoint.newGasDetailBuilder(now)).thenReturn(gasDetailBuilder);
        UsagePointBuilder usagePointBuilder = FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.newUsagePoint(any(), any())).thenReturn(usagePointBuilder);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.GAS);
        when(meteringService.getServiceCategory(ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getInstallationTime()).thenReturn(now);
        ArgumentCaptor<SpatialCoordinates> coordinatesArgumentCaptor = ArgumentCaptor.forClass(SpatialCoordinates.class);
        when(usagePointBuilder.withGeoCoordinates(coordinatesArgumentCaptor.capture())).thenReturn(usagePointBuilder);
        Location location = mock(Location.class);
        when(locationService.findLocationById(13L)).thenReturn(Optional.of(location));

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
        verify(usagePointBuilder).withIsSdp(true);
        verify(usagePointBuilder).withIsVirtual(false);
        verify(usagePointBuilder).withGeoCoordinates(any());
        verify(usagePointBuilder).withLocation(location);
        SpatialCoordinates coordinates = coordinatesArgumentCaptor.getValue();
        assertThat(coordinates.getLatitude().getValue()).isEqualTo(BigDecimal.valueOf(10.1));
        assertThat(coordinates.getLongitude().getValue()).isEqualTo(BigDecimal.valueOf(11.1));
        assertThat(coordinates.getElevation().getValue()).isEqualTo(BigDecimal.valueOf(12.1));
        verify(usagePointBuilder).create();
        verify(gasDetailBuilder).create();
    }

    @Test
    public void testCreateUsagePointUnknownLocation() throws Exception {
        Instant now = Instant.now(clock);
        UsagePointInfo info = new UsagePointInfo();
        info.installationTime = now;
        info.name = "naam";
        info.serviceKind = ServiceKind.GAS;
        info.location = new LocationInfo();
        info.location.locationId = 13L;

        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(6L);
        when(usagePoint.getMRID()).thenReturn(MRID);
        GasDetailBuilder gasDetailBuilder = FakeBuilder.initBuilderStub(null, GasDetailBuilder.class);
        when(usagePoint.newGasDetailBuilder(now)).thenReturn(gasDetailBuilder);
        UsagePointBuilder usagePointBuilder = FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.newUsagePoint(any(), any())).thenReturn(usagePointBuilder);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.GAS);
        when(meteringService.getServiceCategory(ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getInstallationTime()).thenReturn(now);
        ArgumentCaptor<SpatialCoordinates> coordinatesArgumentCaptor = ArgumentCaptor.forClass(SpatialCoordinates.class);
        when(usagePointBuilder.withGeoCoordinates(coordinatesArgumentCaptor.capture())).thenReturn(usagePointBuilder);
        when(locationService.findLocationById(13L)).thenReturn(Optional.empty());

        // Business method
        Response response = target("/usagepoints").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.NO_SUCH_LOCATION.getKey());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo(thesaurus.getFormat(MessageSeeds.NO_SUCH_LOCATION).format(13L));

        verify(usagePointBuilder, never()).create();
    }

    @Test
    public void testCreateUsagePointWithCAS() {
        final Instant NOW = Instant.now(clock);
        final String PROPERTY_NAME = "property";
        final long CAS_ID = 13L;

        // Mock CAS
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(PROPERTY_NAME);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        CustomPropertySet cps = mock(CustomPropertySet.class);
        when(cps.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(CAS_ID);
        when(rcps.getCustomPropertySet()).thenReturn(cps);
        when(customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)).thenReturn(Collections.singletonList(rcps));
        when(propertyValueInfoService.getConverter(propertySpec)).thenReturn(new DefaultPropertyValueConverter());

        // Mock usage point
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(6L);
        when(usagePoint.getMRID()).thenReturn(MRID);
        GasDetailBuilder gasDetailBuilder = FakeBuilder.initBuilderStub(null, GasDetailBuilder.class);
        when(usagePoint.newGasDetailBuilder(NOW)).thenReturn(gasDetailBuilder);
        UsagePointBuilder usagePointBuilder = FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.newUsagePoint(any(), any())).thenReturn(usagePointBuilder);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.GAS);
        when(meteringService.getServiceCategory(ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getInstallationTime()).thenReturn(NOW);
        ArgumentCaptor<CustomPropertySetValues> cpsValuesCaptor = ArgumentCaptor.forClass(CustomPropertySetValues.class);
        when(usagePointBuilder.addCustomPropertySetValues(eq(rcps), cpsValuesCaptor.capture())).thenReturn(usagePointBuilder);

        UsagePointInfo info = new UsagePointInfo();
        info.name = "name";
        info.installationTime = NOW;
        info.serviceKind = ServiceKind.GAS;
        UsagePointCustomPropertySetInfo casInfo = new UsagePointCustomPropertySetInfo();
        casInfo.id = CAS_ID;
        casInfo.isVersioned = false;
        CustomPropertySetAttributeInfo casAttributeInfo = new CustomPropertySetAttributeInfo();
        casAttributeInfo.key = PROPERTY_NAME;
        casAttributeInfo.propertyValueInfo = new PropertyValueInfo<>("value", null);
        casInfo.properties = Collections.singletonList(casAttributeInfo);
        info.customPropertySets = Collections.singletonList(casInfo);

        // Business method
        Response response = target("/usagepoints").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        CustomPropertySetValues actualValues = cpsValuesCaptor.getValue();
        assertThat(actualValues.getProperty(PROPERTY_NAME)).isEqualTo("value");
        verify(usagePointBuilder).create();
    }

    @Test
    public void testCreateUsagePointWithUnknownCAS() throws IOException {
        final Instant NOW = Instant.now(clock);
        final String PROPERTY_NAME = "property";
        final long CAS_ID = 13L;

        // Mock CAS
        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(15L);
        when(customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)).thenReturn(Collections.singletonList(rcps));

        // Mock usage point
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(6L);
        when(usagePoint.getMRID()).thenReturn(MRID);
        GasDetailBuilder gasDetailBuilder = FakeBuilder.initBuilderStub(null, GasDetailBuilder.class);
        when(usagePoint.newGasDetailBuilder(NOW)).thenReturn(gasDetailBuilder);
        UsagePointBuilder usagePointBuilder = FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.newUsagePoint(any(), any())).thenReturn(usagePointBuilder);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.GAS);
        when(meteringService.getServiceCategory(ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getInstallationTime()).thenReturn(NOW);

        UsagePointInfo info = new UsagePointInfo();
        info.name = "name";
        info.installationTime = NOW;
        info.serviceKind = ServiceKind.GAS;
        UsagePointCustomPropertySetInfo casInfo = new UsagePointCustomPropertySetInfo();
        casInfo.id = CAS_ID;
        casInfo.isVersioned = false;
        CustomPropertySetAttributeInfo casAttributeInfo = new CustomPropertySetAttributeInfo();
        casAttributeInfo.key = PROPERTY_NAME;
        casAttributeInfo.propertyValueInfo = new PropertyValueInfo<>("value", null);
        casInfo.properties = Collections.singletonList(casAttributeInfo);
        info.customPropertySets = Collections.singletonList(casInfo);

        // Business method
        Response response = target("/usagepoints").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.CAS_IS_NOT_ATTACHED_TO_USAGE_POINT.getKey());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo(thesaurus.getFormat(MessageSeeds.CAS_IS_NOT_ATTACHED_TO_USAGE_POINT).format(CAS_ID));

        verify(usagePointBuilder, never()).create();
    }

    @Test
    public void testUpdateUsagePoint() throws Exception {
        UsagePointInfo info = new UsagePointInfo();
        info.name = "new name";
        info.serviceKind = ServiceKind.ELECTRICITY;
        info.aliasName = "new alias";
        info.description = "new descr";
        info.outageRegion = "new outage region";
        info.readRoute = "new read route";
        info.serviceDeliveryRemark = "new service delivery remark";
        info.servicePriority = "new priority";
        info.location = new LocationInfo();
        info.location.locationId = 13L;
        info.coordinates = new CoordinatesInfo();
        info.coordinates.latitude = BigDecimal.valueOf(10.1);
        info.coordinates.longitude = BigDecimal.valueOf(11.1);
        info.coordinates.elevation = BigDecimal.valueOf(12.1);
        info.version = 2L;

        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        Location location = mock(Location.class);
        when(location.getId()).thenReturn(13L);
        when(locationService.findLocationById(13L)).thenReturn(Optional.of(location));
        ArgumentCaptor<SpatialCoordinates> coordinatesArgumentCaptor = ArgumentCaptor.forClass(SpatialCoordinates.class);
        doNothing().when(usagePoint).setSpatialCoordinates(coordinatesArgumentCaptor.capture());

        // Business method
        Response response = target("/usagepoints/" + MRID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePoint).setName("new name");
        verify(usagePoint).setAliasName("new alias");
        verify(usagePoint).setDescription("new descr");
        verify(usagePoint).setOutageRegion("new outage region");
        verify(usagePoint).setReadRoute("new read route");
        verify(usagePoint).setServiceDeliveryRemark("new service delivery remark");
        verify(usagePoint).setServicePriority("new priority");
        verify(usagePoint).setLocation(13L);
        verify(usagePoint).update();

        SpatialCoordinates coordinates = coordinatesArgumentCaptor.getValue();
        assertThat(coordinates.getLatitude().getValue()).isEqualTo(BigDecimal.valueOf(10.1));
        assertThat(coordinates.getLongitude().getValue()).isEqualTo(BigDecimal.valueOf(11.1));
        assertThat(coordinates.getElevation().getValue()).isEqualTo(BigDecimal.valueOf(12.1));
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
    public void testUpdateUsagePointWrongMrid() {
        when(meteringService.findAndLockUsagePointByMRIDAndVersion("xxx", 2L)).thenReturn(Optional.empty());

        UsagePointInfo info = new UsagePointInfo();
        info.mrid = "xxx";
        info.version = 2L;

        // Business method
        Response response = target("/usagepoints/xxx").request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testUpdateUsagePointWrongVersion() {
        when(meteringService.findAndLockUsagePointByMRIDAndVersion(MRID, 2L)).thenReturn(Optional.empty());

        UsagePointInfo info = new UsagePointInfo();
        info.mrid = MRID;
        info.version = 2L;

        // Business method
        Response response = target("/usagepoints/" + MRID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testUpdateUsagePointUnknownLocation() throws IOException {
        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        when(locationService.findLocationById(anyLong())).thenReturn(Optional.empty());

        UsagePointInfo info = new UsagePointInfo();
        info.version = 2L;
        info.location = new LocationInfo();
        info.location.locationId = 123123L;

        // Business method
        Response response = target("/usagepoints/" + MRID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.NO_SUCH_LOCATION.getKey());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo(thesaurus.getFormat(MessageSeeds.NO_SUCH_LOCATION).format(123123L));

        verify(usagePoint, never()).update();
    }

    @Test
    public void testUpdateUsagePointNoPayload() {
        mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        // Business method
        Response response = target("/usagepoints/" + MRID).request().put(null);

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
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

    @Test
    public void testDeleteUsagePoint() {
        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);

        UsagePointInfo info = new UsagePointInfo();
        info.mrid = MRID;
        info.version = 2L;

        // Business method
        Response response = target("/usagepoints/" + MRID).request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(usagePoint).makeObsolete();
    }

    @Test
    public void testDeleteUsagePointNoPayload() {
        // Business method
        Response response = target("/usagepoints/" + MRID).request().delete();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDeleteUsagePointWrongMrid() {
        when(meteringService.findAndLockUsagePointByMRIDAndVersion("xxx", 2L)).thenReturn(Optional.empty());

        UsagePointInfo info = new UsagePointInfo();
        info.mrid = "xxx";
        info.version = 2L;

        // Business method
        Response response = target("/usagepoints/xxx").request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteUsagePointWrongVersion() {
        when(meteringService.findAndLockUsagePointByMRIDAndVersion(MRID, 2L)).thenReturn(Optional.empty());

        UsagePointInfo info = new UsagePointInfo();
        info.mrid = MRID;
        info.version = 2L;

        // Business method
        Response response = target("/usagepoints/" + MRID).request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
