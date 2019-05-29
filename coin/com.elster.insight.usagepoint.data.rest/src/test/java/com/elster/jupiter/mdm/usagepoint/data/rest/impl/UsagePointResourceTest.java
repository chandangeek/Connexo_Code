/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.ProcessInstanceInfos;
import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetAttributeInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
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
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationCustomPropertySetUsage;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.StatusCode;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointTransitionInfo;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationTask;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.joda.time.DateMidnight;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
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
    private static final String USAGE_POINT_NAME = "The name";
    private static final Instant NOW = ZonedDateTime.of(2015, 12, 10, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();
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
    private CustomPropertySet<UsagePoint, PersistentDomainExtension<UsagePoint>> customPropertySet;
    @Mock
    private UsagePointMetrologyConfiguration usagePointMetrologyConfiguration;
    @Mock
    private UsagePointPropertySet usagePointPropertySet;
    @Mock
    private MetrologyConfigurationCustomPropertySetUsage metrologyConfigurationCustomPropertySetUsage;
    @Mock
    private DataValidationTask validationTask;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private Query<UsagePoint> usagePointQuery;
    @Mock
    private State usagePointState;
    @Mock
    private UsagePointLifeCycle usagePointLifeCycle;
    @Mock
    private Stage usagePointStage;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private PropertyValueConverter propertyValueConverter;
    @Mock
    private UsagePointStateChangeRequest usagePointStateChangeRequest;
    @Mock
    private UsagePointInfoFactory usagePointInfoFactory;
    @Mock
    private ResourceHelper resourceHelper;



    @Before
    public void setUp1() {
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));

        when(serviceCategory.newUsagePoint(eq("test"), any(Instant.class))).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withLifeCycle(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withIsSdp(anyBoolean())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withIsVirtual(anyBoolean())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withReadRoute(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withServiceDeliveryRemark(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withServicePriority(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withServiceLocationString(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.create()).thenReturn(usagePoint);
        when(usagePointBuilder.validate()).thenReturn(usagePoint);

        when(usagePoint.newElectricityDetailBuilder(any(Instant.class))).thenReturn(electricityDetailBuilder);
        when(usagePoint.getUsedCalendars()).thenReturn(mock(UsagePoint.UsedCalendars.class));
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

        when(usagePointStage.getName()).thenReturn(UsagePointStage.OPERATIONAL.getKey());
        when(usagePointState.getStage()).thenReturn(Optional.of(usagePointStage));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getCreateDate()).thenReturn(Instant.now().minusSeconds(60 * 60 * 24));
        when(usagePoint.getModificationDate()).thenReturn(Instant.now().minusSeconds(60 * 60 * 5));
        when(usagePoint.getDetail(any(Instant.class))).thenReturn(Optional.empty());
        when(usagePoint.getServiceLocation()).thenReturn(Optional.empty());
        when(usagePoint.getMRID()).thenReturn("MRID");
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
        when(usagePoint.getInstallationTime()).thenReturn(Instant.EPOCH);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        when(usagePoint.getServiceLocationString()).thenReturn("serviceLocation");
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.empty());
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getState()).thenReturn(usagePointState);

        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.of(usagePointMetrologyConfiguration));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.of(usagePointMetrologyConfiguration));
        when(metrologyConfigurationService.findLinkableMetrologyConfigurations((any(UsagePoint.class)))).thenReturn(Collections.singletonList(usagePointMetrologyConfiguration));

        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllPropertySets()).thenReturn(Collections.emptyList());
        when(usagePoint.forCustomProperties()).thenReturn(extension);
        when(extension.getPropertySet(1L)).thenReturn(usagePointPropertySet);
        doReturn(customPropertySet).when(usagePointPropertySet).getCustomPropertySet();
        when(registeredCustomPropertySet.isEditableByCurrentUser()).thenReturn(true);
        when(customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        when(registeredCustomPropertySet.getId()).thenReturn(1L);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.emptyList());

        when(propertySpec.getName()).thenReturn("code.quality");
        when(propertyValueConverter.convertInfoToValue(eq(propertySpec), any(Object.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1].toString());
        when(propertyValueInfoService.getConverter(propertySpec)).thenReturn(propertyValueConverter);

        when(usagePoint.getSpatialCoordinates()).thenReturn(Optional.empty());
        when(usagePoint.getLocation()).thenReturn(Optional.empty());
        when(locationService.findLocationById(anyLong())).thenReturn(Optional.empty());

        doReturn(usagePointQuery).when(meteringService).getUsagePointQuery();
        doReturn(Collections.singletonList(usagePoint)).when(usagePointQuery)
                .select(any(Condition.class), anyInt(), anyInt());
        when(usagePointGroup.toSubQuery()).thenReturn(mock(Subquery.class));
        when(usagePointGroup.getId()).thenReturn(51L);

        when(usagePointLifeCycle.getStates()).thenReturn(Collections.singletonList(usagePointState));
        when(usagePointLifeCycle.getName()).thenReturn("Life cycle");
        when(usagePointLifeCycle.getId()).thenReturn(1L);
        when(usagePointLifeCycle.getVersion()).thenReturn(1L);

        when(usagePoint.getLifeCycle()).thenReturn(usagePointLifeCycle);
        when(usagePointState.getId()).thenReturn(1L);
        when(usagePointState.getName()).thenReturn("State");
        when(usagePointState.getVersion()).thenReturn(1L);

        when(usagePointLifeCycleService.getLastUsagePointStateChangeRequest(usagePoint)).thenReturn(Optional.of(usagePointStateChangeRequest));
        when(usagePointStateChangeRequest.getTransitionTime()).thenReturn(Instant.EPOCH);
        when(validationService.findValidationTasks()).thenReturn(Collections.singletonList(validationTask));
        when(validationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        when(validationTask.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(validationTask.getScheduleExpression()).thenReturn(PeriodicalScheduleExpression
                .every(6)
                .hours()
                .at(10, 0)
                .build());
        when(validationTask.getMetrologyPurpose()).thenReturn(Optional.empty());
        when(validationTask.getEndDeviceGroup()).thenReturn(Optional.empty());
        when(validationTask.getLastRun()).thenReturn(Optional.empty());
        when(validationTask.getLastOccurrence()).thenReturn(Optional.empty());
        when(validationTask.getId()).thenReturn(31L);
        when(validationTask.getMetrologyPurpose()).thenReturn(Optional.empty());
        doReturn(usagePointQuery).when(meteringService).getUsagePointQuery();
        doReturn(Collections.singletonList(usagePoint)).when(usagePointQuery)
                .select(any(Condition.class), anyInt(), anyInt());
        when(usagePointGroup.toSubQuery()).thenReturn(mock(Subquery.class));
        when(usagePointGroup.getId()).thenReturn(51L);
    }

    @Test
    public void testGetUsagePointInfo() {
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.hasPrivilege(any(String.class), any(String.class))).thenReturn(true);
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.empty());
        UsagePointInfo response = target("usagepoints/" + USAGE_POINT_NAME).request().get(UsagePointInfo.class);

        assertThat(response.mRID).isEqualTo("MRID");
        assertThat(response.name).isEqualTo(USAGE_POINT_NAME);
        assertThat(response.lastTransitionTime).isEqualTo(0L);
        assertThat(response.state).isNotNull();
        assertThat(response.state.id).isEqualTo(1L);
        assertThat(response.state.name).isEqualTo("State");
        assertThat(response.lifeCycle.id).isEqualTo(1L);
        assertThat(response.lifeCycle.name).isEqualTo("Life cycle");
        assertThat(response.isReadyForLinkingMC).isEqualTo(false);
    }

    @Test
    public void testValidateUsagePointGeneralBeforeCreating() {
        UsagePointInfo info = new UsagePointInfo();
        info.name = "test";
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
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("name");
        assertThat(jsonModel.<String>get("$.errors[1].id")).isEqualTo("serviceCategory");
        assertThat(jsonModel.<String>get("$.errors[2].id")).isEqualTo("typeOfUsagePoint");
    }

    @Test
    public void testValidateUsagePointTechnicalBeforeCreating() {
        UsagePointInfo info = new UsagePointInfo();
        info.name = "test";
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
        when(calendarService.findCalendar(anyLong())).thenReturn(Optional.of(mockCalendar()));
        Response response = target("usagepoints").request().post(Entity.json(getBasicUsagePointInfo()));
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testUpdateUsagePoint() {
        when(meteringService.findUsagePointById(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.empty());

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
        verify(usagePoint).setName("upd");
        verify(usagePoint, never()).setInstallationTime(any(Instant.class));
        verify(usagePoint, never()).setSdp(anyBoolean());
        verify(usagePoint, never()).setVirtual(anyBoolean());
        verify(usagePoint).setReadRoute("upd");
        verify(usagePoint).setServiceDeliveryRemark("upd");
        verify(usagePoint).update();
        verify(usagePoint).newElectricityDetailBuilder(any(Instant.class));
    }

    @Test
    public void testUpdateUsagePointWithCustomPropertySet() {
        when(meteringService.findUsagePointById(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(usagePointPropertySet.getValues()).thenReturn(CustomPropertySetValues.empty());
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.empty());

        CustomPropertySetAttributeInfo attributeInfo = new CustomPropertySetAttributeInfo();
        attributeInfo.key = propertySpec.getName();
        attributeInfo.propertyValueInfo = new PropertyValueInfo<>("Poor", null, true);
        CustomPropertySetInfo casInfo = new CustomPropertySetInfo();
        casInfo.id = registeredCustomPropertySet.getId();
        casInfo.properties = Collections.singletonList(attributeInfo);
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
        info.customPropertySets = Collections.singletonList(casInfo);

        ArgumentCaptor<CustomPropertySetValues> valueCaptor = ArgumentCaptor.forClass(CustomPropertySetValues.class);
        Response response = target("usagepoints/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePointPropertySet).setValues(valueCaptor.capture());
        assertThat(valueCaptor.getValue().getProperty(propertySpec.getName())).isEqualTo("Poor");
    }

    @Test
    public void testUpdateUsagePointWithCustomPropertySetUnchanged() {
        when(meteringService.findUsagePointById(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(propertySpec.getName(), "Poor");
        when(usagePointPropertySet.getValues()).thenReturn(values);
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.empty());

        CustomPropertySetAttributeInfo attributeInfo = new CustomPropertySetAttributeInfo();
        attributeInfo.key = propertySpec.getName();
        attributeInfo.propertyValueInfo = new PropertyValueInfo<>("Poor", null, true);
        CustomPropertySetInfo casInfo = new CustomPropertySetInfo();
        casInfo.id = registeredCustomPropertySet.getId();
        casInfo.properties = Collections.singletonList(attributeInfo);
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
        info.customPropertySets = Collections.singletonList(casInfo);

        Response response = target("usagepoints/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint).setName("upd");
        verify(usagePoint, never()).setInstallationTime(any(Instant.class));
        verify(usagePoint, never()).setSdp(anyBoolean());
        verify(usagePoint, never()).setVirtual(anyBoolean());
        verify(usagePoint).setReadRoute("upd");
        verify(usagePoint).setServiceDeliveryRemark("upd");
        verify(usagePoint).update();
        verify(usagePoint).newElectricityDetailBuilder(any(Instant.class));
        verify(usagePointPropertySet, never()).setValues(any(CustomPropertySetValues.class));
    }

    @Test
    public void testUpdateUsagePointWithCustomPropertySetEquivalent() {
        when(meteringService.findUsagePointById(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(usagePointPropertySet.getValues()).thenReturn(CustomPropertySetValues.empty());
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.empty());

        CustomPropertySetAttributeInfo attributeInfo = new CustomPropertySetAttributeInfo();
        attributeInfo.key = propertySpec.getName();
        attributeInfo.propertyValueInfo = new PropertyValueInfo<>(null, null, true);
        CustomPropertySetInfo casInfo = new CustomPropertySetInfo();
        casInfo.id = registeredCustomPropertySet.getId();
        casInfo.properties = Collections.singletonList(attributeInfo);
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
        info.customPropertySets = Collections.singletonList(casInfo);

        Response response = target("usagepoints/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint).setName("upd");
        verify(usagePoint, never()).setInstallationTime(any(Instant.class));
        verify(usagePoint, never()).setSdp(anyBoolean());
        verify(usagePoint, never()).setVirtual(anyBoolean());
        verify(usagePoint).setReadRoute("upd");
        verify(usagePoint).setServiceDeliveryRemark("upd");
        verify(usagePoint).update();
        verify(usagePoint).newElectricityDetailBuilder(any(Instant.class));
        verify(usagePointPropertySet, never()).setValues(any(CustomPropertySetValues.class));
    }

    @Test
    public void testGetLinkableMetrologyConfigurations() throws IOException {
        when(usagePointMetrologyConfiguration.getCustomPropertySets()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        when(usagePointMetrologyConfiguration.getId()).thenReturn(1L);
        when(usagePointMetrologyConfiguration.getName()).thenReturn("TestMC");
        when(metrologyConfigurationCustomPropertySetUsage.getRegisteredCustomPropertySet()).thenReturn(registeredCustomPropertySet);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        info.id = registeredCustomPropertySet.getId();
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/metrologyconfiguration/linkable").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.metrologyConfigurations")).hasSize(1);
        assertThat(model.<Number>get("$.metrologyConfigurations[0].id")).isEqualTo(1);
        assertThat(model.<Number>get("$.metrologyConfigurations[0].customPropertySets[0].id")).isEqualTo(1);
    }

    @Test
    public void testLinkMetrologyConfigurationToUsagePoint() {
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.of(usagePointMetrologyConfiguration));
        when(usagePointMetrologyConfiguration.isActive()).thenReturn(true);
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.empty());
        Instant now = Instant.ofEpochMilli(1462876396000L);
        when(usagePoint.getInstallationTime()).thenReturn(now);
        CustomPropertySetInfo casInfo = new CustomPropertySetInfo();
        casInfo.id = registeredCustomPropertySet.getId();

        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = 1L;
        info.name = "Test";
        info.version = 1L;
        info.customPropertySets = Collections.singletonList(casInfo);
        info.activationTime = Instant.now();
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/metrologyconfiguration").queryParam("validate", "true")
                .queryParam("customPropertySetId", 1L)
                .request()
                .put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(202);
        verify(usagePoint, never()).apply(any(UsagePointMetrologyConfiguration.class));
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        when(effectiveMetrologyConfigurationOnUsagePoint.getStart()).thenReturn(now);

        response = target("usagepoints/" + USAGE_POINT_NAME + "/metrologyconfiguration").queryParam("validate", "false").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint).apply(eq(usagePointMetrologyConfiguration), any(Instant.class));

        //unlink usage point

        UsagePointInfo usagePointInfo = new UsagePointInfo();
        usagePointInfo.id = usagePoint.getId();
        usagePointInfo.version = usagePoint.getVersion();


        response = target("usagepoints/" + USAGE_POINT_NAME + "/unlinkmetrologyconfiguration").queryParam("validate", "false").request().put(Entity.json(usagePointInfo));

        assertThat(response.getStatus()).isEqualTo(200);
        verify(effectiveMetrologyConfigurationOnUsagePoint, times(1)).close(now);
    }

    @Test
    public void testUnlinkMeterRoleFromUsagePoint() {

        Meter meter1 = mock(Meter.class);
        when(meter1.getName()).thenReturn("meter1");
        when(meteringService.findMeterByName("meter1")).thenReturn(Optional.of(meter1));

        MeterRole meterRole = mock(MeterRole.class);
        when(meterRole.getKey()).thenReturn("key1");
        when(metrologyConfigurationService.findMeterRole("key1")).thenReturn(Optional.of(meterRole));

        UsagePointMeterActivator linker = mock(UsagePointMeterActivator.class);
        when(usagePoint.linkMeters()).thenReturn(linker);

        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter1));

        Interval interval = new Interval(new Date(1255659900000L), null);
        when(meterActivation.getInterval()).thenReturn(interval);
        when(usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));

        Long timeStamp = 1555659900000L;

        Response response = target("/usagepoints/"+USAGE_POINT_NAME+"/meterroles/key1/unlink/"+timeStamp).request().put(Entity.json(timeStamp));
        assertThat(response.getStatus()).isEqualTo(200);

        verify(linker).clear(Instant.ofEpochMilli(timeStamp), meterRole);
        verify(linker).complete();
    }


    @Test
    public void testCanActivateAndClearMetersOnUsagePoint() {
        Instant activationDate = Instant.now();
        when(usagePointStage.getName()).thenReturn(UsagePointStage.PRE_OPERATIONAL.getKey());

        Meter meter1 = mock(Meter.class);
        when(meter1.getName()).thenReturn("meter1");
        when(meteringService.findMeterByName("meter1")).thenReturn(Optional.of(meter1));

        Meter meter2 = mock(Meter.class);
        when(meter2.getName()).thenReturn("meter2");
        when(meteringService.findMeterByName("meter2")).thenReturn(Optional.of(meter2));

        MeterRole meterRole1 = mock(MeterRole.class);
        when(meterRole1.getKey()).thenReturn("key1");
        when(metrologyConfigurationService.findMeterRole("key1")).thenReturn(Optional.of(meterRole1));

        MeterRole meterRole2 = mock(MeterRole.class);
        when(meterRole2.getKey()).thenReturn("key2");
        when(metrologyConfigurationService.findMeterRole("key2")).thenReturn(Optional.of(meterRole2));

        MeterRole meterRole3 = mock(MeterRole.class);
        when(meterRole3.getKey()).thenReturn("key3");
        when(metrologyConfigurationService.findMeterRole("key3")).thenReturn(Optional.of(meterRole3));

        UsagePointMeterActivator linker = mock(UsagePointMeterActivator.class);
        when(usagePoint.linkMeters()).thenReturn(linker);

        MeterActivationInfo meterActivation1 = new MeterActivationInfo();
        meterActivation1.meter = new MeterInfo();
        meterActivation1.meter.name = meter1.getName();
        meterActivation1.meterRole = new MeterRoleInfo();
        meterActivation1.meterRole.id = meterRole1.getKey();
        meterActivation1.meterRole.activationTime = activationDate;

        MeterActivationInfo meterActivation2 = new MeterActivationInfo();
        meterActivation2.meter = new MeterInfo();
        meterActivation2.meter.name = meter2.getName();
        meterActivation2.meterRole = new MeterRoleInfo();
        meterActivation2.meterRole.id = meterRole2.getKey();
        meterActivation2.meterRole.activationTime = activationDate;

        MeterActivationInfo meterActivation3 = new MeterActivationInfo();
        meterActivation3.meterRole = new MeterRoleInfo();
        meterActivation3.meterRole.id = meterRole3.getKey();
        meterActivation3.meterRole.activationTime = activationDate;

        UsagePointInfo info = new UsagePointInfo();
        info.version = usagePoint.getVersion();
        info.meterActivations = Arrays.asList(meterActivation1, meterActivation2, meterActivation3);
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.empty());

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/activatemeters").request()
                .put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);

        verify(linker).activate(eq(activationDate), eq(meter1), eq(meterRole1));
        verify(linker).activate(eq(activationDate), eq(meter2), eq(meterRole2));
        verify(linker).completeRemoveOrAdd();
    }

    @Test
    @Ignore("Fails since CONM-129 : Usage point life cycle: Create a usage point in Active state")
    public void testCanActivateAndClearMetersOnUsagePointWithNoPreOperationalStage() throws IOException {
        UsagePointInfo info = new UsagePointInfo();
        info.version = usagePoint.getVersion();

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/activatemeters").request().put(Entity.json(info));
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());

        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
        assertThat(model.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(model.<String>get("$.message")).isEqualTo(MessageSeeds.USAGE_POINT_INCORRECT_STAGE.getDefaultFormat());
        assertThat(model.<String>get("$.error")).isEqualTo(MessageSeeds.USAGE_POINT_INCORRECT_STAGE.getKey());
    }

    @Test
    public void testUsagePointIncorrectStageWhenActivateMeter() throws IOException {
        UsagePointInfo info = new UsagePointInfo();
        info.version = usagePoint.getVersion();
        when(usagePointStage.getName()).thenReturn(UsagePointStage.POST_OPERATIONAL.getKey());

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/activatemeters").request().put(Entity.json(info));
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());

        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
        assertThat(model.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(model.<String>get("$.message")).isEqualTo(MessageSeeds.USAGE_POINT_INCORRECT_STAGE.getDefaultFormat());
        assertThat(model.<String>get("$.error")).isEqualTo(MessageSeeds.USAGE_POINT_INCORRECT_STAGE.getKey());
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
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));

        HeadEndInterface headEndInterface = mock(HeadEndInterface.class);
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(1L);
        when(meter.getMRID()).thenReturn("00000000-0000-0000-0000-0000000000ff");
        when(meter.getName()).thenReturn("meter1");
        when(meter.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));
        when(meter.getVersion()).thenReturn(1L);
        when(headEndInterface.getURLForEndDevice(meter)).thenReturn(Optional.empty());

        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole1));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));

        IssueFilter issueFilter = mock(IssueFilter.class);
        when(issueService.newIssueFilter()).thenReturn(issueFilter);
        Finder issueFinder = mock(Finder.class);
        when(issueFinder.find()).thenReturn(Collections.emptyList());
        doReturn(issueFinder).when(issueService).findIssues(issueFilter);
        IssueStatus issueStatus = mock(IssueStatus.class);
        when(issueService.findStatus(anyString())).thenReturn(Optional.of(issueStatus));
        when(bpmService.getRunningProcesses(anyString(), anyString())).thenReturn(new ProcessInstanceInfos());

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/meteractivations").request().get();
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());

        assertThat(model.<List>get("$.meterActivations")).hasSize(2);
        assertThat(model.<String>get("$.meterActivations[0].meter.mRID")).isEqualTo("00000000-0000-0000-0000-0000000000ff");
        assertThat(model.<String>get("$.meterActivations[0].meter.name")).isEqualTo("meter1");
        assertThat(model.<String>get("$.meterActivations[0].meterRole.id")).isEqualTo("key1");
        assertThat(model.<Object>get("$.meterActivations[1].meter")).isNull();
        assertThat(model.<String>get("$.meterActivations[1].meterRole.id")).isEqualTo("key2");
    }

    @Test
    public void testUsagePointMetrologyConfigurationDetails() {
        UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = mockMetrologyConfigurationWithContract(1, "MetrologyConfiguration");

        MetrologyContract metrologyContract = usagePointMetrologyConfiguration.getContracts().get(0);
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(effectiveMetrologyConfigurationOnUsagePoint.isEffectiveAt(any(Instant.class))).thenReturn(true);

        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(any(MetrologyContract.class), any(Instant.class)))
                .thenReturn(Optional.of(channelsContainer));
        when(effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(eq(metrologyContract), any(Instant.class)))
                .thenReturn(Optional.of(channelsContainer));

        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.metrologyConfiguration.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.name")).isEqualTo("MetrologyConfiguration");
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.status.id")).isEqualTo("incomplete");
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.status.name")).isEqualTo("Incomplete");
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.meterRoles[0].id")).isEqualTo(DefaultMeterRole.DEFAULT.getKey());
        assertThat(jsonModel.<String>get("$.metrologyConfiguration.meterRoles[0].name")).isEqualTo(DefaultMeterRole.DEFAULT.getDefaultFormat());
        assertThat(jsonModel.<Boolean>get("$.metrologyConfiguration.meterRoles[0].required")).isEqualTo(false);
        assertThat(jsonModel.<Number>get("$.metrologyConfiguration.purposes[0].id")).isEqualTo(100);
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

    @Test
    public void testGetHistoryOfMeters() throws Exception {
        MeterActivation meterActivation = mock(MeterActivation.class);
        Meter meter = mock(Meter.class);
        HeadEndInterface headEndInterface = mock(HeadEndInterface.class);
        MeterRole meterRole = mock(MeterRole.class);
        ProcessInstanceInfo processInstanceInfo = new ProcessInstanceInfo();
        processInstanceInfo.processId = "1";
        processInstanceInfo.name = "Replace meter";
        ProcessInstanceInfos processInstanceInfos = new ProcessInstanceInfos(Collections.singletonList(processInstanceInfo));
        when(meterRole.getDisplayName()).thenReturn("Meter role");
        when(meterActivation.getId()).thenReturn(1L);
        when(meterActivation.getStart()).thenReturn(Instant.ofEpochMilli(1486466700000L));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        when(usagePoint.getMRID()).thenReturn("40a4-cb543");
        when(meter.getName()).thenReturn("Meter");
        when(meter.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));
        when(meter.getMRID()).thenReturn("38c2-44b5");
        when(headEndInterface.getURLForEndDevice(meter)).thenReturn(Optional.of(new URL("http://localhost:8989/apps/multisense/index.html#/devices/D1")));
        when(bpmService.getRunningProcesses(any(), any())).thenReturn(processInstanceInfos);

        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/history/meters").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.meters")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.meters[0].id")).isEqualTo(1);
        assertThat(jsonModel.<Long>get("$.meters[0].start")).isEqualTo(1486466700000L);
        assertThat(jsonModel.<Boolean>get("$.meters[0].current")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.meters[0].meter")).isEqualTo("Meter");
        assertThat(jsonModel.<String>get("$.meters[0].url")).isEqualTo("http://localhost:8989/apps/multisense/index.html#/devices/D1");
        assertThat(jsonModel.<String>get("$.meters[0].meterRole")).isEqualTo("Meter role");
        assertThat(jsonModel.<List>get("$.meters[0].ongoingProcesses")).hasSize(1);
        assertThat(jsonModel.<String>get("$.meters[0].ongoingProcesses[0].id")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.meters[0].ongoingProcesses[0].name")).isEqualTo("Replace meter");
    }

    public void testGetValidationTasksOnUsagePoint() throws Exception {
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationtasks").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.dataValidationTasks")).hasSize(1);
        assertThat(model.<Integer>get("$.dataValidationTasks[0].id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.dataValidationTasks[0].usagePointGroup.id")).isEqualTo(51);
    }

    @Test
    public void testUsagePointCreationWithMetrologyConfigurationAndMeters() throws Exception {
        when(calendarService.findCalendar(anyLong())).thenReturn(Optional.of(mockCalendar()));
        UsagePointInfo usagePointInfo = getInfoWithMetrologyConfigurationAndMeters();
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        Response response = target("usagepoints").request().post(Entity.json(usagePointInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(usagePointBuilder).create();
        verify(transactionService).getContext();
        verify(usagePoint).apply(any(), any(Instant.class), any(Set.class));
    }

    @Test
    public void testUsagePointCreationWithTransition() throws Exception {
        UsagePointTransition transition = mock(UsagePointTransition.class);
        UsagePointInfo usagePointInfo = getInfoWithMetrologyConfigurationAndMeters();
        UsagePointTransitionInfo transitionToPerform = new UsagePointTransitionInfo();
        transitionToPerform.properties = Collections.emptyList();

        transitionToPerform.name = "Install active";
        transitionToPerform.effectiveTimestamp = clock.instant();
        usagePointInfo.transitionToPerform = transitionToPerform;

        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        when(usagePointLifeCycleConfigurationService.findUsagePointTransition(any(Long.class))).thenReturn(Optional.of(transition));
        when(calendarService.findCalendar(anyLong())).thenReturn(Optional.of(mockCalendar()));

        Response response = target("usagepoints").request().post(Entity.json(usagePointInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(usagePointLifeCycleService).scheduleTransition(usagePoint, transition, transitionToPerform.effectiveTimestamp, "INS", Collections.emptyMap());
        verify(usagePointBuilder).create();
        verify(transactionService).getContext();
        verify(usagePoint).apply(any(), any(Instant.class), any(Set.class));
        verify(usagePointLifeCycleService).scheduleTransition(any(), any(), any(), any(),any());
    }

    @Test
    public void testGetUsagePointPrivileges() throws Exception {
        EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        Stage  stage = mock(Stage.class);
        State state = mock(State.class);
        List<EffectiveMetrologyConfigurationOnUsagePoint> configurationOnUsagePointList = Arrays.asList(metrologyConfigurationOnUsagePoint);
        Range<Instant> range = Range.open(new DateMidnight(2014, 1, 1).toDate().toInstant(), new DateMidnight(2014, 2, 1).toDate().toInstant());

        when(usagePoint.getState()).thenReturn(state);
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(stage.getName()).thenReturn(UsagePointStage.SUSPENDED.getKey());
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(configurationOnUsagePointList);
        when(metrologyConfigurationOnUsagePoint.getRange()).thenReturn(range);

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/metrologyconfiguration/privileges").request().get();
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.privileges")).hasSize(1);
        assertThat(model.<String>get("$.privileges[0].name")).isEqualTo("usagepoint.action.link.metrology.configuration");
    }

    @Test
    public void testGetUsagePointPrivilegesWithOpenMC() throws Exception {
        EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationOnUsagePoint1 = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        Stage  stage = mock(Stage.class);
        State state = mock(State.class);
        List<EffectiveMetrologyConfigurationOnUsagePoint> configurationOnUsagePointList =  Arrays.asList(metrologyConfigurationOnUsagePoint,metrologyConfigurationOnUsagePoint1);
        Range<Instant> range = Range.closed(new DateMidnight(2014, 1, 1).toDate().toInstant(), new DateMidnight(2014, 2, 1).toDate().toInstant());
        Range<Instant> range1 = Range.atLeast(Instant.from(NOW));
        when(usagePoint.getState()).thenReturn(state);
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(stage.getName()).thenReturn(UsagePointStage.SUSPENDED.getKey());
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(configurationOnUsagePointList);
        when(metrologyConfigurationOnUsagePoint1.getRange()).thenReturn(range1);
        when(metrologyConfigurationOnUsagePoint.getRange()).thenReturn(range);

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/metrologyconfiguration/privileges").request().get();
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(0);
        assertThat(model.<List>get("$.privileges")).hasSize(0);
    }

    @Test
    public void testGetUsagePointPrivilegesWithNoMC() throws Exception {
        Stage  stage = mock(Stage.class);
        State state = mock(State.class);
        List<EffectiveMetrologyConfigurationOnUsagePoint> configurationOnUsagePointList = Collections.emptyList();
        when(usagePoint.getState()).thenReturn(state);
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(stage.getName()).thenReturn(UsagePointStage.SUSPENDED.getKey());
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(configurationOnUsagePointList);
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/metrologyconfiguration/privileges").request().get();
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.privileges")).hasSize(1);
        assertThat(model.<String>get("$.privileges[0].name")).isEqualTo("usagepoint.action.link.metrology.configuration");
    }

    @Test
    public void testGetMetrologyConfigurationsHistory() throws IOException {
        Instant effectiveTime = Instant.now();
        Instant closedTime = Instant.ofEpochMilli(effectiveTime.toEpochMilli() / 2);
        ReadingType readingType = mockReadingType();

        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyPurpose.getName()).thenReturn("Billing");
        when(deliverable.getName()).thenReturn("Secondary Sum A+ (Wh)");
        when(deliverable.getReadingType()).thenReturn(readingType);

        UsagePointMetrologyConfiguration currentMetrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        UsagePointMetrologyConfiguration closedMetrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffective = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        EffectiveMetrologyConfigurationOnUsagePoint closedEffective = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);

        when(currentEffective.getStart()).thenReturn(effectiveTime);
        when(currentEffective.getId()).thenReturn(555L);
        when(currentEffective.getMetrologyConfiguration()).thenReturn(currentMetrologyConfiguration);
        when(currentMetrologyConfiguration.getName()).thenReturn("Current effective");
        when(currentEffective.isEffectiveAt(any(Instant.class))).thenReturn(true);
        when(currentMetrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));

        when(closedEffective.getStart()).thenReturn(closedTime);
        when(closedEffective.getId()).thenReturn(666L);
        when(closedEffective.getMetrologyConfiguration()).thenReturn(closedMetrologyConfiguration);
        when(closedEffective.isEffectiveAt(any(Instant.class))).thenReturn(false);
        when(closedMetrologyConfiguration.getName()).thenReturn("Closed effective");
        when(closedMetrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(closedEffective.getUsagePoint()).thenReturn(usagePoint);
        when(currentEffective.getUsagePoint()).thenReturn(usagePoint);
        when(closedMetrologyConfiguration.getId()).thenReturn(666L);
        when(currentMetrologyConfiguration.getId()).thenReturn(555L);
        when(usagePoint.getMRID()).thenReturn("MRID");

        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Arrays.asList(currentEffective, closedEffective));
        when(bpmService.getRunningProcesses(anyString(), anyString())).thenReturn(new ProcessInstanceInfos());

        when(currentEffective.getUsagePoint()).thenReturn(usagePoint);
        when(closedEffective.getUsagePoint()).thenReturn(usagePoint);
        when(currentMetrologyConfiguration.getId()).thenReturn(555L);
        when(closedMetrologyConfiguration.getId()).thenReturn(666L);
        when(usagePoint.getMRID()).thenReturn("MRID");
        Response response = target("/usagepoints/" + USAGE_POINT_NAME + "/history/metrologyConfigurations").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(jsonModel.<Boolean>get("$.data[0]current")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.data[0].start")).isEqualTo(effectiveTime.toEpochMilli());
        assertThat(jsonModel.<String>get("$.data[0].purposesWithReadingTypes.Billing[0].aliasName")).isEqualTo("readingTypeAliasName");
        assertThat(jsonModel.<String>get("$.data[0].metrologyConfiguration.name")).isEqualTo("Current effective");
        assertThat(jsonModel.<Integer>get("$.data[0].metrologyConfiguration.id")).isEqualTo(555);

        assertThat(jsonModel.<Boolean>get("$.data[1]current")).isEqualTo(false);
        assertThat(jsonModel.<Long>get("$.data[1].start")).isEqualTo(closedTime.toEpochMilli());
        assertThat(jsonModel.<String>get("$.data[1].purposesWithReadingTypes.Billing[0].aliasName")).isEqualTo("readingTypeAliasName");
        assertThat(jsonModel.<String>get("$.data[1].metrologyConfiguration.name")).isEqualTo("Closed effective");
        assertThat(jsonModel.<Integer>get("$.data[1].metrologyConfiguration.id")).isEqualTo(666);
    }

    private UsagePointInfo getInfoWithMetrologyConfigurationAndMeters() {
        UsagePointInfo usagePointInfo = getBasicUsagePointInfo();
        CustomPropertySetInfo casInfo = new CustomPropertySetInfo();
        casInfo.id = 1L;

        MetrologyConfigurationInfo configurationInfo = new MetrologyConfigurationInfo();
        configurationInfo.id = 1L;
        configurationInfo.name = "Test";
        configurationInfo.version = 1L;
        configurationInfo.activationTime = Instant.now();
        configurationInfo.customPropertySets = Collections.singletonList(casInfo);
        Meter meter1 = mock(Meter.class);
        when(meter1.getName()).thenReturn("meter1");
        when(meteringService.findMeterByName("meter1")).thenReturn(Optional.of(meter1));

        MeterRole meterRole = mock(MeterRole.class);
        when(meterRole.getKey()).thenReturn("key1");
        when(metrologyConfigurationService.findMeterRole("key1")).thenReturn(Optional.of(meterRole));

        UsagePointMeterActivator linker = mock(UsagePointMeterActivator.class);
        when(usagePoint.linkMeters()).thenReturn(linker);

        MeterActivationInfo meterActivation = new MeterActivationInfo();
        meterActivation.meter = new MeterInfo();
        meterActivation.meter.name = meter1.getName();
        meterActivation.meterRole = new MeterRoleInfo();
        meterActivation.meterRole.id = meterRole.getKey();

        usagePointInfo.version = usagePoint.getVersion();
        usagePointInfo.meterActivations = Collections.singletonList(meterActivation);

        configurationInfo.meterRoles = Collections.singletonList(new MeterRoleInfo());
        usagePointInfo.metrologyConfiguration = configurationInfo;

        CalendarOnUsagePointInfo calendarOnUsagePointInfo = new CalendarOnUsagePointInfo();
        calendarOnUsagePointInfo.calendar = new CalendarInfo();
        //calendarOnUsagePointInfo.next = calendarOnUsagePointInfo;
        calendarOnUsagePointInfo.immediately = true;
        calendarOnUsagePointInfo.usagePointId = 1L;
        usagePointInfo.calendars = Collections.singletonList(calendarOnUsagePointInfo);
        return usagePointInfo;
    }

    private ReadingType mockReadingType() {
        ReadingType readingType = mock(ReadingType.class);

        when(readingType.getMRID()).thenReturn("readingTypeMRID");
        when(readingType.getAliasName()).thenReturn("readingTypeAliasName");
        when(readingType.getName()).thenReturn("readingTypeName");
        when(readingType.isActive()).thenReturn(true);
        when(readingType.isCumulative()).thenReturn(true);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.BILLINGPERIOD);
        when(readingType.getAccumulation()).thenReturn(Accumulation.CUMULATIVE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK5MIN);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType.getArgument()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.ACRE);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("USD"));

        return readingType;
    }

    private UsagePointInfo getBasicUsagePointInfo() {
        UsagePointInfo info = new UsagePointInfo();
        info.name = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.techInfo = new ElectricityUsagePointDetailsInfo();
        info.geoCoordinates = "";
        info.location = "";
        info.extendedGeoCoordinates = new CoordinatesInfo();
        info.extendedLocation = new LocationInfo();
        CalendarOnUsagePointInfo calendarOnUsagePointInfo = new CalendarOnUsagePointInfo();
        calendarOnUsagePointInfo.calendar = new CalendarInfo();
        //calendarOnUsagePointInfo.next = calendarOnUsagePointInfo;
        calendarOnUsagePointInfo.immediately = true;
        calendarOnUsagePointInfo.usagePointId = 1L;
        info.calendars = Collections.singletonList(calendarOnUsagePointInfo);
        info.lifeCycle = new UsagePointLifeCycleInfo();
        info.lifeCycle.name = "LifeCycleName";
        return info;
    }
}
