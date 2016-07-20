package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.bpm.ProcessInstanceInfos;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationCustomPropertySetUsage;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationEvaluator;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointResourceTest extends UsagePointDataRestApplicationJerseyTest {

    public static final Instant NOW = ZonedDateTime.of(2015, 12, 10, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();
    @Mock
    private ValidationEvaluator evaluator;
    @Mock
    private User principal;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private UsagePointBuilder usagePointBuilder;
    @Mock
    private ElectricityDetailBuilder electricityDetailBuilder;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;
    @Mock
    private CustomPropertySetInfoFactory customPropertySetInfoFactory;
    @Mock
    private CustomPropertySet customPropertySet;
    @Mock
    private UsagePointMetrologyConfiguration usagePointMetrologyConfiguration;
    @Mock
    private UsagePointPropertySet usagePointPropertySet;
    @Mock
    private MetrologyConfigurationCustomPropertySetUsage metrologyConfigurationCustomPropertySetUsage;

    @Before
    public void setUp1() {
        when(meteringService.findUsagePoint("MRID")).thenReturn(Optional.of(usagePoint));
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));

        when(serviceCategory.newUsagePoint(anyString(), any(Instant.class))).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withIsSdp(anyBoolean())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withIsVirtual(anyBoolean())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withName(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withReadRoute(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withServiceDeliveryRemark(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withServicePriority(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withServiceLocationString(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.create()).thenReturn(usagePoint);
        when(usagePointBuilder.validate()).thenReturn(usagePoint);

        when(usagePoint.newElectricityDetailBuilder(any(Instant.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withCollar(any())).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withEstimatedLoad(any(Quantity.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withGrounded(any(YesNoAnswer.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withInterruptible(any(YesNoAnswer.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withLimiter(any(YesNoAnswer.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withLoadLimiterType(any())).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withLoadLimit(any(Quantity.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withRatedPower(any(Quantity.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withRatedCurrent(any(Quantity.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withPhaseCode(any())).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withNominalServiceVoltage(any(Quantity.class))).thenReturn(electricityDetailBuilder);

        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getCreateDate()).thenReturn(Instant.now().minusSeconds(60 * 60 * 24));
        when(usagePoint.getModificationDate()).thenReturn(Instant.now().minusSeconds(60 * 60 * 5));
        when(usagePoint.getDetail(any(Instant.class))).thenReturn(Optional.empty());
        when(usagePoint.getServiceLocation()).thenReturn(Optional.empty());
        when(usagePoint.getMRID()).thenReturn("MRID");
        when(usagePoint.getInstallationTime()).thenReturn(Instant.EPOCH);
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.empty());
        when(usagePoint.getServiceLocationString()).thenReturn("serviceLocation");
        when(usagePoint.getConnectionState()).thenReturn(ConnectionState.UNDER_CONSTRUCTION);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);

        when(meteringService.findUsagePoint("test")).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.of(usagePointMetrologyConfiguration));
        when(metrologyConfigurationService.findLinkableMetrologyConfigurations((any(UsagePoint.class)))).thenReturn(Arrays.asList(usagePointMetrologyConfiguration));

        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllPropertySets()).thenReturn(Collections.emptyList());
        when(usagePoint.forCustomProperties()).thenReturn(extension);
        when(extension.getPropertySet(1L)).thenReturn(usagePointPropertySet);
        when(usagePointPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(registeredCustomPropertySet.isEditableByCurrentUser()).thenReturn(true);
        when(customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)).thenReturn(Arrays.asList(registeredCustomPropertySet));
        when(registeredCustomPropertySet.getId()).thenReturn(1L);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.of(usagePointMetrologyConfiguration));
        when(metrologyConfigurationService.findLinkableMetrologyConfigurations((any(UsagePoint.class)))).thenReturn(Arrays.asList(usagePointMetrologyConfiguration));
        when(usagePoint.forCustomProperties().getPropertySet(1L)).thenReturn(usagePointPropertySet);
        when(meteringService.findUsagePoint(anyString())).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getSpatialCoordinates()).thenReturn(Optional.empty());
        when(usagePoint.getLocation()).thenReturn(Optional.empty());
        when(locationService.findLocationById(anyLong())).thenReturn(Optional.empty());
    }

    @Test
    public void testGetUsagePointInfo() {
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.hasPrivilege(any(String.class), any(String.class))).thenReturn(true);
        UsagePointInfo response = target("usagepoints/MRID").request().get(UsagePointInfo.class);

        assertThat(response.mRID).isEqualTo("MRID");
    }

    @Test
    public void testValidateUsagePointGeneralBeforeCreating() {
        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.techInfo = new ElectricityUsagePointDetailsInfo();
        info.extendedGeoCoordinates = new CoordinatesInfo();
        info.extendedLocation = new LocationInfo();
        info.geoCoordinates = "";
        info.location = "";

        Response response = target("usagepoints").queryParam("validate", true).queryParam("step", 1).request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(202);
    }

    @Test
    public void testValidateUsagePointGeneralBeforeCreatingFailed() throws Exception {
        UsagePointInfo info = new UsagePointInfo();
        info.isVirtual = true;
        info.geoCoordinates = "";
        info.location = "";
        info.extendedGeoCoordinates = new CoordinatesInfo();
        info.extendedLocation = new LocationInfo();

        Response response = target("usagepoints").queryParam("validate", true).queryParam("step", 1).request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(400);
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("mRID");
        assertThat(jsonModel.<String>get("$.errors[1].id")).isEqualTo("serviceCategory");
        assertThat(jsonModel.<String>get("$.errors[2].id")).isEqualTo("typeOfUsagePoint");
    }


    @Test
    public void testValidateUsagePointTechnicalBeforeCreating() {
        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.techInfo = new ElectricityUsagePointDetailsInfo();
        info.geoCoordinates = "";
        info.location = "";
        info.extendedGeoCoordinates = new CoordinatesInfo();
        info.extendedLocation = new LocationInfo();

        Response response = target("usagepoints").queryParam("validate", true).queryParam("step", 2).request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(202);
    }

    @Test
    public void testUsagePointCreating() {
        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.techInfo = new ElectricityUsagePointDetailsInfo();
        info.geoCoordinates = "";
        info.location = "";
        info.extendedGeoCoordinates = new CoordinatesInfo();
        info.extendedLocation = new LocationInfo();

        Response response = target("usagepoints").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testUpadateUsagePoint() {
        when(meteringService.findUsagePoint(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        UsagePointInfo info = new UsagePointInfo();
        info.id = 1L;
        info.mRID = "upd";
        info.name = "upd";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.readRoute = "upd";
        info.serviceDeliveryRemark = "upd";
        info.version = 1L;
        info.techInfo = new ElectricityUsagePointDetailsInfo();
        info.geoCoordinates = "";
        info.location = "";
        info.extendedGeoCoordinates = new CoordinatesInfo();
        info.extendedLocation = new LocationInfo();

        Response response = target("usagepoints/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, never()).setMRID(anyString());
        verify(usagePoint, times(1)).setName("upd");
        verify(usagePoint, never()).setInstallationTime(any(Instant.class));
        verify(usagePoint, never()).setSdp(anyBoolean());
        verify(usagePoint, never()).setVirtual(anyBoolean());
        verify(usagePoint, times(1)).setReadRoute("upd");
        verify(usagePoint, times(1)).setServiceDeliveryRemark("upd");
        verify(usagePoint, times(1)).update();
        verify(usagePoint, times(1)).newElectricityDetailBuilder(any(Instant.class));
    }

    @Test
    public void testGetLinkableMetrologyConfigurations() {
        when(usagePointMetrologyConfiguration.getCustomPropertySets()).thenReturn(Arrays.asList(registeredCustomPropertySet));
        when(usagePointMetrologyConfiguration.getId()).thenReturn(1L);
        when(usagePointMetrologyConfiguration.getName()).thenReturn("TestMC");
        when(metrologyConfigurationCustomPropertySetUsage.getRegisteredCustomPropertySet()).thenReturn(registeredCustomPropertySet);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        info.id = registeredCustomPropertySet.getId();
        when(customPropertySetInfoFactory.getGeneralAndPropertiesInfo(any(RegisteredCustomPropertySet.class))).thenReturn(info);
        MetrologyConfigurationInfos metrologyConfigs = target("usagepoints/test/metrologyconfiguration/linkable").request().get(MetrologyConfigurationInfos.class);
        assertThat(metrologyConfigs.total == 1);
        assertThat(metrologyConfigs.metrologyConfigurations.get(0).id == 1);
        assertThat(metrologyConfigs.metrologyConfigurations.get(0).customPropertySets.get(0).id == 1);
    }

    @Test
    public void testLinkMetrologyConfigurationToUsagePoint() {
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.of(usagePointMetrologyConfiguration));
        when(usagePointMetrologyConfiguration.isActive()).thenReturn(true);
        when(usagePoint.getMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.empty());
        Instant now = Instant.ofEpochMilli(1462876396000L);
        when(usagePoint.getInstallationTime()).thenReturn(now);
        CustomPropertySetInfo casInfo = new CustomPropertySetInfo();
        casInfo.id = 1L;

        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = 1L;
        info.name = "Test";
        info.version = 1L;
        info.customPropertySets = Arrays.asList(casInfo);
        Response response = target("usagepoints/test/metrologyconfiguration").queryParam("validate", "true")
                .queryParam("customPropertySetId", 1L)
                .request()
                .put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(202);
        verify(usagePoint, never()).apply(any(UsagePointMetrologyConfiguration.class));

        response = target("usagepoints/test/metrologyconfiguration").queryParam("validate", "false").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, times(1)).apply(usagePointMetrologyConfiguration, now);
    }

    @Test
    public void testCanActivateAndClearMetersOnUsagePoint() {
        Meter meter1 = mock(Meter.class);
        when(meter1.getMRID()).thenReturn("mrid1");
        when(meteringService.findMeter("mrid1")).thenReturn(Optional.of(meter1));

        Meter meter2 = mock(Meter.class);
        when(meter2.getMRID()).thenReturn("mrid2");
        when(meteringService.findMeter("mrid2")).thenReturn(Optional.of(meter2));

        MeterRole meterRole1 = mock(MeterRole.class);
        when(meterRole1.getKey()).thenReturn("key1");
        when(metrologyConfigurationService.findMeterRole("key1")).thenReturn(Optional.of(meterRole1));

        MeterRole meterRole2 = mock(MeterRole.class);
        when(meterRole2.getKey()).thenReturn("key2");
        when(metrologyConfigurationService.findMeterRole("key2")).thenReturn(Optional.of(meterRole2));

        MeterRole meterRole3 = mock(MeterRole.class);
        when(meterRole3.getKey()).thenReturn("key3");
        when(metrologyConfigurationService.findMeterRole("key3")).thenReturn(Optional.of(meterRole3));

        when(meteringService.findUsagePoint("test")).thenReturn(Optional.of(usagePoint));
        UsagePointMeterActivator linker = mock(UsagePointMeterActivator.class);
        when(usagePoint.linkMeters()).thenReturn(linker);

        MeterActivationInfo meterActivation1 = new MeterActivationInfo();
        meterActivation1.meter = new MeterInfo();
        meterActivation1.meter.mRID = meter1.getMRID();
        meterActivation1.meterRole = new MeterRoleInfo();
        meterActivation1.meterRole.id = meterRole1.getKey();

        MeterActivationInfo meterActivation2 = new MeterActivationInfo();
        meterActivation2.meter = new MeterInfo();
        meterActivation2.meter.mRID = meter2.getMRID();
        meterActivation2.meterRole = new MeterRoleInfo();
        meterActivation2.meterRole.id = meterRole2.getKey();

        MeterActivationInfo meterActivation3 = new MeterActivationInfo();
        meterActivation3.meterRole = new MeterRoleInfo();
        meterActivation3.meterRole.id = meterRole3.getKey();

        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.version = usagePoint.getVersion();
        info.meterActivations = Arrays.asList(meterActivation1, meterActivation2, meterActivation3);

        Response response = target("usagepoints/test/activatemeters").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);

        verify(linker).activate(eq(meter1), eq(meterRole1));
        verify(linker).activate(eq(meter2), eq(meterRole2));
        verify(linker).clear(eq(meterRole3));
        verify(linker).complete();
    }

    @Test
    public void testGetMetersOnUsagePoint() throws Exception {
        Instant now = Instant.ofEpochMilli(1462876396000L);
        when(usagePoint.getInstallationTime()).thenReturn(now);

        MeterRole meterRole1 = mock(MeterRole.class);
        when(meterRole1.getKey()).thenReturn("key1");
        when(meterRole1.getDisplayName()).thenReturn("name1");

        MeterRole meterRole2 = mock(MeterRole.class);
        when(meterRole2.getKey()).thenReturn("key2");
        when(meterRole2.getDisplayName()).thenReturn("name2");

        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(100L);
        when(metrologyConfiguration.getName()).thenReturn("conf");
        when(metrologyConfiguration.getMeterRoles()).thenReturn(Arrays.asList(meterRole1, meterRole2));
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.of(metrologyConfiguration));

        HeadEndInterface headEndInterface = mock(HeadEndInterface.class);
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(1L);
        when(meter.getMRID()).thenReturn("meter1");
        when(meter.getName()).thenReturn("meter1");
        when(meter.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));
        when(meter.getVersion()).thenReturn(1L);
        when(headEndInterface.getURLForEndDevice(meter)).thenReturn(Optional.empty());

        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole1));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(usagePoint.getMeterActivations(now)).thenReturn(Collections.singletonList(meterActivation));

        IssueFilter issueFilter = mock(IssueFilter.class);
        when(issueService.newIssueFilter()).thenReturn(issueFilter);
        Finder issueFinder = mock(Finder.class);
        when(issueFinder.find()).thenReturn(Collections.emptyList());
        when(issueService.findIssues(issueFilter)).thenReturn(issueFinder);
        IssueStatus issueStatus = mock(IssueStatus.class);
        when(issueService.findStatus(anyString())).thenReturn(Optional.of(issueStatus));
        when(bpmService.getRunningProcesses(anyString(), anyString())).thenReturn(new ProcessInstanceInfos());

        Response response = target("usagepoints/test/meteractivations").request().get();
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());

        assertThat(model.<List>get("$.meterActivations")).hasSize(2);
        assertThat(model.<String>get("$.meterActivations[0].meter.mRID")).isEqualTo("meter1");
        assertThat(model.<String>get("$.meterActivations[0].meterRole.id")).isEqualTo("key1");
        assertThat(model.<Object>get("$.meterActivations[1].meter")).isNull();
        assertThat(model.<String>get("$.meterActivations[1].meterRole.id")).isEqualTo("key2");
    }

    @Test
    public void testUsagePointMetrologyConfigurationDetails() {
        UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = mockMetrologyConfigurationWithContract(1, "MetrologyConfiguration");
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.of(usagePointMetrologyConfiguration));
        MetrologyContract metrologyContract = usagePointMetrologyConfiguration.getContracts().stream().findFirst().get();
        MetrologyContract.Status status = mock(MetrologyContract.Status.class);
        when(metrologyContract.getStatus(usagePoint)).thenReturn(status);
        when(metrologyContract.getStatus(usagePoint).getKey()).thenReturn("INCOMPLETE");
        when(metrologyContract.getStatus(usagePoint).getName()).thenReturn("Incomplete");

        // Business method
        String json = target("/usagepoints/MRID").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.metrologyConfiguration.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.name")).isEqualTo("MetrologyConfiguration");
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.status.id")).isEqualTo("incomplete");
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.status.name")).isEqualTo("Incomplete");
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.meterRoles[0].id")).isEqualTo(DefaultMeterRole.DEFAULT.getKey());
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.meterRoles[0].name")).isEqualTo(DefaultMeterRole.DEFAULT.getDefaultFormat());
        assertThat(jsonModel.<Boolean>get("$.metrologyConfiguration.meterRoles[0].required")).isEqualTo(false);
        assertThat(jsonModel.<Number>get("$.metrologyConfiguration.purposes[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.purposes[0].name")).isEqualTo(DefaultMetrologyPurpose.BILLING.getName().getDefaultMessage());
        assertThat(jsonModel.<Boolean>get("$.metrologyConfiguration.purposes[0].required")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.metrologyConfiguration.purposes[0].active")).isEqualTo(true);
    }

    @Test
    public void testGetUsagePointLocation() throws Exception {
        LocationTemplate locationTemplate = mock(LocationTemplate.class);
        List<String> templateElementsNames = new ArrayList<>();

        templateElementsNames.add("zipCode");
        templateElementsNames.add("countryCode");
        templateElementsNames.add("countryName");

        when(meteringService.getLocationTemplate()).thenReturn(locationTemplate);
        when(locationTemplate.getTemplateElementsNames()).thenReturn(templateElementsNames);

        LocationTemplate.TemplateField zipCode = mock(LocationTemplate.TemplateField.class);
        LocationTemplate.TemplateField countryCode = mock(LocationTemplate.TemplateField.class);
        LocationTemplate.TemplateField countryName = mock(LocationTemplate.TemplateField.class);

        List<LocationTemplate.TemplateField> templateMembers = new ArrayList<>();
        templateMembers.add(zipCode);
        templateMembers.add(countryCode);
        templateMembers.add(countryName);
        when(locationTemplate.getTemplateMembers()).thenReturn(templateMembers);
        when(zipCode.getName()).thenReturn("zipCode");
        when(countryCode.getName()).thenReturn("countryCode");
        when(countryName.getName()).thenReturn("countryName");

        when(zipCode.isMandatory()).thenReturn(true);
        when(countryCode.isMandatory()).thenReturn(false);
        when(countryName.isMandatory()).thenReturn(false);

        Response response = target("usagepoints/locations/1").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
