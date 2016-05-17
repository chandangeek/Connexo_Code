package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationCustomPropertySetUsage;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointResourceTest extends UsagePointDataRestApplicationJerseyTest {

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    public static final Instant NOW = ZonedDateTime.of(2015, 12, 10, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();
    public static final Instant LAST_READING = ZonedDateTime.of(2015, 12, 9, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();
    private static long intervalStart = 1410774630000L;

    @Mock
    private ValidationEvaluator evaluator;

    @Mock
    private User principal;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private Meter meter;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Channel channel, register;
    @Mock
    private ReadingQuality suspect, notSuspect;
    @Mock
    private IntervalReadingRecord irr1, irr2, irr3, irr4;
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
        when(meteringService.findUsagePointLocation("MRID")).thenReturn(Optional.empty());
        when(meteringService.findUsagePointGeoCoordinates("MRID")).thenReturn(Optional.empty());

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

        when(usagePoint.getMeter(any())).thenReturn(Optional.of(meter));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getCreateDate()).thenReturn(Instant.now().minusSeconds(60*60*24));
        when(usagePoint.getModificationDate()).thenReturn(Instant.now().minusSeconds(60*60*5));
        when(usagePoint.getDetail(any(Instant.class))).thenReturn(Optional.empty());
        when(usagePoint.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        when(usagePoint.getServiceLocation()).thenReturn(Optional.empty());
        when(usagePoint.getMRID()).thenReturn("MRID");
        when(usagePoint.getInstallationTime()).thenReturn(Instant.EPOCH);
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.empty());
        when(usagePoint.getServiceLocationString()).thenReturn("serviceLocation");
        when(usagePoint.getConnectionState()).thenReturn(ConnectionState.UNDER_CONSTRUCTION);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);

        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllPropertySets()).thenReturn(Collections.emptyList());
        when(usagePoint.forCustomProperties()).thenReturn(extension);

        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();

        Range<Instant> intervalToNow = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.now());
        when(meterActivation.getRange()).thenReturn(intervalToNow);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel, register));

        when(register.isRegular()).thenReturn(false);
        when(channel.isRegular()).thenReturn(true);
        when(register.getMeterActivation()).thenReturn(meterActivation);
        when(channel.getMeterActivation()).thenReturn(meterActivation);
        when(channel.getReadings(any())).thenReturn(Arrays.asList(irr1, irr2, irr3, irr4));

        when(usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(any())).thenReturn(Optional.empty());

        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(validationService.getEvaluator(eq(meter), any(Range.class))).thenReturn(evaluator);
        when(validationService.getLastChecked(any(MeterActivation.class))).thenReturn(Optional.of(NOW));
        when(suspect.getTypeCode()).thenReturn("3.5.258");
        when(notSuspect.getTypeCode()).thenReturn("0.0.0");
        when(suspect.getType()).thenReturn(new ReadingQualityType("3.5.258"));
        when(notSuspect.getType()).thenReturn(new ReadingQualityType("0.0.0"));

        ReadingQualityType readingQualitySuspect = new ReadingQualityType("3.5.258");
        DataValidationStatus statusForSuspect = mockDataValidationStatus(readingQualitySuspect, false);
        when(evaluator.getValidationStatus(eq(channel), any(), any())).thenReturn(Arrays.asList(statusForSuspect, statusForSuspect, statusForSuspect, statusForSuspect));
        when(evaluator.getValidationStatus(eq(register), any(), any())).thenReturn(Arrays.asList(statusForSuspect, statusForSuspect, statusForSuspect, statusForSuspect, statusForSuspect));

        when(meteringService.findUsagePoint("test")).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        when(registeredCustomPropertySet.isEditableByCurrentUser()).thenReturn(true);
        when(customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)).thenReturn(Arrays.asList(registeredCustomPropertySet));
        when(registeredCustomPropertySet.getId()).thenReturn(1L);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.of(usagePointMetrologyConfiguration));
        when(metrologyConfigurationService.findLinkableMetrologyConfigurations((any(UsagePoint.class)))).thenReturn(Arrays.asList(usagePointMetrologyConfiguration));
        when(usagePoint.forCustomProperties().getPropertySet(1L)).thenReturn(usagePointPropertySet);
        when(usagePointPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
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

        Response response = target("usagepoints").queryParam("validate",true).queryParam("step",1).request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(202);
    }

    @Test
    public void testValidateUsagePointGeneralBeforeCreatingFailed() throws Exception {

        UsagePointInfo info = new UsagePointInfo();
        info.isVirtual = true;

        Response response = target("usagepoints").queryParam("validate",true).queryParam("step",1).request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(400);
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String >get("$.errors[0].id")).isEqualTo("mRID");
        assertThat(jsonModel.<String >get("$.errors[1].id")).isEqualTo("serviceCategory");
        assertThat(jsonModel.<String >get("$.errors[2].id")).isEqualTo("typeOfUsagePoint");
    }


    @Test
    public void testValidateUsagePointTechnicalBeforeCreating() {

        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.techInfo = new ElectricityUsagePointDetailsInfo();

        Response response = target("usagepoints").queryParam("validate",true).queryParam("step",2).request().post(Entity.json(info));
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
        info.location = "upd";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.readRoute = "upd";
        info.serviceDeliveryRemark = "upd";
        info.version = 1L;
        info.techInfo = new ElectricityUsagePointDetailsInfo();

        Response response = target("usagepoints/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, never()).setMRID(anyString());
        verify(usagePoint, times(1)).setName("upd");
        verify(usagePoint, times(1)).setServiceLocationString("upd");
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
        CustomPropertySetInfo casInfo = new CustomPropertySetInfo();
        casInfo.id = 1L;

        MetrologyConfigurationInfo usagePointMetrologyConfigurationInfo = new MetrologyConfigurationInfo();
        usagePointMetrologyConfigurationInfo.id = 1L;
        usagePointMetrologyConfigurationInfo.version = 1L;
        usagePointMetrologyConfigurationInfo.name = "Test";
        usagePointMetrologyConfigurationInfo.customPropertySets = Arrays.asList(casInfo);
        Response response = target("usagepoints/test/metrologyconfiguration").queryParam("validate", "true")
                .queryParam("customPropertySetId", 1L)
                .request()
                .put(Entity.json(usagePointMetrologyConfigurationInfo));
        assertThat(response.getStatus()).isEqualTo(202);
        when(usagePointMetrologyConfiguration.isActive()).thenReturn(true);
        verify(usagePoint, never()).apply(usagePointMetrologyConfiguration);
        response = target("usagepoints/test/metrologyconfiguration").queryParam("validate", "false").request().put(Entity.json(usagePointMetrologyConfigurationInfo));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, times(1)).apply(usagePointMetrologyConfiguration);
    }

    private DataValidationStatus mockDataValidationStatus(ReadingQualityType readingQualityType, boolean isBulk) {
        DataValidationStatus status = mock(DataValidationStatus.class);
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.getType()).thenReturn(readingQualityType);
        List<? extends ReadingQualityRecord> readingQualities = Arrays.asList(readingQualityRecord);
        if (isBulk) {
            doReturn(readingQualities).when(status).getBulkReadingQualities();
        } else {
            doReturn(readingQualities).when(status).getReadingQualities();
        }
        return status;
    }

    @Test
    public void testUsagePointMetrologyConfigurationDetails() {
        Optional<MetrologyConfiguration> usagePointMetrologyConfiguration = Optional.of(this.mockMetrologyConfiguration(1, "MetrologyConfiguration"));
        when(usagePoint.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        String json = target("/usagepoints/MRID").request().get(String.class);
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
}