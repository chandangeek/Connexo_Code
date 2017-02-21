/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.StatusCode;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.conditions.And;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.NumericalRegisterImpl;
import com.energyict.mdc.device.data.impl.search.DeviceSearchDomain;
import com.energyict.mdc.device.data.rest.DevicePrivileges;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.device.topology.impl.DataLoggerLinkException;
import com.energyict.mdc.device.topology.impl.DataLoggerReferenceImpl;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DeviceResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final Instant NOW = Instant.ofEpochMilli(1409738114);
    private static final long startTimeFirst = 1416403197000L;
    private static final long endTimeFirst = 1479561597000L;
    private static final long endTimeSecond = 1489561597000L;
    private static final long startTimeNew = 1469561597000L;
    private static final long endTimeNew = 1499561597000L;

    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    private ReadingType readingType;

    @Override
    protected void setupTranslations() {
        super.setupTranslations();
        Stream.of(MessageSeeds.values()).forEach(this::mockTranslation);
        when(this.meteringTranslationService.getDisplayName(any(QualityCodeIndex.class)))
                .thenAnswer(invocationOnMock -> {
                    QualityCodeIndex qualityCodeIndex = (QualityCodeIndex) invocationOnMock.getArguments()[0];
                    return qualityCodeIndex.getTranslationKey().getDefaultFormat();
                });
    }

    private void mockTranslation(MessageSeeds messageSeed) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(messageSeed.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(messageSeed);
    }

    @Before
    public void setupStubs() {
        readingType = mockReadingType("0.1.2.3.5.6.7.8.9.1.2.3.4.5.6.7.8");
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.of(readingType));
        IssueType dataCollectionIssueType = mock(IssueType.class);
        when(issueService.findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE)).thenReturn(Optional.of(dataCollectionIssueType));
        IssueType dataValidationIssueType = mock(IssueType.class);
        when(issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME)).thenReturn(Optional.of(dataValidationIssueType));
        Finder<OpenIssue> issueFinder = mock(Finder.class);
        when(issueFinder.find()).thenReturn(Collections.emptyList());
        when(issueService.findOpenIssuesForDevice(any(String.class))).thenReturn(issueFinder);
        when(topologyService.findDataloggerReference(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.getSlaveRegister(any(Register.class), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.findDataLoggerChannelUsagesForChannels(any(Channel.class), any(Range.class))).thenReturn(Collections.emptyList());
        when(topologyService.getSlaveChannel(any(Channel.class), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.getSlaveRegister(any(Register.class), any(Instant.class))).thenReturn(Optional.empty());
    }

    @Test
    public void testGetConnectionMethodsJsonBindings() throws Exception {
        Device device = mock(Device.class);
        when(device.getVersion()).thenReturn(1L);
        String name = "ZABF0000000";
        when(device.getName()).thenReturn(name);
        when(deviceService.findAndLockDeviceByNameAndVersion(name, device.getVersion())).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(name)).thenReturn(Optional.of(device));

        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getCommunicationWindow()).thenReturn(new ComWindow(100, 200));
        when(connectionTask.getNumberOfSimultaneousConnections()).thenReturn(2);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(connectionTask.getRescheduleDelay()).thenReturn(TimeDuration.minutes(15));
        when(connectionTask.getProperties()).thenReturn(Collections.emptyList());
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("occp");
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        NextExecutionSpecs nextExecSpecs = mock(NextExecutionSpecs.class);
        when(nextExecSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.minutes(60)));
        when(connectionTask.getNextExecutionSpecs()).thenReturn(nextExecSpecs);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getName()).thenReturn("sct");
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), connectionTask.getVersion())).thenReturn(Optional.of(connectionTask));
        when(connectionTaskService.findConnectionTask(connectionTask.getId())).thenReturn(Optional.of(connectionTask));
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));

        Map<String, Object> response = target("/devices/" + name + "/connectionmethods").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("connectionMethods");
        List<Map<String, Object>> connectionMethods = (List<Map<String, Object>>) response.get("connectionMethods");
        assertThat(connectionMethods).hasSize(1);
        Map<String, Object> connectionTypeJson = connectionMethods.get(0);
        assertThat(connectionTypeJson)
                .containsKey("direction")
                .containsKey("name")
                .containsKey("id")
                .containsKey("status")
                .containsKey("connectionType")
                .containsKey("comWindowStart")
                .containsKey("comWindowEnd")
                .containsKey("comPortPool")
                .containsKey("isDefault")
                .containsKey("connectionStrategy")
                .containsKey("properties")
                .containsKey("numberOfSimultaneousConnections")
                .containsKey("rescheduleRetryDelay")
                .containsKey("nextExecutionSpecs");
    }

    @Test
    public void testCreatePausedInboundConnectionMethod() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE;
        info.isDefault = false;
        info.comPortPool = "cpp";

        Device device = mock(Device.class);
        when(device.getVersion()).thenReturn(1L);
        String deviceName = "ZABF0000000";
        when(device.getName()).thenReturn(deviceName);
        when(deviceService.findAndLockDeviceByNameAndVersion(deviceName, device.getVersion())).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));

        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), connectionTask.getVersion())).thenReturn(Optional.of(connectionTask));
        when(connectionTaskService.findConnectionTask(connectionTask.getId())).thenReturn(Optional.of(connectionTask));
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);

        Response response = target("/devices/" + deviceName + "/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(connectionTask, never()).activate();
        verify(connectionTask, never()).deactivate();
        verify(inboundConnectionTaskBuilder, times(1)).setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        verify(inboundConnectionTaskBuilder, times(1)).setComPortPool(comPortPool);
    }

    @Test
    public void testCreateActiveInboundConnectionMethod() throws Exception {

        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggableClass.getName()).thenReturn("ctpc");

        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));

        Device device = mock(Device.class);
        when(device.getVersion()).thenReturn(1L);
        String deviceName = "ZABF0000000";
        when(device.getName()).thenReturn(deviceName);
        when(deviceService.findAndLockDeviceByNameAndVersion(deviceName, device.getVersion())).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);

        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");

        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());

        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), connectionTask.getVersion())).thenReturn(Optional.of(connectionTask));
        when(connectionTaskService.findConnectionTask(connectionTask.getId())).thenReturn(Optional.of(connectionTask));

        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault = false;
        info.comPortPool = "cpp";
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

        Response response = target("/devices/" + deviceName + "/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(connectionTask, never()).activate();
        verify(connectionTask, never()).deactivate();
        verify(inboundConnectionTaskBuilder, times(1)).setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        verify(inboundConnectionTaskBuilder, times(1)).setComPortPool(comPortPool);
        verify(connectionTaskService, never()).setDefaultConnectionTask(connectionTask);
    }

    @Test
    public void testCreateDefaultInboundConnectionMethod() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault = true;
        info.comPortPool = "cpp";

        Device device = mock(Device.class);
        when(device.getVersion()).thenReturn(1L);
        String deviceName = "ZABF0000000";
        when(device.getName()).thenReturn(deviceName);
        when(deviceService.findAndLockDeviceByNameAndVersion(deviceName, device.getVersion())).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), connectionTask.getVersion())).thenReturn(Optional.of(connectionTask));
        when(connectionTaskService.findConnectionTask(connectionTask.getId())).thenReturn(Optional.of(connectionTask));
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);

        Response response = target("/devices/" + deviceName + "/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(connectionTaskService).setDefaultConnectionTask(connectionTask);
    }

    @Test
    public void testUpdateAndUndefaultInboundConnectionMethod() throws Exception {
        Device device = mock(Device.class);
        when(device.getVersion()).thenReturn(1L);
        String deviceName = "ZABF0000000";
        when(device.getName()).thenReturn(deviceName);
        when(deviceService.findAndLockDeviceByNameAndVersion(deviceName, device.getVersion())).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?, ?>>asList(connectionTask));
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getId()).thenReturn(5L);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), connectionTask.getVersion())).thenReturn(Optional.of(connectionTask));
        when(connectionTaskService.findConnectionTask(connectionTask.getId())).thenReturn(Optional.of(connectionTask));
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);

        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault = false;
        info.comPortPool = "cpp";
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

        Response response = target("/devices/" + deviceName + "/connectionmethods/5").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTaskService, times(1)).clearDefaultConnectionTask(device);
    }

    @Test
    public void testUpdateOnlyClearsDefaultIfConnectionMethodWasDefaultBeforeUpdate() throws Exception {
        Device device = mock(Device.class);
        when(device.getVersion()).thenReturn(1L);
        String deviceName = "ZABF0000000";
        when(device.getName()).thenReturn(deviceName);
        when(deviceService.findAndLockDeviceByNameAndVersion(deviceName, device.getVersion())).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?, ?>>asList(connectionTask));
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getId()).thenReturn(5L);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.isDefault()).thenReturn(false);
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), connectionTask.getVersion())).thenReturn(Optional.of(connectionTask));
        when(connectionTaskService.findConnectionTask(connectionTask.getId())).thenReturn(Optional.of(connectionTask));
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);

        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault = false;
        info.comPortPool = "cpp";
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

        Response response = target("/devices/" + deviceName + "/connectionmethods/5").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTaskService, never()).clearDefaultConnectionTask(device);
    }

    @Test
    public void testComSchedulesBulkAddOnDevice() {
        BulkRequestInfo request = new BulkRequestInfo();
        request.action = "add";
        request.strategy = "keep";
        request.deviceIds = Arrays.asList(13L, 24L);
        request.scheduleIds = Arrays.asList(1L);
        Entity<BulkRequestInfo> json = Entity.json(request);
        Optional<DestinationSpec> destinationSpec = Optional.of(mock(DestinationSpec.class));
        when(messageService.getDestinationSpec(SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION)).thenReturn(destinationSpec);
        when(jsonService.serialize(any())).thenAnswer(invocation -> new ObjectMapper().writeValueAsString(invocation.getArguments()[0]));
        MessageBuilder builder = mock(MessageBuilder.class);
        when(destinationSpec.get().message(anyString())).thenReturn(builder);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(destinationSpec.get().message(stringArgumentCaptor.capture())).thenReturn(builder);
        mockAppServers(SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION);
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(comSchedule));

        Response response = target("/devices/schedules").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model(stringArgumentCaptor.getValue());
        assertThat(jsonModel.<String>get("$.action")).isEqualTo("Add");
        assertThat(jsonModel.<List>get("$.deviceIds")).containsOnly(13, 24);
        assertThat(jsonModel.<List>get("$.scheduleIds")).containsOnly(1);
    }

    @Test
    public void testComSchedulesBulkAddOnDeviceOverlappingTasks() throws Exception {
        BulkRequestInfo request = new BulkRequestInfo();
        request.action = "add";
        request.strategy = "keep";
        request.deviceIds = Arrays.asList(13L, 24L);
        request.scheduleIds = Arrays.asList(1L, 2L);
        Entity<BulkRequestInfo> json = Entity.json(request);
        Optional<DestinationSpec> destinationSpec = Optional.of(mock(DestinationSpec.class));
        when(messageService.getDestinationSpec(SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION)).thenReturn(destinationSpec);
        when(jsonService.serialize(any())).thenAnswer(invocation -> new ObjectMapper().writeValueAsString(invocation.getArguments()[0]));
        MessageBuilder builder = mock(MessageBuilder.class);
        when(destinationSpec.get().message(anyString())).thenReturn(builder);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(destinationSpec.get().message(stringArgumentCaptor.capture())).thenReturn(builder);
        mockAppServers(SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION);
        ComSchedule comSchedule = mock(ComSchedule.class);
        ComSchedule comSchedule2 = mock(ComSchedule.class);
        ComTask comTask = mock(ComTask.class);
        when(comSchedule.getComTasks()).thenReturn(Collections.singletonList(comTask));
        when(comSchedule2.getComTasks()).thenReturn(Collections.singletonList(comTask));
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(comSchedule));
        when(schedulingService.findSchedule(2L)).thenReturn(Optional.of(comSchedule2));

        Response response = target("/devices/schedules").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("There are overlapping communication tasks in the schedules");
        assertThat(jsonModel.<String>get("$.error")).isEqualTo("OverlappingComTasks");
    }

    @Test
    public void testComSchedulesBulkAddOnDeviceWithFilter() throws Exception {
        BulkRequestInfo request = new BulkRequestInfo();
        request.action = "add";
        request.strategy = "keep";
        request.filter = "[{'property':'name','value':[{'operator':'==','criteria':'DAO*'}]},{'property':'deviceType','value':[{'operator':'==','criteria':['1','2','3']}]}]".replace('\'', '"');
        request.scheduleIds = Arrays.asList(1L);
        Entity<BulkRequestInfo> json = Entity.json(request);
        Optional<DestinationSpec> destinationSpec = Optional.of(mock(DestinationSpec.class));
        when(messageService.getDestinationSpec(SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION)).thenReturn(destinationSpec);
        when(jsonService.serialize(any())).thenAnswer(invocation -> new ObjectMapper().writeValueAsString(invocation.getArguments()[0]));
        DeviceSearchDomain searchDomain = mock(DeviceSearchDomain.class);
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));
        SearchableProperty mridProperty = mock(SearchableProperty.class);
        when(mridProperty.getName()).thenReturn("mRID");
        SearchableProperty deviceTypeProperty = mock(SearchableProperty.class);
        when(deviceTypeProperty.getName()).thenReturn("deviceType");
        when(deviceTypeProperty.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(searchDomain.getProperties()).thenReturn(Arrays.asList(mridProperty, deviceTypeProperty));
        SearchablePropertyValue.ValueBean mridBean = new SearchablePropertyValue.ValueBean();
        mridBean.propertyName = "name";
        mridBean.operator = SearchablePropertyOperator.EQUAL;
        mridBean.values = Collections.singletonList("DAO*");
        SearchablePropertyValue.ValueBean deviceTypeBean = new SearchablePropertyValue.ValueBean();
        deviceTypeBean.propertyName = "deviceType";
        deviceTypeBean.operator = SearchablePropertyOperator.EQUAL;
        deviceTypeBean.values = Arrays.asList("1", "2", "3");
        when(searchDomain.getPropertiesValues(Matchers.any(Function.class)))
                .thenReturn(Arrays.asList(
                        new SearchablePropertyValue(mridProperty, mridBean),
                        new SearchablePropertyValue(deviceTypeProperty, deviceTypeBean)
                ));
        MessageBuilder builder = mock(MessageBuilder.class);
        when(destinationSpec.get().message(anyString())).thenReturn(builder);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(destinationSpec.get().message(stringArgumentCaptor.capture())).thenReturn(builder);
        mockAppServers(SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION);
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(comSchedule));
        Response response = target("/devices/schedules").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model(stringArgumentCaptor.getValue());
        assertThat(jsonModel.<String>get("$.action")).isEqualTo("Add");
        assertThat(jsonModel.<List>get("$.deviceIds")).isNull();
        assertThat(jsonModel.<Map>get("$.filter.properties")).hasSize(2);
        assertThat(jsonModel.<List<String>>get("$.filter.properties.deviceType.values[*]")).containsExactly("1", "2", "3");
        assertThat(jsonModel.<List<Integer>>get("$.scheduleIds")).containsOnly(1);
    }

    @Test
    public void testComSchedulesBulkRemoveFromDeviceWithFilter() throws Exception {
        BulkRequestInfo request = new BulkRequestInfo();
        request.action = "remove";
        request.filter = "[{'property':'serialNumber','value': [{'operator': '==', 'criteria': '*001'}]}]".replace('\'', '"');
        request.scheduleIds = Arrays.asList(1L);
        Entity<BulkRequestInfo> json = Entity.json(request);
        Optional<DestinationSpec> destinationSpec = Optional.of(mock(DestinationSpec.class));
        when(messageService.getDestinationSpec(SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION)).thenReturn(destinationSpec);
        when(jsonService.serialize(any())).thenAnswer(invocation -> new ObjectMapper().writeValueAsString(invocation.getArguments()[0]));
        DeviceSearchDomain searchDomain = mock(DeviceSearchDomain.class);
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));
        SearchableProperty serialNumberProperty = mock(SearchableProperty.class);
        when(serialNumberProperty.getName()).thenReturn("serialNumber");
        when(searchDomain.getProperties()).thenReturn(Collections.singletonList(serialNumberProperty));
        SearchablePropertyValue.ValueBean serialNumberBean = new SearchablePropertyValue.ValueBean();
        serialNumberBean.propertyName = "serialNumber";
        serialNumberBean.operator = SearchablePropertyOperator.EQUAL;
        serialNumberBean.values = Collections.singletonList("*001");
        when(searchDomain.getPropertiesValues(Matchers.any(Function.class))).thenReturn(Collections.singletonList(new SearchablePropertyValue(serialNumberProperty, serialNumberBean)));
        MessageBuilder builder = mock(MessageBuilder.class);
        when(destinationSpec.get().message(anyString())).thenReturn(builder);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(destinationSpec.get().message(stringArgumentCaptor.capture())).thenReturn(builder);
        mockAppServers(SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION);
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(comSchedule));

        Response response = target("/devices/schedules").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model(stringArgumentCaptor.getValue());
        System.out.println(stringArgumentCaptor.getValue());
        assertThat(jsonModel.<String>get("$.action")).isEqualTo("Remove");
        assertThat(jsonModel.<List>get("$.deviceIds")).isNull();
        assertThat(jsonModel.<String>get("$.filter.properties.serialNumber.propertyName")).isEqualTo(serialNumberBean.propertyName);
        assertThat(jsonModel.<String>get("$.filter.properties.serialNumber.operator")).isEqualTo(serialNumberBean.operator.name());
        assertThat(jsonModel.<String>get("$.filter.properties.serialNumber.values[0]")).isEqualTo("*001");
        assertThat(jsonModel.<List<Integer>>get("$.scheduleIds")).containsOnly(1);
    }

    @Test
    public void testComSchedulesBulkWithoutAction() throws Exception {
        BulkRequestInfo request = new BulkRequestInfo();
        request.action = null;
        request.filter = ExtjsFilter.filter("serialNumber", "*001");
        request.scheduleIds = Arrays.asList(1L);
        Entity<BulkRequestInfo> json = Entity.json(request);
        Optional<DestinationSpec> destinationSpec = Optional.of(mock(DestinationSpec.class));
        when(messageService.getDestinationSpec(SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION)).thenReturn(destinationSpec);
        mockAppServers(SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION);

        Response response = target("/devices/schedules").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllLoadProfiles() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("channel1", "1.1.1", 1);
        LoadProfile loadProfile1 = mockLoadProfile("lp3", 3, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile2 = mockLoadProfile("Lp2", 2, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp1", 1, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES), channel1);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(loadProfile1.getDevice()).thenReturn(device1);
        when(loadProfile2.getDevice()).thenReturn(device1);
        when(loadProfile3.getDevice()).thenReturn(device1);
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device1));
        doReturn("translated").when(thesaurus).getString(anyString(), anyString());
        when(channel1.getReadingType()).thenReturn(readingType);

        Map response = target("/devices/name/loadprofiles").request().get(Map.class);
        assertThat(response).containsKey("total").containsKey("loadProfiles");
        assertThat((List) response.get("loadProfiles")).isSortedAccordingTo(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((String) o1.get("name")).compareToIgnoreCase((String) o2.get("name"));
            }
        });
    }

    @Test
    public void testGetAllLoadProfilesIsSorted() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("channel1", "1.1.1", 1);
        LoadProfile loadProfile1 = mockLoadProfile("lp3", 3, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile2 = mockLoadProfile("Lp2", 2, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp1", 1, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES), channel1);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device1));
        doReturn("translated").when(thesaurus).getString(anyString(), anyString());
    }

    @Test
    public void testGetOneLoadProfile() throws Exception {
        Device device1 = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);
        when(device1.getDeviceType()).thenReturn(deviceType);
        when(deviceType.isDataloggerSlave()).thenReturn(false);
        Channel channel1 = mockChannel("Z-channel1", "1.1", 0);
        Channel channel2 = mockChannel("A-channel2", "1.2", 1);
        LoadProfile loadProfile1 = mockLoadProfile("lp1", 1, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES), channel1, channel2);
        LoadProfile loadProfile2 = mockLoadProfile("lp2", 2, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(device1.getMultiplier()).thenReturn(BigDecimal.ONE);
        when(loadProfile1.getDevice()).thenReturn(device1);
        when(loadProfile2.getDevice()).thenReturn(device1);
        when(loadProfile3.getDevice()).thenReturn(device1);
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device1));
        doReturn("translated").when(thesaurus).getString(anyString(), anyString());
        when(clock.instant()).thenReturn(NOW);
        when(channel1.getDevice()).thenReturn(device1);
        when(channel2.getDevice()).thenReturn(device1);
        when(channel1.getLastDateTime()).thenReturn(Optional.empty());
        when(channel2.getLastDateTime()).thenReturn(Optional.empty());
        when(channel1.getLoadProfile()).thenReturn(loadProfile1);
        when(channel1.getOverflow()).thenReturn(Optional.empty());
        when(channel2.getLoadProfile()).thenReturn(loadProfile1);
        when(channel2.getOverflow()).thenReturn(Optional.empty());
        when(channel2.getCalculatedReadingType(clock.instant())).thenReturn(Optional.empty());
        when(channel1.getCalculatedReadingType(clock.instant())).thenReturn(Optional.empty());
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(device1.forValidation()).thenReturn(deviceValidation);
        when(channel1.getReadingType()).thenReturn(readingType);
        when(channel1.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());
        when(channel2.getReadingType()).thenReturn(readingType);
        when(channel2.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());

        Map<String, Object> response = target("/devices/name/loadprofiles/1").request().get(Map.class);
        assertThat(response)
                .hasSize(9)
                .contains(MapEntry.entry("id", 1))
                .contains(MapEntry.entry("name", "lp1"))
                .contains(MapEntry.entry("lastReading", 1406617200000L))
                .contains(MapEntry.entry("obisCode", "1.2.3.4.5.1"))
                .containsKey("channels")
                .containsKey("validationInfo")
                .containsKey("interval")
                .containsKey("parent")
                .containsKey("version");
        Map<String, Object> interval = (Map<String, Object>) response.get("interval");
        assertThat(interval)
                .contains(MapEntry.entry("count", 15))
                .contains(MapEntry.entry("timeUnit", "minutes"));

        List<Map<String, Object>> channels = (List<Map<String, Object>>) response.get("channels");
        assertThat(channels).hasSize(2);
        assertThat(channels.get(0).get("name")).isEqualTo("A-channel2");
        assertThat(channels.get(1).get("name")).isEqualTo("Z-channel1");
    }

    @Test
    public void testGetNonExistingLoadProfile() throws Exception {
        Device device1 = mock(Device.class);
        LoadProfile loadProfile1 = mockLoadProfile("lp1", 1, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile2 = mockLoadProfile("lp2", 2, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device1));
        doReturn("translated").when(thesaurus).getString(anyString(), anyString());

        Response response = target("/devices/name/loadprofiles/7").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testLoadProfileData() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("channel1", "1.1", 0);
        Channel channel2 = mockChannel("channel2", "1.2", 1);
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES), channel1, channel2);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile3));
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device1));
        when(channel1.getDevice()).thenReturn(device1);
        when(channel2.getDevice()).thenReturn(device1);
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(device1.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(channel1, NOW)).thenReturn(true);
        when(deviceValidation.isValidationActive(channel2, NOW)).thenReturn(true);
        List<LoadProfileReading> loadProfileReadings = new ArrayList<>();
        final long startTime = 1388534400000L;
        long start = startTime;
        for (int i = 0; i < 2880; i++) {
            loadProfileReadings.add(mockLoadProfileReading(loadProfile3, Ranges.openClosed(Instant.ofEpochMilli(start), Instant.ofEpochMilli(start + 900)), channel1, channel2));
            start += 900;
        }
        when(loadProfile3.getChannelData(any(Range.class))).thenReturn(loadProfileReadings);

        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":" + startTime + "},{\"property\":\"intervalEnd\",\"value\":1391212800000}]");
        Map response = target("/devices/name/loadprofiles/3/data")
                .queryParam("filter", filter)
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(11);
        List data = (List) response.get("data");
        assertThat(data).hasSize(10);
        assertThat((Map) data.get(0))
                .containsKey("interval")
                .containsKey("channelData")
                .containsKey("readingTime")
                .containsKey("readingQualities");

        Map<String, List<String>> readingQualitiesPerChannel = (Map<String, List<String>>) ((Map) data.get(0)).get("readingQualities");

        List<String> readingQualitiesChannel1 = readingQualitiesPerChannel.get(String.valueOf(channel1.getId()));
        assertThat(readingQualitiesChannel1).hasSize(1);
        assertThat(readingQualitiesChannel1.get(0)).isEqualTo("RAM checksum error");

        List<String> readingQualitiesChannel2 = readingQualitiesPerChannel.get(String.valueOf(channel2.getId()));
        assertThat(readingQualitiesChannel2).hasSize(1);
        assertThat(readingQualitiesChannel2.get(0)).isEqualTo("RAM checksum error");

        Map<String, Long> interval = (Map<String, Long>) ((Map) data.get(0)).get("interval");
        assertThat(interval.get("start")).isEqualTo(startTime);
        assertThat(interval.get("end")).isEqualTo(startTime + 900);
        Map<String, BigDecimal> channelData = (Map<String, BigDecimal>) ((Map) data.get(0)).get("channelData");
        assertThat(channelData).hasSize(2).containsKey("0").containsKey("1");
    }

    @Test
    public void testLoadProfileChannelData() throws Exception {
        Device device1 = mock(Device.class);
        when(device1.getMultiplier()).thenReturn(BigDecimal.ONE);
        long channel_id = 7L;
        Channel channel1 = mockChannel("channel1", "1.1", channel_id);
        when(channel1.getDevice()).thenReturn(device1);
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(device1.forValidation()).thenReturn(deviceValidation);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getId()).thenReturn(channel_id);
        when(channel1.getChannelSpec()).thenReturn(channelSpec);
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES), channel1);
        when(channel1.getLoadProfile()).thenReturn(loadProfile3);
        when(channel1.getReadingType()).thenReturn(readingType);
        when(channel1.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile3));
        when(device1.getChannels()).thenReturn(Arrays.asList(channel1));
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device1));
        List<LoadProfileReading> loadProfileReadings = new ArrayList<>();
        final long startTime = 1388534400000L;
        long start = startTime;
        for (int i = 0; i < 2880; i++) {
            loadProfileReadings.add(mockLoadProfileReading(loadProfile3, Ranges.openClosed(Instant.ofEpochMilli(start), Instant.ofEpochMilli(start + 900)), channel1));
            start += 900;
        }
        when(channel1.getChannelData(any(Range.class))).thenReturn(loadProfileReadings);
        Range<Instant> range = Range.closedOpen(Instant.ofEpochMilli(1410774630000L), Instant.ofEpochMilli(1410828630000L));
        when(topologyService.getDataLoggerChannelTimeLine(any(Channel.class), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(channel1, range)));

        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":1410774630000},{\"property\":\"intervalEnd\",\"value\":1410828630000}]");
        Map response = target("/devices/name/channels/7/data")
                .queryParam("filter", filter)
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(11);
        List data = (List) response.get("data");
        assertThat(data).hasSize(10);
        assertThat((Map) data.get(0))
                .containsKey("interval")
                .containsKey("value")
                .containsKey("readingTime")
                .containsKey("readingQualities");
        assertThat(((List<String>) ((Map) data.get(0)).get("readingQualities")).get(0)).isEqualTo("RAM checksum error");
        Map<String, Long> interval = (Map<String, Long>) ((Map) data.get(0)).get("interval");
        assertThat(interval.get("start")).isEqualTo(startTime);
        assertThat(interval.get("end")).isEqualTo(startTime + 900);
    }

    @Test
    public void testGetAllLogBooksReturnsEmptyList() {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        Map response = target("/devices/name/logbooks").queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        assertThat(response)
                .contains(MapEntry.entry("total", 0))
                .containsKey("data");
    }

    @Test
    public void testGetAllLogBooks() {
        Instant lastLogBook = Instant.now();
        Instant lastReading = Instant.now();

        Device device = mock(Device.class);
        List<LogBook> logBooks = new ArrayList<>();
        logBooks.add(mockLogBook("C_LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", lastLogBook, lastReading));
        logBooks.add(mockLogBook("B_LogBook", 2L, "0.0.0.0.0.3", "0.0.0.0.0.4", lastLogBook, lastReading));
        logBooks.add(mockLogBook("A_LogBook", 3L, "0.0.0.0.0.5", "0.0.0.0.0.6", lastLogBook, lastReading));

        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(logBooks);

        Map response = target("/devices/name/logbooks").queryParam("start", 0).queryParam("limit", 2).request().get(Map.class);

        assertThat(response)
                .contains(MapEntry.entry("total", 3))
                .containsKey("data");

        assertThat((List) response.get("data")).hasSize(2);

        Map logBookInfo1 = (Map) ((List) response.get("data")).get(0);
        assertThat(logBookInfo1)
                .contains(MapEntry.entry("id", 3))
                .contains(MapEntry.entry("name", "A_LogBook"))
                .contains(MapEntry.entry("obisCode", "0.0.0.0.0.5"))
                .contains(MapEntry.entry("overruledObisCode", "0.0.0.0.0.6"))
                .contains(MapEntry.entry("lastEventDate", lastLogBook.toEpochMilli()))
                .contains(MapEntry.entry("lastReading", lastReading.toEpochMilli()));

        Map logBookInfo2 = (Map) ((List) response.get("data")).get(1);
        assertThat(logBookInfo2)
                .contains(MapEntry.entry("id", 2))
                .contains(MapEntry.entry("name", "B_LogBook"))
                .contains(MapEntry.entry("obisCode", "0.0.0.0.0.3"))
                .contains(MapEntry.entry("overruledObisCode", "0.0.0.0.0.4"))
                .contains(MapEntry.entry("lastEventDate", lastLogBook.toEpochMilli()))
                .contains(MapEntry.entry("lastReading", lastReading.toEpochMilli()));
    }

    @Test
    public void testGetLogBookById() {
        Instant lastLogBook = Instant.now();
        Instant lastReading = Instant.now();

        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", lastLogBook, lastReading);
        EndDeviceEventRecord endDeviceEvent = mock(EndDeviceEventRecord.class);
        EndDeviceEventType endDeviceEventType = mock(EndDeviceEventType.class);

        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));
        when(logBook.getEndDeviceEvents(Matchers.any(Range.class))).thenReturn(Arrays.asList(endDeviceEvent));
        when(endDeviceEvent.getEventType()).thenReturn(endDeviceEventType);
        when(endDeviceEventType.getMRID()).thenReturn("0.2.38.57");
        when(endDeviceEventType.getType()).thenReturn(EndDeviceType.NA);
        when(endDeviceEventType.getDomain()).thenReturn(EndDeviceDomain.BATTERY);
        when(endDeviceEventType.getSubDomain()).thenReturn(EndDeviceSubDomain.VOLTAGE);
        when(endDeviceEventType.getEventOrAction()).thenReturn(EndDeviceEventOrAction.DECREASED);
        when(nlsService.getThesaurus(Matchers.anyString(), Matchers.<Layer>any())).thenReturn(thesaurus);

        LogBookInfo info = target("/devices/name/logbooks/1").request().get(LogBookInfo.class);

        assertThat(info.id).isEqualTo(1);
        assertThat(info.name).isEqualTo("LogBook");
        assertThat(info.obisCode).isEqualTo(ObisCode.fromString("0.0.0.0.0.1"));
        assertThat(info.overruledObisCode).isEqualTo(ObisCode.fromString("0.0.0.0.0.2"));
        assertThat(info.lastEventDate).isEqualTo(lastLogBook);
        assertThat(info.lastReading).isEqualTo(lastReading);

        EndDeviceEventTypeInfo eventType = info.lastEventType;
        assertThat(eventType.code).isEqualTo("0.2.38.57");
        assertThat(eventType.deviceType.id).isEqualTo(0);
        assertThat(eventType.deviceType.name).isEqualTo("NA");
        assertThat(eventType.domain.id).isEqualTo(2);
        assertThat(eventType.domain.name).isEqualTo("Battery");
        assertThat(eventType.subDomain.id).isEqualTo(38);
        assertThat(eventType.subDomain.name).isEqualTo("Voltage");
        assertThat(eventType.eventOrAction.id).isEqualTo(57);
        assertThat(eventType.eventOrAction.name).isEqualTo("Decreased");
    }

    @Test
    public void testGetLogBookByIdNotFound() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/name/logbooks/134").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookDataIncorrectIntervalParameter() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/name/logbooks/1/data").queryParam("filter", "[%7B%22property%22:%22intervalStart%22,%22value%22:2%7D,%7B%22property%22:%22intervalEnd%22,%22value%22:1%7D]")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookDataInvalidDomainParameter() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/name/logbooks/1/data").queryParam("filter", "[%7B%22property%22:%22domain%22,%22value%22:%22100500%22%7D]").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookDataInvalidSubDomainParameter() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/name/logbooks/1/data").queryParam("filter", "[%7B%22property%22:%22subDomain%22,%22value%22:%22100500%22%7D]").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookDataInvalidEventOrActionParameter() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/name/logbooks/1/data").queryParam("filter", "[%7B%22property%22:%22eventOrAction%22,%22value%22:%22100500%22%7D]").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookData() {
        Instant start = Instant.now();
        Instant end = Instant.now();
        String message = "Message";
        int eventLogId = 13;
        String deviceCode = "DeviceEventType";
        String eventTypeCode = "0.2.1.4";
        EndDeviceType type = EndDeviceType.NA;
        EndDeviceDomain domain = EndDeviceDomain.BATTERY;
        EndDeviceSubDomain subDomain = EndDeviceSubDomain.ACCESS;
        EndDeviceEventOrAction eventorAction = EndDeviceEventOrAction.ACTIVATED;

        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);
        EndDeviceEventRecord endDeviceEventRecord = mock(EndDeviceEventRecord.class);
        EndDeviceEventType endDeviceType = mock(EndDeviceEventType.class);

        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));
        List<EndDeviceEventRecord> records = new ArrayList<>();
        records.add(endDeviceEventRecord);
        when(logBook.getEndDeviceEventsByFilter(Matchers.<EndDeviceEventRecordFilterSpecification>any())).thenReturn(records);
        when(endDeviceEventRecord.getCreatedDateTime()).thenReturn(start);
        when(endDeviceEventRecord.getModTime()).thenReturn(end);
        when(endDeviceEventRecord.getDescription()).thenReturn(message);
        when(endDeviceEventRecord.getLogBookPosition()).thenReturn(eventLogId);
        when(endDeviceEventRecord.getEventType()).thenReturn(endDeviceType);
        when(endDeviceEventRecord.getDeviceEventType()).thenReturn(deviceCode);
        when(endDeviceType.getMRID()).thenReturn(eventTypeCode);
        when(endDeviceType.getType()).thenReturn(type);
        when(endDeviceType.getDomain()).thenReturn(domain);
        when(endDeviceType.getSubDomain()).thenReturn(subDomain);
        when(endDeviceType.getEventOrAction()).thenReturn(eventorAction);

        when(nlsService.getThesaurus(Matchers.anyString(), Matchers.<Layer>any())).thenReturn(thesaurus);

        Map<?, ?> response = target("/devices/name/logbooks/1/data")
                .queryParam("filter", ("[{\"property\":\"intervalStart\",\"value\":1},"
                        + "{\"property\":\"intervalEnd\",\"value\":2},"
                        + "{\"property\":\"domain\",\"value\":\"BATTERY\"},"
                        + "{\"property\":\"subDomain\",\"value\":\"ACCESS\"},"
                        + "{\"property\":\"eventOrAction\",\"value\":\"ACTIVATED\"}]").replace("{", "%7B").replace("}", "%7D").replace("\"", "%22")).request().get(Map.class);

        assertThat(response.get("total")).isEqualTo(1);

        List<?> infos = (List<?>) response.get("data");
        assertThat(infos).hasSize(1);
        assertThat((Map<String, Object>) infos.get(0))
                .contains(MapEntry.entry("eventDate", start.toEpochMilli()))
                .contains(MapEntry.entry("deviceCode", deviceCode))
                .contains(MapEntry.entry("eventLogId", eventLogId))
                .contains(MapEntry.entry("readingDate", end.toEpochMilli()))
                .contains(MapEntry.entry("message", message));

        Map<String, Object> eventType = (Map<String, Object>) ((Map<String, Object>) infos.get(0)).get("eventType");
        assertThat(eventType).contains(MapEntry.entry("code", eventTypeCode));
        Map<String, Object> deviceTypeMap = (Map<String, Object>) eventType.get("deviceType");
        assertThat(deviceTypeMap).contains(MapEntry.entry("id", type.getValue())).contains(MapEntry.entry("name", type.getMnemonic()));

        Map<String, Object> domainTypeMap = (Map<String, Object>) eventType.get("domain");
        assertThat(domainTypeMap).contains(MapEntry.entry("id", domain.getValue())).contains(MapEntry.entry("name", domain.getMnemonic()));

        Map<String, Object> subDomainMap = (Map<String, Object>) eventType.get("subDomain");
        assertThat(subDomainMap).contains(MapEntry.entry("id", subDomain.getValue())).contains(MapEntry.entry("name", subDomain.getMnemonic()));

        Map<String, Object> eventOrActionMap = (Map<String, Object>) eventType.get("eventOrAction");
        assertThat(eventOrActionMap).contains(MapEntry.entry("id", eventorAction.getValue())).contains(MapEntry.entry("name", eventorAction.getMnemonic()));
    }

    @Test
    public void testGetCommunicationTopology() {
        when(clock.instant()).thenReturn(NOW);
        int limit = 10;
        mockTopologyTimeline(limit);

        String response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(7);
        assertThat(model.<List>get("$.slaveDevices")).hasSize(7);
        assertThat(model.<String>get("$.slaveDevices[0].name")).isEqualTo("slave1");
        assertThat(model.<String>get("$.slaveDevices[6].name")).isEqualTo("slave7");
    }

    @Test
    public void testCommunicationTopologyPaging() {
        when(clock.instant()).thenReturn(NOW);
        int limit = 2;
        mockTopologyTimeline(limit);
        String response = target("/devices/gateway/topology/communication")
                .queryParam("start", 3).queryParam("limit", limit)
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(6); // 3 (start) + 2 (limit) + 1 (for FE)
        assertThat(model.<List>get("$.slaveDevices")).hasSize(limit);
        assertThat(model.<String>get("$.slaveDevices[0].name")).isEqualTo("slave4");
        assertThat(model.<String>get("$.slaveDevices[1].name")).isEqualTo("slave5");
    }

    @Test
    public void testGetCommunicationTopologyPagingBigStart() {
        int limit = 2;
        mockTopologyTimeline(limit);
        String response = target("/devices/gateway/topology/communication")
                .queryParam("start", 1000).queryParam("limit", limit)
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(1000); // 1000 (start) + 0 (limit) + 0 (for FE: no additional pages)
        assertThat(model.<List>get("$.slaveDevices")).hasSize(0);
    }

    @Test
    public void testGetCommunicationTopologyPagingBigEnd() {
        when(clock.instant()).thenReturn(NOW);
        int limit = 1000;
        mockTopologyTimeline(limit);
        String response = target("/devices/gateway/topology/communication")
                .queryParam("start", 6).queryParam("limit", limit)
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(7);
        assertThat(model.<List>get("$.slaveDevices")).hasSize(1);
    }

    @Test
    public void testGetCommunicationTopologyNoPaging() {
        when(clock.instant()).thenReturn(NOW);
        mockTopologyTimeline(Integer.MAX_VALUE);
        String response = target("/devices/gateway/topology/communication")
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(7);
        assertThat(model.<List>get("$.slaveDevices")).hasSize(7);
    }

    private void mockTopologyTimeline(int limit) {
        Device gateway = mockDeviceForTopologyTest("gateway");
        Device slave1 = mockDeviceForTopologyTest("slave1", gateway);
        Device slave2 = mockDeviceForTopologyTest("slave2", gateway);
        Device slave3 = mockDeviceForTopologyTest("slave3", gateway);
        Device slave4 = mockDeviceForTopologyTest("slave4", gateway);
        Device slave5 = mockDeviceForTopologyTest("slave5", gateway);
        Device slave6 = mockDeviceForTopologyTest("slave6", gateway);
        Device slave7 = mockDeviceForTopologyTest("slave7", gateway);
        Set<Device> slaves = new HashSet<>(Arrays.<Device>asList(slave5, slave2, slave7, slave4, slave1, slave6, slave3));

        TopologyTimeline topologyTimeline = mock(TopologyTimeline.class);
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        when(topologyTimeline.mostRecentlyAddedOn(slave1)).thenReturn(Optional.of(Instant.ofEpochMilli(10L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave2)).thenReturn(Optional.of(Instant.ofEpochMilli(20L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave3)).thenReturn(Optional.of(Instant.ofEpochMilli(30L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave4)).thenReturn(Optional.of(Instant.ofEpochMilli(40L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave5)).thenReturn(Optional.of(Instant.ofEpochMilli(50L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave6)).thenReturn(Optional.of(Instant.ofEpochMilli(60L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave7)).thenReturn(Optional.of(Instant.ofEpochMilli(70L)));

        DeviceTopology deviceTopology = mock(DeviceTopology.class);
        when(deviceService.findDeviceByName("gateway")).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalTopology(gateway, Range.atMost(NOW))).thenReturn(deviceTopology);
        when(deviceTopology.timelined()).thenReturn(topologyTimeline);
        when(topologyService.getPysicalTopologyTimeline(gateway)).thenReturn(topologyTimeline);
        when(topologyService.getPhysicalTopologyTimelineAdditions(gateway, limit)).thenReturn(topologyTimeline);
    }

    @Test
    public void testGetCommunicationTopologyFilter() throws Exception {
        when(clock.instant()).thenReturn(NOW);
        Device gateway = mockDeviceForTopologyTest("gateway");
        Device slave1 = mockDeviceForTopologyTest("SimpleStringName", gateway);
        Device slave2 = mockDeviceForTopologyTest("123456789", gateway);
        when(slave2.getSerialNumber()).thenReturn(null);
        Set<Device> slaves = new HashSet<>(Arrays.<Device>asList(slave1, slave2));

        int limit = 10;
        DeviceTopology deviceTopology = mock(DeviceTopology.class);

        TopologyTimeline topologyTimeline = mock(TopologyTimeline.class);
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        when(topologyTimeline.mostRecentlyAddedOn(slave1)).thenReturn(Optional.of(Instant.ofEpochMilli(10L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave2)).thenReturn(Optional.of(Instant.ofEpochMilli(20L)));
        when(deviceTopology.timelined()).thenReturn(topologyTimeline);

        when(deviceService.findDeviceByName("gateway")).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalTopology(gateway, Range.atMost(NOW))).thenReturn(deviceTopology);
        when(topologyService.getPysicalTopologyTimeline(gateway)).thenReturn(topologyTimeline);
        when(topologyService.getPhysicalTopologyTimelineAdditions(gateway, limit)).thenReturn(topologyTimeline);


        Map<?, ?> response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"name\",\"value\":\"*\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"name\",\"value\":\"%\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"name\",\"value\":\"Simple%Name\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"name\",\"value\":\"Simple?Name\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(0);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"name\",\"value\":\"1234*\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"name\",\"value\":\"*789\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"name\",\"value\":\"%34*7?9\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);
    }

    @Test
    public void testGetCommunicationTopologyFilterOnSerialNumber() throws Exception {
        when(clock.instant()).thenReturn(NOW);
        Device gateway = mockDeviceForTopologyTest("gateway");
        Device slave1 = mockDeviceForTopologyTest("SimpleStringName", gateway);
        Device slave2 = mockDeviceForTopologyTest("123456789", gateway);
        when(slave2.getSerialNumber()).thenReturn(null);
        Set<Device> slaves = new HashSet<>(Arrays.<Device>asList(slave1, slave2));
        int limit = 10;

        DeviceTopology deviceTopology = mock(DeviceTopology.class);
        TopologyTimeline topologyTimeline = mock(TopologyTimeline.class);
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        when(topologyTimeline.mostRecentlyAddedOn(slave1)).thenReturn(Optional.of(Instant.ofEpochMilli(10L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave2)).thenReturn(Optional.of(Instant.ofEpochMilli(20L)));

        when(deviceTopology.timelined()).thenReturn(topologyTimeline);

        when(deviceService.findDeviceByName("gateway")).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalTopology(gateway, Range.atMost(NOW))).thenReturn(deviceTopology);
        when(topologyService.getPysicalTopologyTimeline(gateway)).thenReturn(topologyTimeline);
        when(topologyService.getPhysicalTopologyTimelineAdditions(gateway, limit)).thenReturn(topologyTimeline);


        Map<?, ?> response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"*\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"%\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"D(E%\\\\Q\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(0);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"123456?89\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"1234*\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"*789\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", limit)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"%34*7?9\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);
    }

    @Test
    public void testDeviceTopologyInfo() {
        when(clock.instant()).thenReturn(NOW);
        Device gateway = mockDeviceForTopologyTest("gateway");
        Device slave1 = mockDeviceForTopologyTest("slave1", gateway);
        Device slave2 = mockDeviceForTopologyTest("slave2", gateway);
        Device slave3 = mockDeviceForTopologyTest("slave3", gateway);
        Device slave4 = mockDeviceForTopologyTest("slave4", gateway);
        Device slave5 = mockDeviceForTopologyTest("slave5", gateway);
        Device slave6 = mockDeviceForTopologyTest("slave6", gateway);
        Device slave7 = mockDeviceForTopologyTest("slave7", gateway);
        Set<Device> slaves = new HashSet<>(Arrays.<Device>asList(slave3, slave4, slave5, slave6, slave7));
        DeviceTopology deviceTopology = mock(DeviceTopology.class);
        when(deviceService.findDeviceByName("gateway")).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalTopology(gateway, Range.atMost(NOW))).thenReturn(deviceTopology);

        TopologyTimeline topologyTimeline = mock(TopologyTimeline.class);
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        when(topologyTimeline.mostRecentlyAddedOn(slave1)).thenReturn(Optional.of(Instant.ofEpochMilli(10L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave2)).thenReturn(Optional.of(Instant.ofEpochMilli(20L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave3)).thenReturn(Optional.of(Instant.ofEpochMilli(30L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave4)).thenReturn(Optional.of(Instant.ofEpochMilli(40L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave5)).thenReturn(Optional.of(Instant.ofEpochMilli(50L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave6)).thenReturn(Optional.of(Instant.ofEpochMilli(60L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave7)).thenReturn(Optional.of(Instant.ofEpochMilli(70L)));
        when(deviceTopology.timelined()).thenReturn(topologyTimeline);
        when(topologyService.getPysicalTopologyTimeline(gateway)).thenReturn(topologyTimeline);

        List<DeviceTopologyInfo> infos = DeviceTopologyInfo.from(topologyTimeline, deviceLifeCycleConfigurationService);

        assertThat(infos.size()).isEqualTo(5);
        assertThat(infos.get(0).name).isEqualTo("slave7");
        assertThat(infos.get(1).name).isEqualTo("slave6");
        assertThat(infos.get(2).name).isEqualTo("slave5");
        assertThat(infos.get(3).name).isEqualTo("slave4");
        assertThat(infos.get(4).name).isEqualTo("slave3");

        slaves = new HashSet<>(Arrays.<Device>asList(slave1));
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        infos = DeviceTopologyInfo.from(topologyTimeline, deviceLifeCycleConfigurationService);
        assertThat(infos.size()).isEqualTo(1);
    }

    @Test
    public void testUpdateMasterDevice() {
        Device device = mockDeviceForTopologyTest("device");
        when(device.getLocation()).thenReturn(Optional.empty());
        when(device.getSpatialCoordinates()).thenReturn(Optional.empty());
        DeviceConfiguration deviceConfig = device.getDeviceConfiguration();
        when(device.getUsagePoint()).thenReturn(Optional.empty());
        Device gateway = mockDeviceForTopologyTest("gateway");
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(device.getBatch()).thenReturn(Optional.empty());
        Device oldGateway = mockDeviceForTopologyTest("oldGateway");
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(oldGateway));
        when(locationService.findLocationById(anyLong())).thenReturn(Optional.empty());
        when(deviceConfigurationService.findTimeOfUseOptions(any())).thenReturn(Optional.empty());

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.masterDeviceId = gateway.getId();
        info.masterDeviceName = gateway.getName();
        info.name = "device";
        info.parent = new VersionInfo<>(1L, 1L);
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(device)).thenReturn(topologyTimeLine);
        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(topologyService).setPhysicalGateway(device, gateway);
    }

    @Test
    public void testImpossibleToSetMasterDeviceBecauseItIsGateway() {
        Device device = mockDeviceForTopologyTest("device");
        DeviceConfiguration deviceConfig = device.getDeviceConfiguration();
        when(deviceConfig.isDirectlyAddressable()).thenReturn(true);
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));

        DeviceInfo info = new DeviceInfo();
        info.version = 13l;
        info.masterDeviceId = 2L;
        info.masterDeviceName = "2";
        info.name = "device";
        info.parent = new VersionInfo<>(1L, 1L);

        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDeleteMasterDevice() {
        Device device = mockDeviceForTopologyTest("device");
        when(device.getLocation()).thenReturn(Optional.empty());
        when(device.getSpatialCoordinates()).thenReturn(Optional.empty());
        when(device.getUsagePoint()).thenReturn(Optional.empty());
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfiguration));
        when(device.getLocation()).thenReturn(Optional.empty());
        when(device.getSpatialCoordinates()).thenReturn(Optional.empty());
        when(deviceConfigurationService.findTimeOfUseOptions(any())).thenReturn(Optional.empty());

        when(device.getBatch()).thenReturn(Optional.empty());
        Device oldMaster = mock(Device.class);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(oldMaster));

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13l;
        info.masterDeviceName = null;
        info.name = "device";
        info.parent = new VersionInfo<>(1L, 1L);
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(device)).thenReturn(topologyTimeLine);
        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(topologyService).clearPhysicalGateway(device);
    }

    @Test
    public void testLinkFirstSlaveToDataLogger() {
        when(clock.instant()).thenReturn(NOW);
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");
        Channel dataLoggerChannel = prepareMockedChannel(mock(Channel.class));
        when(dataLoggerChannel.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChannel.getId()).thenReturn(2L);
        when(dataLogger.getChannels()).thenReturn(Collections.singletonList(dataLoggerChannel));

        Device slave1 = mockDeviceForTopologyTest("slave1");
        Channel slaveChannel1 = prepareMockedChannel(mock(Channel.class));
        when(slaveChannel1.getDevice()).thenReturn(slave1);
        when(slaveChannel1.getId()).thenReturn(1L);
        when(slave1.getChannels()).thenReturn(Collections.singletonList(slaveChannel1));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getUsagePoint()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.of(slave1));

        DataLoggerSlaveChannelInfo channelMappingForSlave1 = new DataLoggerSlaveChannelInfo();
        channelMappingForSlave1.slaveChannel = newChannelInfo(1L, "firstSlave", 1L);
        channelMappingForSlave1.dataLoggerChannel = newChannelInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        slaveInfo1.id = 100L;
        slaveInfo1.name = "firstSlave";
        slaveInfo1.deviceTypeName = "firstSlaveDeviceType";
        slaveInfo1.deviceConfigurationId = 2L;
        slaveInfo1.deviceConfigurationName = "firstSlaveDeviceConfiguration";
        slaveInfo1.serialNumber = "100";
        slaveInfo1.yearOfCertification = 1960;
        slaveInfo1.version = 1;
        slaveInfo1.dataLoggerSlaveChannelInfos = Collections.singletonList(channelMappingForSlave1);
        slaveInfo1.linkingTimeStamp = Instant.now().toEpochMilli();

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);

        when(topologyService.getSlaveChannel(eq(dataLoggerChannel), any(Instant.class))).thenReturn(Optional.of(slaveChannel1));
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(dataLogger)).thenReturn(topologyTimeLine);
        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(topologyService).setDataLogger(eq(slave1), eq(dataLogger), eq(Instant.ofEpochMilli(slaveInfo1.linkingTimeStamp)), any(Map.class), any(Map.class));
    }

    @Test
    public void testLinkSlaveToDataLoggerWhileDataLoggerNotFound() throws IOException {
        when(deviceService.findDeviceByName("dataLogger")).thenReturn(Optional.empty());

        Device slave1 = mockDeviceForTopologyTest("slave1");
        Channel slaveChannel1 = prepareMockedChannel(mock(Channel.class));
        when(slaveChannel1.getDevice()).thenReturn(slave1);
        when(slaveChannel1.getId()).thenReturn(1L);
        when(slave1.getChannels()).thenReturn(Collections.singletonList(slaveChannel1));

        DataLoggerSlaveChannelInfo channelMappingForSlave1 = new DataLoggerSlaveChannelInfo();
        channelMappingForSlave1.slaveChannel = newChannelInfo(1L, "firstSlave", 1L);
        channelMappingForSlave1.dataLoggerChannel = newChannelInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        slaveInfo1.id = 100L;
        slaveInfo1.name = "firstSlave";
        slaveInfo1.deviceTypeName = "firstSlaveDeviceType";
        slaveInfo1.deviceConfigurationId = 2L;
        slaveInfo1.deviceConfigurationName = "firstSlaveDeviceConfiguration";
        slaveInfo1.serialNumber = "100";
        slaveInfo1.yearOfCertification = 1960;
        slaveInfo1.version = 1;
        slaveInfo1.dataLoggerSlaveChannelInfos = Collections.singletonList(channelMappingForSlave1);

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);

        Response response = target("/devices/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.hasEntity()).isTrue();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.error")).isEqualTo(MessageSeeds.NO_SUCH_DEVICE.getKey());
    }

    @Test
    public void testLinkSlaveToDataLoggerWhileSlaveNotFound() throws IOException {
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");
        Channel dataLoggerChannel = prepareMockedChannel(mock(Channel.class));
        when(dataLoggerChannel.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChannel.getId()).thenReturn(2L);
        when(dataLogger.getChannels()).thenReturn(Collections.singletonList(dataLoggerChannel));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getCurrentMeterActivation()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.empty()); // Slave device will not be found

        DataLoggerSlaveChannelInfo channelMappingForSlave1 = new DataLoggerSlaveChannelInfo();
        channelMappingForSlave1.slaveChannel = newChannelInfo(1L, "firstSlave", 1L);
        channelMappingForSlave1.dataLoggerChannel = newChannelInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        slaveInfo1.id = 100L;
        slaveInfo1.name = "firstSlave";
        slaveInfo1.deviceTypeName = "firstSlaveDeviceType";
        slaveInfo1.deviceConfigurationId = 2L;
        slaveInfo1.deviceConfigurationName = "firstSlaveDeviceConfiguration";
        slaveInfo1.serialNumber = "100";
        slaveInfo1.yearOfCertification = 1960;
        slaveInfo1.version = 1;
        slaveInfo1.dataLoggerSlaveChannelInfos = Collections.singletonList(channelMappingForSlave1);

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);

        Response response = target("/devices/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.hasEntity()).isTrue();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.error")).isEqualTo(MessageSeeds.NO_SUCH_DEVICE.getKey());
    }

    @Test
    public void testLinkSlaveToDataLoggerThrowsError() throws IOException {
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");
        Channel dataLoggerChannel = prepareMockedChannel(mock(Channel.class));
        when(dataLoggerChannel.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChannel.getId()).thenReturn(2L);
        when(dataLoggerChannel.getReadingType()).thenReturn(readingType);

        when(dataLogger.getChannels()).thenReturn(Collections.singletonList(dataLoggerChannel));

        Device slave1 = mockDeviceForTopologyTest("slave1");
        Channel slaveChannel1 = prepareMockedChannel(mock(Channel.class));
        when(slaveChannel1.getDevice()).thenReturn(slave1);
        when(slaveChannel1.getId()).thenReturn(1L);
        when(slave1.getChannels()).thenReturn(Collections.singletonList(slaveChannel1));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getCurrentMeterActivation()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.of(slave1));

        DataLoggerSlaveChannelInfo channelMappingForSlave1 = new DataLoggerSlaveChannelInfo();
        channelMappingForSlave1.slaveChannel = newChannelInfo(1L, "firstSlave", 1L);
        channelMappingForSlave1.dataLoggerChannel = newChannelInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        slaveInfo1.id = 100L;
        slaveInfo1.name = "firstSlave";
        slaveInfo1.deviceTypeName = "firstSlaveDeviceType";
        slaveInfo1.deviceConfigurationId = 2L;
        slaveInfo1.deviceConfigurationName = "firstSlaveDeviceConfiguration";
        slaveInfo1.serialNumber = "100";
        slaveInfo1.yearOfCertification = 1960;
        slaveInfo1.version = 1;
        slaveInfo1.dataLoggerSlaveChannelInfos = Collections.singletonList(channelMappingForSlave1);
        slaveInfo1.linkingTimeStamp = Instant.now().toEpochMilli();

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);

        when(topologyService.getSlaveChannel(eq(dataLoggerChannel), any(Instant.class))).thenReturn(Optional.of(slaveChannel1));
        Mockito.doThrow(DataLoggerLinkException.noPhysicalChannelForReadingType(thesaurus, readingType))
                .when(topologyService)
                .setDataLogger(eq(slave1), eq(dataLogger), eq(Instant.ofEpochMilli(slaveInfo1.linkingTimeStamp)), any(Map.class), any(Map.class));


        Response response = target("/devices/1").request().put(Entity.json(info));

        // Simulating a mismatch between mdc-channels and pulse channels: e.g. pulse channel having the mdc-channels' readingtype does not exist
        verify(topologyService).setDataLogger(eq(slave1), eq(dataLogger), eq(Instant.ofEpochMilli(slaveInfo1.linkingTimeStamp)), any(Map.class), any(Map.class));

        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
        assertThat(response.hasEntity()).isTrue();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.error")).isEqualTo("DataLoggerLinkException.noPhysicalSlaveChannelForReadingTypeX");
    }


    private Channel prepareMockedChannel(Channel mockedChannel) {
        ReadingType readingType = prepareMockedReadingType(mock(ReadingType.class));
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getName()).thenReturn("some loadprofile type");
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getId()).thenReturn(11L);
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);

        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getNbrOfFractionDigits()).thenReturn(2);
        when(channelSpec.isUseMultiplier()).thenReturn(false);
        when(channelSpec.getOverflow()).thenReturn(Optional.of(new BigDecimal(999999)));

        when(mockedChannel.getReadingType()).thenReturn(readingType);
        when(mockedChannel.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(mockedChannel.getLastReading()).thenReturn(Optional.empty());
        when(mockedChannel.getLastDateTime()).thenReturn(Optional.empty());
        when(mockedChannel.getCalculatedReadingType(any(Instant.class))).thenReturn(Optional.empty());
        when(mockedChannel.getOverflow()).thenReturn(Optional.empty());
        when(mockedChannel.getUnit()).thenReturn(Unit.get("kWh"));
        when(mockedChannel.getChannelSpec()).thenReturn(channelSpec);
        when(mockedChannel.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());
        when(mockedChannel.getLoadProfile()).thenReturn(loadProfile);
        when(mockedChannel.getOverflow()).thenReturn(Optional.empty());
        return mockedChannel;
    }

    private ReadingType prepareMockedReadingType(ReadingType readingType) {
        when(readingType.getMRID()).thenReturn("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        when(readingType.getName()).thenReturn("readingTypeName");
        when(readingType.isActive()).thenReturn(true);
        when(readingType.isCumulative()).thenReturn(true);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.NORMAL);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getAccumulation()).thenReturn(Accumulation.CUMULATIVE);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.NOTAPPLICABLE);
        when(readingType.getCommodity()).thenReturn(Commodity.DEVICE);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.CURRENT);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(0, 1));
        when(readingType.getArgument()).thenReturn(new RationalNumber(0, 1));
        when(readingType.getTou()).thenReturn(0);
        when(readingType.getCpp()).thenReturn(0);
        when(readingType.getConsumptionTier()).thenReturn(0);
        when(readingType.getPhases()).thenReturn(Phase.PHASES1);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        when(readingType.getVersion()).thenReturn(1L);
        return readingType;
    }

    private ChannelInfo newChannelInfo(long id, String deviceMRID, long version) {
        ChannelInfo mock = new ChannelInfo();
        mock.id = id;
        mock.interval = new TimeDurationInfo(900);
        mock.parent = new VersionInfo<String>(deviceMRID, version);
        return mock;
    }

    @Test
    public void getUnlinkedSlavesTest() throws Exception {
        Finder<DataLoggerReferenceImpl> finder = mockFinder(Collections.emptyList());
        doReturn(finder).when(topologyService).findAllEffectiveDataLoggerSlaveDevices();

        Device slave1 = mockDeviceForTopologyTest("slave1");
        Device slave2 = mockDeviceForTopologyTest("slave2");
        Finder<Device> finderDevice = mockFinder(Arrays.asList(slave1, slave2));
        doReturn(finderDevice).when(deviceService).findAllDevices(any(Condition.class));

        String response = target("devices/unlinkeddataloggerslaves")
                .queryParam("like", URLEncoder.encode("slave"))
                .request()
                .get(String.class);

        verify(topologyService, times(1)).findAllEffectiveDataLoggerSlaveDevices();
        verify(deviceService, times(1)).findAllDevices(any(Condition.class));

        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.devices")).hasSize(2);
        assertThat(model.<String>get("$.devices[0].name")).isEqualTo("slave1");
        assertThat(model.<String>get("$.devices[1].name")).isEqualTo("slave2");
    }

    @Test
    public void testLinkNewSlavesToDataLogger() {
        when(clock.instant()).thenReturn(NOW);
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");
        Channel dataLoggerChannel = prepareMockedChannel(mock(Channel.class));
        when(dataLoggerChannel.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChannel.getId()).thenReturn(2L);
        when(dataLogger.getChannels()).thenReturn(Collections.singletonList(dataLoggerChannel));

        Device slave1 = mockDeviceForTopologyTest("slave1");
        when(deviceService.newDevice(any(DeviceConfiguration.class), eq("firstSlave"), any(Instant.class))).thenReturn(slave1);
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.of(slave1));
        Channel slaveChannel1 = prepareMockedChannel(mock(Channel.class));
        when(slaveChannel1.getDevice()).thenReturn(slave1);
        when(slaveChannel1.getId()).thenReturn(1L);
        when(slave1.getChannels()).thenReturn(Collections.singletonList(slaveChannel1));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();
        DeviceConfiguration slaveDeviceConfig = mock(DeviceConfiguration.class);

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getUsagePoint()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findDeviceConfiguration(2L)).thenReturn(Optional.of(slaveDeviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.empty());

        DataLoggerSlaveChannelInfo channelMappingForSlave1 = new DataLoggerSlaveChannelInfo();
        channelMappingForSlave1.slaveChannel = newChannelInfo(1L, "firstSlave", 1L);
        channelMappingForSlave1.dataLoggerChannel = newChannelInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        slaveInfo1.id = 0;
        slaveInfo1.name = "firstSlave";
        slaveInfo1.deviceTypeName = "firstSlaveDeviceType";
        slaveInfo1.deviceConfigurationId = 2L;
        slaveInfo1.deviceConfigurationName = "firstSlaveDeviceConfiguration";
        slaveInfo1.serialNumber = "100";
        slaveInfo1.yearOfCertification = 1960;
        slaveInfo1.version = 0;
        slaveInfo1.dataLoggerSlaveChannelInfos = Collections.singletonList(channelMappingForSlave1);
        slaveInfo1.linkingTimeStamp = 1234567890L;

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);

        when(topologyService.getSlaveChannel(eq(dataLoggerChannel), any(Instant.class))).thenReturn(Optional.of(slaveChannel1));
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(dataLogger)).thenReturn(topologyTimeLine);
        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        // A slave device need to be created
        verify(deviceService).newDevice(eq(slaveDeviceConfig), eq("firstSlave"), any(Instant.class));
    }

    @Test
    public void testLinkSlaveTwice() {
        when(clock.instant()).thenReturn(NOW);
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");
        Channel dataLoggerChannel = prepareMockedChannel(mock(Channel.class));
        when(dataLoggerChannel.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChannel.getId()).thenReturn(2L);
        when(dataLogger.getChannels()).thenReturn(Collections.singletonList(dataLoggerChannel));

        Device slave1 = mockDeviceForTopologyTest("slave1");
        when(deviceService.newDevice(any(DeviceConfiguration.class), eq("firstSlave"), eq("firstSlave"), any(Instant.class))).thenReturn(slave1);
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.of(slave1));
        Channel slaveChannel1 = prepareMockedChannel(mock(Channel.class));
        when(slaveChannel1.getDevice()).thenReturn(slave1);
        when(slaveChannel1.getId()).thenReturn(1L);
        when(slave1.getChannels()).thenReturn(Collections.singletonList(slaveChannel1));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();
        DeviceConfiguration slaveDeviceConfig = mock(DeviceConfiguration.class);

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getUsagePoint()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findDeviceConfiguration(2L)).thenReturn(Optional.of(slaveDeviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.empty());

        DataLoggerSlaveChannelInfo channelMappingForSlave1 = new DataLoggerSlaveChannelInfo();
        channelMappingForSlave1.dataLoggerChannel = newChannelInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        Instant now = Instant.now();
        slaveInfo1.linkingTimeStamp = now.toEpochMilli();
        slaveInfo1.dataLoggerSlaveChannelInfos = Collections.singletonList(channelMappingForSlave1);

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);   //no linked channels

        doReturn(Arrays.asList(slave1)).when(topologyService).findDataLoggerSlaves(dataLogger);
        when(topologyService.getSlaveChannel(eq(dataLoggerChannel), any(Instant.class))).thenReturn(Optional.of(slaveChannel1));  // datalogger has linked channel
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(dataLogger)).thenReturn(topologyTimeLine);
        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        // Already linked, shouldn't be linked a second time
        verify(topologyService, never()).setDataLogger(any(Device.class), eq(dataLogger), eq(now), any(Map.class), any(Map.class));
    }

    @Test
    public void testUnlinkedWhenDataLoggerChannelUnlinked() {
        when(clock.instant()).thenReturn(NOW);
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");
        Channel dataLoggerChannel = prepareMockedChannel(mock(Channel.class));
        when(dataLoggerChannel.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChannel.getId()).thenReturn(2L);
        when(dataLogger.getChannels()).thenReturn(Collections.singletonList(dataLoggerChannel));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();
        DeviceConfiguration slaveDeviceConfig = mock(DeviceConfiguration.class);

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getUsagePoint()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findDeviceConfiguration(2L)).thenReturn(Optional.of(slaveDeviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.empty());

        DataLoggerSlaveChannelInfo channelMappingForSlave1 = new DataLoggerSlaveChannelInfo();
        channelMappingForSlave1.dataLoggerChannel = newChannelInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        Instant now = Instant.now();
        slaveInfo1.linkingTimeStamp = now.toEpochMilli();
        slaveInfo1.dataLoggerSlaveChannelInfos = Collections.singletonList(channelMappingForSlave1);

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);   //no linked channels

        when(topologyService.getSlaveChannel(eq(dataLoggerChannel), any(Instant.class))).thenReturn(Optional.empty());
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(dataLogger)).thenReturn(topologyTimeLine);
        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(topologyService, never()).setDataLogger(any(Device.class), eq(dataLogger), eq(now), any(Map.class), any(Map.class));
    }

    @Test
    public void testLinkFirstSlaveThroughRegistersToDataLogger() {
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");

        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(222L);

        NumericalRegisterSpec dataLoggerRegisterSpec = mock(NumericalRegisterSpec.class);
        when(dataLoggerRegisterSpec.getId()).thenReturn(2L);
        when(dataLoggerRegisterSpec.getRegisterType()).thenReturn(registerType);
        when(dataLoggerRegisterSpec.getObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(dataLoggerRegisterSpec.getDeviceObisCode()).thenReturn(null);
        when(dataLoggerRegisterSpec.getOverflowValue()).thenReturn(Optional.empty());
        when(dataLoggerRegisterSpec.isUseMultiplier()).thenReturn(false);

        NumericalRegister dataLoggerRegister = prepareMockedRegister(mock(NumericalRegister.class), dataLogger);
        when(dataLoggerRegister.getDevice()).thenReturn(dataLogger);
        when(dataLoggerRegister.getRegisterSpec()).thenReturn(dataLoggerRegisterSpec);
        when(dataLoggerRegister.getRegisterSpecId()).thenReturn(2L);
        when(dataLoggerRegister.getLastReading()).thenReturn(Optional.empty());
        when(dataLogger.getRegisters()).thenReturn(Collections.singletonList(dataLoggerRegister));

        Device slave1 = mockDeviceForTopologyTest("slave1");
        when(slave1.getmRID()).thenReturn("firstSlave");
        NumericalRegisterSpec slave1RegisterSpec = mock(NumericalRegisterSpec.class);
        when(slave1RegisterSpec.getRegisterType()).thenReturn(registerType);
        when(slave1RegisterSpec.getObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(slave1RegisterSpec.getDeviceObisCode()).thenReturn(null);
        when(slave1RegisterSpec.getOverflowValue()).thenReturn(Optional.empty());
        when(slave1RegisterSpec.isUseMultiplier()).thenReturn(false);

        NumericalRegister slaveRegister1 = prepareMockedRegister(mock(NumericalRegisterImpl.class), slave1);
        when(slaveRegister1.getDevice()).thenReturn(slave1);
        when(slaveRegister1.getRegisterSpec()).thenReturn(slave1RegisterSpec);
        when(slaveRegister1.getRegisterSpecId()).thenReturn(1L);
        when(slaveRegister1.getLastReading()).thenReturn(Optional.empty());
        when(slave1.getRegisters()).thenReturn(Collections.singletonList(slaveRegister1));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getUsagePoint()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.of(slave1));

        DataLoggerSlaveRegisterInfo registerMappingForSlave1 = new DataLoggerSlaveRegisterInfo();
        registerMappingForSlave1.slaveRegister = newRegisterInfo(1L, "firstSlave", 1L);
        registerMappingForSlave1.dataLoggerRegister = newRegisterInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        slaveInfo1.id = 100L;
        slaveInfo1.name = "firstSlave";
        slaveInfo1.deviceTypeName = "firstSlaveDeviceType";
        slaveInfo1.deviceConfigurationId = 2L;
        slaveInfo1.deviceConfigurationName = "firstSlaveDeviceConfiguration";
        slaveInfo1.serialNumber = "100";
        slaveInfo1.yearOfCertification = 1960;
        slaveInfo1.version = 1;
        slaveInfo1.dataLoggerSlaveRegisterInfos = Collections.singletonList(registerMappingForSlave1);
        slaveInfo1.linkingTimeStamp = Instant.now().toEpochMilli();

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);

        when(topologyService.getSlaveRegister(eq(dataLoggerRegister), any(Instant.class))).thenReturn(Optional.of(slaveRegister1));
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(dataLogger)).thenReturn(topologyTimeLine);

        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(topologyService).setDataLogger(eq(slave1), eq(dataLogger), eq(Instant.ofEpochMilli(slaveInfo1.linkingTimeStamp)), any(Map.class), any(Map.class));
    }

    @Test
    public void testLinkFirstSlaveThroughChannelsAndRegistersToDataLogger() {
        when(clock.instant()).thenReturn(NOW);
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");

        Channel dataLoggerChannel = prepareMockedChannel(mock(Channel.class));
        when(dataLoggerChannel.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChannel.getId()).thenReturn(2L);
        when(dataLogger.getChannels()).thenReturn(Collections.singletonList(dataLoggerChannel));

        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(2222L);

        NumericalRegisterSpec dataLoggerRegisterSpec = mock(NumericalRegisterSpec.class);
        when(dataLoggerRegisterSpec.getId()).thenReturn(2L);
        when(dataLoggerRegisterSpec.getRegisterType()).thenReturn(registerType);
        when(dataLoggerRegisterSpec.getObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(dataLoggerRegisterSpec.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(dataLoggerRegisterSpec.getOverflowValue()).thenReturn(Optional.empty());
        when(dataLoggerRegisterSpec.isUseMultiplier()).thenReturn(false);

        NumericalRegister dataLoggerRegister = prepareMockedRegister(mock(NumericalRegister.class), dataLogger);
        when(dataLoggerRegister.getDevice()).thenReturn(dataLogger);
        when(dataLoggerRegister.getRegisterSpec()).thenReturn(dataLoggerRegisterSpec);
        when(dataLoggerRegister.getRegisterSpecId()).thenReturn(2L);
        when(dataLoggerRegister.getLastReading()).thenReturn(Optional.empty());
        when(dataLogger.getRegisters()).thenReturn(Collections.singletonList(dataLoggerRegister));

        Device slave1 = mockDeviceForTopologyTest("slave1");
        when(slave1.getmRID()).thenReturn("firstSlave");

        Channel slaveChannel1 = prepareMockedChannel(mock(Channel.class));
        when(slaveChannel1.getDevice()).thenReturn(slave1);
        when(slaveChannel1.getId()).thenReturn(1L);

        when(slave1.getChannels()).thenReturn(Collections.singletonList(slaveChannel1));

        NumericalRegisterSpec slave1RegisterSpec = mock(NumericalRegisterSpec.class);
        when(slave1RegisterSpec.getRegisterType()).thenReturn(registerType);
        when(slave1RegisterSpec.getObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(slave1RegisterSpec.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(slave1RegisterSpec.getOverflowValue()).thenReturn(Optional.empty());
        when(slave1RegisterSpec.isUseMultiplier()).thenReturn(false);

        NumericalRegister slaveRegister1 = prepareMockedRegister(mock(NumericalRegisterImpl.class), slave1);
        when(slaveRegister1.getDevice()).thenReturn(slave1);
        when(slaveRegister1.getRegisterSpec()).thenReturn(slave1RegisterSpec);
        when(slaveRegister1.getRegisterSpecId()).thenReturn(1L);
        when(slaveRegister1.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(slaveRegister1.getLastReading()).thenReturn(Optional.empty());
        when(slave1.getRegisters()).thenReturn(Collections.singletonList(slaveRegister1));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getUsagePoint()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.of(slave1));

        DataLoggerSlaveChannelInfo channelMappingForSlave1 = new DataLoggerSlaveChannelInfo();
        channelMappingForSlave1.slaveChannel = newChannelInfo(1L, "firstSlave", 1L);
        channelMappingForSlave1.dataLoggerChannel = newChannelInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveRegisterInfo registerMappingForSlave1 = new DataLoggerSlaveRegisterInfo();
        registerMappingForSlave1.slaveRegister = newRegisterInfo(1L, "firstSlave", 1L);
        registerMappingForSlave1.dataLoggerRegister = newRegisterInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        slaveInfo1.id = 100L;
        slaveInfo1.name = "firstSlave";
        slaveInfo1.deviceTypeName = "firstSlaveDeviceType";
        slaveInfo1.deviceConfigurationId = 2L;
        slaveInfo1.deviceConfigurationName = "firstSlaveDeviceConfiguration";
        slaveInfo1.serialNumber = "100";
        slaveInfo1.yearOfCertification = 1960;
        slaveInfo1.version = 1;
        slaveInfo1.dataLoggerSlaveChannelInfos = Collections.singletonList(channelMappingForSlave1);
        slaveInfo1.dataLoggerSlaveRegisterInfos = Collections.singletonList(registerMappingForSlave1);
        slaveInfo1.linkingTimeStamp = Instant.now().toEpochMilli();

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);

        when(topologyService.getSlaveChannel(eq(dataLoggerChannel), any(Instant.class))).thenReturn(Optional.of(slaveChannel1));
        when(topologyService.getSlaveRegister(eq(dataLoggerRegister), any(Instant.class))).thenReturn(Optional.of(slaveRegister1));
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(dataLogger)).thenReturn(topologyTimeLine);
        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(topologyService).setDataLogger(eq(slave1), eq(dataLogger), eq(Instant.ofEpochMilli(slaveInfo1.linkingTimeStamp)), any(Map.class), any(Map.class));
    }

    @Test
    public void testLinkSlaveToDataLoggerThroughRegistersThrowsError() throws IOException {
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");

        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(222L);

        NumericalRegisterSpec dataLoggerRegisterSpec = mock(NumericalRegisterSpec.class);
        when(dataLoggerRegisterSpec.getId()).thenReturn(2L);
        when(dataLoggerRegisterSpec.getRegisterType()).thenReturn(registerType);
        when(dataLoggerRegisterSpec.getObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(dataLoggerRegisterSpec.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(dataLoggerRegisterSpec.getDeviceObisCode()).thenReturn(null);
        when(dataLoggerRegisterSpec.getOverflowValue()).thenReturn(Optional.empty());
        when(dataLoggerRegisterSpec.isUseMultiplier()).thenReturn(false);

        NumericalRegister dataLoggerRegister = prepareMockedRegister(mock(NumericalRegister.class), dataLogger);
        when(dataLoggerRegister.getDevice()).thenReturn(dataLogger);
        when(dataLoggerRegister.getRegisterSpec()).thenReturn(dataLoggerRegisterSpec);
        when(dataLoggerRegister.getRegisterSpecId()).thenReturn(2L);
        when(dataLoggerRegister.getLastReading()).thenReturn(Optional.empty());
        when(dataLoggerRegister.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(dataLogger.getRegisters()).thenReturn(Collections.singletonList(dataLoggerRegister));

        Device slave1 = mockDeviceForTopologyTest("slave1");
        when(slave1.getmRID()).thenReturn("firstSlave");
        NumericalRegisterSpec slave1RegisterSpec = mock(NumericalRegisterSpec.class);
        when(slave1RegisterSpec.getRegisterType()).thenReturn(registerType);
        when(slave1RegisterSpec.getObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(slave1RegisterSpec.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(slave1RegisterSpec.getDeviceObisCode()).thenReturn(null);
        when(slave1RegisterSpec.getOverflowValue()).thenReturn(Optional.empty());
        when(slave1RegisterSpec.isUseMultiplier()).thenReturn(false);

        NumericalRegister slaveRegister1 = prepareMockedRegister(mock(NumericalRegisterImpl.class), slave1);
        when(slaveRegister1.getDevice()).thenReturn(slave1);
        when(slaveRegister1.getRegisterSpec()).thenReturn(slave1RegisterSpec);
        when(slaveRegister1.getRegisterSpecId()).thenReturn(1L);
        when(slaveRegister1.getLastReading()).thenReturn(Optional.empty());
        when(slaveRegister1.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(slave1.getRegisters()).thenReturn(Collections.singletonList(slaveRegister1));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getCurrentMeterActivation()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.of(slave1));

        DataLoggerSlaveRegisterInfo registerMappingForSlave1 = new DataLoggerSlaveRegisterInfo();
        registerMappingForSlave1.slaveRegister = newRegisterInfo(1L, "firstSlave", 1L);
        registerMappingForSlave1.dataLoggerRegister = newRegisterInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        slaveInfo1.id = 100L;
        slaveInfo1.name = "firstSlave";
        slaveInfo1.deviceTypeName = "firstSlaveDeviceType";
        slaveInfo1.deviceConfigurationId = 2L;
        slaveInfo1.deviceConfigurationName = "firstSlaveDeviceConfiguration";
        slaveInfo1.serialNumber = "100";
        slaveInfo1.yearOfCertification = 1960;
        slaveInfo1.version = 1;
        slaveInfo1.dataLoggerSlaveRegisterInfos = Collections.singletonList(registerMappingForSlave1);
        slaveInfo1.linkingTimeStamp = Instant.now().toEpochMilli();

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);

        when(topologyService.getSlaveRegister(eq(dataLoggerRegister), any(Instant.class))).thenReturn(Optional.of(slaveRegister1));
        Mockito.doThrow(DataLoggerLinkException.noPhysicalChannelForReadingType(thesaurus, readingType))
                .when(topologyService)
                .setDataLogger(eq(slave1), eq(dataLogger), eq(Instant.ofEpochMilli(slaveInfo1.linkingTimeStamp)), any(Map.class), any(Map.class));

        Response response = target("/devices/1").request().put(Entity.json(info));

        // Simulating a mismatch between mdc-channels and pulse channels: e.g. pulse channel having the mdc-channels' readingtype does not exist
        verify(topologyService).setDataLogger(eq(slave1), eq(dataLogger), eq(Instant.ofEpochMilli(slaveInfo1.linkingTimeStamp)), any(Map.class), any(Map.class));

        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
        assertThat(response.hasEntity()).isTrue();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.error")).isEqualTo("DataLoggerLinkException.noPhysicalSlaveChannelForReadingTypeX");
    }

    @Test
    public void testUnLinkSlave() {
        when(clock.instant()).thenReturn(NOW);
        Device dataLogger = mockDeviceForTopologyTest("dataLogger");
        Channel dataLoggerChannel = prepareMockedChannel(mock(Channel.class));
        when(dataLoggerChannel.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChannel.getId()).thenReturn(2L);
        when(dataLogger.getChannels()).thenReturn(Collections.singletonList(dataLoggerChannel));

        Device slave1 = mockDeviceForTopologyTest("slave1");
        when(slave1.getId()).thenReturn(111L);
        when(deviceService.newDevice(any(DeviceConfiguration.class), eq("firstSlave"), eq("firstSlave"), any(Instant.class))).thenReturn(slave1);
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.of(slave1));
        Channel slaveChannel1 = prepareMockedChannel(mock(Channel.class));
        when(slaveChannel1.getDevice()).thenReturn(slave1);
        when(slaveChannel1.getId()).thenReturn(1L);
        when(slave1.getChannels()).thenReturn(Collections.singletonList(slaveChannel1));

        DeviceConfiguration deviceConfig = dataLogger.getDeviceConfiguration();
        DeviceConfiguration slaveDeviceConfig = mock(DeviceConfiguration.class);

        when(deviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(dataLogger.getUsagePoint()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findDeviceConfiguration(2L)).thenReturn(Optional.of(slaveDeviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(dataLogger.getBatch()).thenReturn(Optional.empty());
        when(deviceService.findDeviceByName("firstSlave")).thenReturn(Optional.empty());

        DataLoggerSlaveChannelInfo channelMappingForSlave1 = new DataLoggerSlaveChannelInfo();
        channelMappingForSlave1.dataLoggerChannel = newChannelInfo(2L, "dataLogger", 13L);

        DataLoggerSlaveDeviceInfo slaveInfo1 = new DataLoggerSlaveDeviceInfo();
        slaveInfo1.id = 111L;
        Instant now = Instant.now();
        slaveInfo1.unlinkingTimeStamp = now.getEpochSecond();
        slaveInfo1.dataLoggerSlaveChannelInfos = Collections.singletonList(channelMappingForSlave1);

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.name = "dataLogger";
        info.parent = new VersionInfo<>(1L, 1L);
        info.dataLoggerSlaveDevices = Collections.singletonList(slaveInfo1);   //no linked channels

        doReturn(Arrays.asList(slave1)).when(topologyService).findDataLoggerSlaves(dataLogger);
        when(topologyService.getSlaveChannel(eq(dataLoggerChannel), any(Instant.class))).thenReturn(Optional.of(slaveChannel1));  // datalogger has linked channel
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(dataLogger)).thenReturn(topologyTimeLine);
        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(topologyService, times(1)).clearDataLogger(slave1, Instant.ofEpochMilli(slaveInfo1.unlinkingTimeStamp));
    }

    private NumericalRegister prepareMockedRegister(NumericalRegister mockedRegister, Device device) {
        when(mockedRegister.getDevice()).thenReturn(device);
        when(mockedRegister.getLastReadingDate()).thenReturn(Optional.empty());
        when(mockedRegister.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());
        when(mockedRegister.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(mockedRegister.getOverflow()).thenReturn(Optional.empty());

        ReadingType readingType = prepareMockedReadingType(mock(ReadingType.class));
        when(mockedRegister.getReadingType()).thenReturn(readingType);
        when(mockedRegister.getCalculatedReadingType(any(Instant.class))).thenReturn(Optional.empty());
        when(readingType.isCumulative()).thenReturn(true);

        return mockedRegister;
    }

    private RegisterInfo newRegisterInfo(long id, String deviceName, long version) {
        RegisterInfo mock = new RegisterInfo();
        mock.id = id;
        mock.deviceName = deviceName;
        mock.parent = new VersionInfo<>(id, version);
        return mock;
    }

    @Test
    public void testActivateEstimationOnDevice() {
        Device device = mockDeviceForTopologyTest("device");
        DeviceConfiguration deviceConfig = device.getDeviceConfiguration();
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(device.getBatch()).thenReturn(Optional.empty());
        when(device.getCurrentMeterActivation()).thenReturn(Optional.empty());

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13l;
        info.estimationStatus = new DeviceEstimationStatusInfo();
        info.estimationStatus.active = true;
        info.name = "device";
        info.parent = new VersionInfo<>(1L, 1L);

        Response response = target("/devices/device/estimationrulesets/esimationstatus").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(device.forEstimation()).activateEstimation();
    }

    @Test
    public void testDeactivateEstimationOnDevice() {
        Device device = mockDeviceForTopologyTest("device");
        DeviceConfiguration deviceConfig = device.getDeviceConfiguration();
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfig));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfig));
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(device.getBatch()).thenReturn(Optional.empty());
        when(device.getCurrentMeterActivation()).thenReturn(Optional.empty());

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.estimationStatus = new DeviceEstimationStatusInfo();
        info.estimationStatus.active = false;
        info.name = "device";
        info.parent = new VersionInfo<>(1L, 1L);

        Response response = target("/devices/device/estimationrulesets/esimationstatus").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(device.forEstimation()).deactivateEstimation();
    }

    private Device mockDeviceForTopologyTest(String name, Device gateway) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(device.getName()).thenReturn(name);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(name + "DeviceType");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name + "DeviceConfig");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSerialNumber()).thenReturn("123456789");
        when(device.getCreateTime()).thenReturn(Instant.EPOCH);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(pluggableClass.getId()).thenReturn(10L);
        DeviceEstimation deviceEstimation = mock(DeviceEstimation.class);
        when(device.forEstimation()).thenReturn(deviceEstimation);
        mockGetOpenDataValidationIssue();
        State state = mockDeviceState("dlc.default.inStock");
        when(device.getState()).thenReturn(state);
        Instant now = Instant.now();
        CIMLifecycleDates dates = mock(CIMLifecycleDates.class);
        Instant shipmentDate = now.minus(5, ChronoUnit.DAYS);
        when(dates.getReceivedDate()).thenReturn(Optional.of(shipmentDate));
        when(dates.getInstalledDate()).thenReturn(Optional.of(now.minus(4, ChronoUnit.DAYS)));
        when(dates.getRemovedDate()).thenReturn(Optional.of(now.minus(3, ChronoUnit.DAYS)));
        when(dates.getRetiredDate()).thenReturn(Optional.of(now.minus(2, ChronoUnit.DAYS)));
        when(device.getLifecycleDates()).thenReturn(dates);
        when(device.getLocation()).thenReturn(Optional.empty());
        when(device.getSpatialCoordinates()).thenReturn(Optional.empty());
        when(dates.setReceivedDate(any(Instant.class))).thenReturn(dates);
        when(device.getBatch()).thenReturn(Optional.empty());
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getStart()).thenReturn(shipmentDate);
        doReturn(Optional.of(meterActivation)).when(device).getCurrentMeterActivation();
        when(deviceService.findDeviceByName(name)).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion(eq(name), anyLong())).thenReturn(Optional.of(device));
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.ofNullable(gateway));
        return device;
    }

    private Device mockDeviceForTopologyTest(String name) {
        return mockDeviceForTopologyTest(name, null);
    }

    private LoadProfileReading mockLoadProfileReading(final LoadProfile loadProfile, Range<Instant> interval, Channel... channels) {
        LoadProfileReading loadProfileReading = mock(LoadProfileReading.class);
        IntervalReadingRecord intervalReadingRecord = mock(IntervalReadingRecord.class);
        when(intervalReadingRecord.getValue()).thenReturn(BigDecimal.TEN);
        ReadingQualityRecord readingQualityCorrupted = mockReadingQuality(ProtocolReadingQualities.CORRUPTED.getCimCode());

        Map<Channel, List<ReadingQualityRecord>> readingQualitiesPerChannel = new HashMap<>();
        for (Channel channel : channels) {
            readingQualitiesPerChannel.put(channel, Arrays.asList(readingQualityCorrupted));
        }

        doReturn(readingQualitiesPerChannel).when(loadProfileReading).getReadingQualities();

        when(loadProfileReading.getReadingTime()).thenReturn(Instant.now());
        when(loadProfileReading.getRange()).thenReturn(interval);
        Map<Channel, IntervalReadingRecord> map = new HashMap<>();
        for (Channel channel : loadProfile.getChannels()) {
            map.put(channel, intervalReadingRecord);
        }
        when(loadProfileReading.getChannelValues()).thenReturn(map);
        return loadProfileReading;
    }

    private ReadingQualityRecord mockReadingQuality(String code) {
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        ReadingQualityType readingQualityType = new ReadingQualityType(code);
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.isActual()).thenReturn(true);
        return readingQuality;
    }

    private Channel mockChannel(String name, String mrid, long id) {
        Channel mock = mock(Channel.class);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getOverflow()).thenReturn(Optional.empty());
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        when(mock.getChannelSpec()).thenReturn(channelSpec);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(mock.getReadingType()).thenReturn(readingType);
        when(mock.getReadingType().getCalculatedReadingType()).thenReturn(Optional.of(readingType));
        when(mock.getInterval()).thenReturn(new TimeDuration("15 minutes"));
        when(mock.getCalculatedReadingType(clock.instant())).thenReturn(Optional.empty());
        Unit unit = Unit.get("kWh");
        when(mock.getLastReading()).thenReturn(Optional.empty());
        when(mock.getLastDateTime()).thenReturn(Optional.empty());
        when(mock.getUnit()).thenReturn(unit);
        return mock;
    }

    private LoadProfile mockLoadProfile(String name, long id, TimeDuration interval, Channel... channels) {
        LoadProfile loadProfile1 = mock(LoadProfile.class);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        when(loadProfileType.getName()).thenReturn(name);
        when(loadProfile1.getInterval()).thenReturn(interval);
        when(loadProfile1.getId()).thenReturn(id);
        when(loadProfile1.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, (int) id));
        when(loadProfile1.getChannels()).thenReturn(channels == null ? Collections.<Channel>emptyList() : Arrays.asList(channels));
        when(loadProfile1.getLastReading()).thenReturn(Optional.of(Instant.ofEpochMilli(1406617200000L))); //  (GMT): Tue, 29 Jul 2014 07:00:00 GMT
        when(loadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        return loadProfile1;
    }

    private LogBook mockLogBook(String name, long id, String obis, String overruledObis, Instant lastLogBook, Instant lastReading) {
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getName()).thenReturn(name);

        LogBook logBook = mock(LogBook.class);
        when(logBook.getId()).thenReturn(id);
        when(logBook.getLogBookType()).thenReturn(logBookType);
        when(logBookType.getObisCode()).thenReturn(ObisCode.fromString(obis));
        when(logBook.getDeviceObisCode()).thenReturn(ObisCode.fromString(overruledObis));
        Optional<Instant> lastLogBookOptional = Optional.ofNullable(lastLogBook);
        when(logBook.getLastLogBook()).thenReturn(lastLogBookOptional);
        Optional<Instant> lastReadingOptional = Optional.ofNullable(lastReading);
        when(logBook.getLatestEventAdditionDate()).thenReturn(lastReadingOptional);
        return logBook;
    }

    private State mockDeviceState(String name) {
        State state = mock(State.class);
        when(state.getName()).thenReturn(name);
        return state;
    }

    private void mockGetOpenDataValidationIssue() {
        Finder finder = mock(Finder.class);
        when(finder.stream()).thenAnswer(invocationOnMock -> Stream.empty());
        when(issueService.findOpenIssuesForDevice(anyString())).thenReturn(finder);
        when(issueService.findIssueType(anyString())).thenReturn(Optional.of(mock(IssueType.class)));
    }

    @Test
    public void testPrivilegesForInStockState() {
        State state = mock(State.class);
        when(state.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device device = mock(Device.class);
        when(device.getState()).thenReturn(state);
        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.of(device));

        DeviceConfiguration configuration = mock(DeviceConfiguration.class);
        when(deviceConfigurationService.getSupportedTimeOfUseOptionsFor(any(), eq(true))).thenReturn(Collections.emptySet());
        when(deviceConfigurationService.findTimeOfUseOptions(anyObject())).thenReturn(Optional.empty());
        when(device.getDeviceConfiguration()).thenReturn(configuration);
        when(device.getDeviceConfiguration().getDeviceType()).thenReturn(null);

        String response = target("/devices/1/privileges").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(17);
        List<String> privileges = model.<List<String>>get("$.privileges[*].name");
        assertThat(privileges).contains(
                DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TOPOLOGY,
                DevicePrivileges.DEVICES_WIDGET_CONNECTION,
                DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TASKS,
                DevicePrivileges.DEVICES_ACTIONS_VALIDATION_RULE_SETS,
                DevicePrivileges.DEVICES_ACTIONS_ESTIMATION_RULE_SETS,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_PLANNING,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TOPOLOGY,
                DevicePrivileges.DEVICES_ACTIONS_DEVICE_COMMANDS,
                DevicePrivileges.DEVICES_ACTIONS_SECURITY_SETTINGS,
                DevicePrivileges.DEVICES_ACTIONS_PROTOCOL_DIALECTS,
                DevicePrivileges.DEVICES_ACTIONS_GENERAL_ATTRIBUTES,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TASKS,
                DevicePrivileges.DEVICES_ACTIONS_CONNECTION_METHODS,
                DevicePrivileges.DEVICES_ACTIONS_DATA_EDIT,
                DevicePrivileges.DEVICES_ACTIONS_CHANGE_DEVICE_CONFIGURATION,
                DevicePrivileges.DEVICES_ACTIONS_FIRMWARE_MANAGEMENT,
                DevicePrivileges.DEVICES_PAGES_COMMUNICATION_PLANNING
        );
    }

    @Test
    public void testPrivilegesForInDecommissionedState() {
        State state = mock(State.class);
        when(state.getName()).thenReturn(DefaultState.DECOMMISSIONED.getKey());
        Device device = mock(Device.class);
        when(device.getState()).thenReturn(state);
        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.of(device));

        DeviceConfiguration configuration = mock(DeviceConfiguration.class);
        when(deviceConfigurationService.getSupportedTimeOfUseOptionsFor(any(), eq(true))).thenReturn(Collections.emptySet());
        when(deviceConfigurationService.findTimeOfUseOptions(anyObject())).thenReturn(Optional.empty());
        when(device.getDeviceConfiguration()).thenReturn(configuration);
        when(device.getDeviceConfiguration().getDeviceType()).thenReturn(null);

        String response = target("/devices/1/privileges").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        List<String> privileges = model.<List<String>>get("$.privileges[*].name");
        assertThat(privileges).isEmpty();
    }

    @Test
    public void testPrivilegesForCustomState() {
        State state = mock(State.class);
        when(state.getName()).thenReturn("Custom state");
        Device device = mock(Device.class);
        when(device.getState()).thenReturn(state);
        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.of(device));
        DeviceConfiguration configuration = mock(DeviceConfiguration.class);
        when(deviceConfigurationService.getSupportedTimeOfUseOptionsFor(any(), eq(true))).thenReturn(Collections.emptySet());
        when(deviceConfigurationService.findTimeOfUseOptions(anyObject())).thenReturn(Optional.empty());
        when(device.getDeviceConfiguration()).thenReturn(configuration);
        when(device.getDeviceConfiguration().getDeviceType()).thenReturn(null);

        String response = target("/devices/1/privileges").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(21);
        List<String> privileges = model.<List<String>>get("$.privileges[*].name");
        assertThat(privileges).contains(
                DevicePrivileges.DEVICES_WIDGET_ISSUES,
                DevicePrivileges.DEVICES_WIDGET_VALIDATION,
                DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TOPOLOGY,
                DevicePrivileges.DEVICES_WIDGET_CONNECTION,
                DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TASKS,
                DevicePrivileges.DEVICES_ACTIONS_VALIDATION,
                DevicePrivileges.DEVICES_ACTIONS_ESTIMATION,
                DevicePrivileges.DEVICES_ACTIONS_VALIDATION_RULE_SETS,
                DevicePrivileges.DEVICES_ACTIONS_ESTIMATION_RULE_SETS,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_PLANNING,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TOPOLOGY,
                DevicePrivileges.DEVICES_ACTIONS_DEVICE_COMMANDS,
                DevicePrivileges.DEVICES_ACTIONS_SECURITY_SETTINGS,
                DevicePrivileges.DEVICES_ACTIONS_PROTOCOL_DIALECTS,
                DevicePrivileges.DEVICES_ACTIONS_GENERAL_ATTRIBUTES,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TASKS,
                DevicePrivileges.DEVICES_ACTIONS_CONNECTION_METHODS,
                DevicePrivileges.DEVICES_ACTIONS_DATA_EDIT,
                DevicePrivileges.DEVICES_ACTIONS_CHANGE_DEVICE_CONFIGURATION,
                DevicePrivileges.DEVICES_ACTIONS_FIRMWARE_MANAGEMENT,
                DevicePrivileges.DEVICES_PAGES_COMMUNICATION_PLANNING
        );
    }

    @Test
    public void testFindAllSerialNumbers() throws Exception {
        Device device = mockDeviceForTopologyTest("COP_TestDevice");
        when(device.getUsagePoint()).thenReturn(Optional.empty());
        Finder<Device> finder = mockFinder(Collections.singletonList(device));
        when(deviceService.findAllDevices(any())).thenReturn(finder);
        String response = target("/devices").queryParam("name", "COP*").queryParam("serialNumber", "*").request().get(String.class);
        ArgumentCaptor<Condition> conditionArgumentCaptor = ArgumentCaptor.forClass(Condition.class);
        verify(deviceService).findAllDevices(conditionArgumentCaptor.capture());
        verifyNoMoreInteractions(deviceService);
        Condition andCondition = conditionArgumentCaptor.getValue();
        assertThat(andCondition).isInstanceOf(And.class);
        List<Condition> conditions = ((And)andCondition).getConditions();
        assertThat(conditions).hasSize(2);
        Condition nameCondition = conditions.get(0);
        assertThat(nameCondition).isInstanceOf(Comparison.class);
        assertThat(((Comparison)nameCondition).getFieldName()).isEqualTo("name");
        assertThat(((Comparison)nameCondition).getOperator()).isEqualTo(Operator.LIKEIGNORECASE);
        assertThat(((Comparison)nameCondition).getValues()).containsExactly("COP%");
        Condition serialCondition = conditions.get(1);
        assertThat(serialCondition).isInstanceOf(Comparison.class);
        assertThat(((Comparison)serialCondition).getFieldName()).isEqualTo("serialNumber");
        assertThat(((Comparison)serialCondition).getOperator()).isEqualTo(Operator.LIKEIGNORECASE);
        assertThat(((Comparison)serialCondition).getValues()).containsExactly("%");
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.devices[0].name")).isEqualTo("COP_TestDevice");
    }

    private CustomPropertySet mockCustomPropertySet() {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(1448191220000L));
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.of(device));
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceConfigurationService.findDeviceType(anyLong())).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(anyLong(), anyLong())).thenReturn(Optional.of(deviceType));
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getName()).thenReturn("testCps");
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType().getId()).thenReturn(1L);
        when(device.getDeviceType().getVersion()).thenReturn(1L);
        when(deviceType.getCustomPropertySets()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getVersion()).thenReturn(1L);
        when(registeredCustomPropertySet.isViewableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.isEditableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.getId()).thenReturn(1L);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        MdcPropertyUtils mdcPropertyUtils = mock(MdcPropertyUtils.class);
        PropertyInfo propertyInfo = mock(PropertyInfo.class);
        PropertyValueInfo propertyValueInfo = mock(PropertyValueInfo.class);
        when(propertyValueInfo.getValue()).thenReturn("testValue");
        when(propertyInfo.getPropertyValueInfo()).thenReturn(propertyValueInfo);
        when(mdcPropertyUtils.convertPropertySpecsToPropertyInfos(anyObject(), anyObject())).thenReturn(Arrays.asList(propertyInfo));
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.emptyDuring(Interval.of(Range.closedOpen(Instant.ofEpochMilli(startTimeFirst), Instant.ofEpochMilli(endTimeFirst))));
        customPropertySetValues.setProperty("testname", "testValue");
        CustomPropertySetValues customPropertySetValues2 = CustomPropertySetValues.emptyDuring(Interval.of(Range.closedOpen(Instant.ofEpochMilli(endTimeFirst), Instant.ofEpochMilli(endTimeSecond))));
        customPropertySetValues2.setProperty("testname2", "testValue2");
        when(customPropertySetService.getUniqueValuesFor(customPropertySet, device)).thenReturn(customPropertySetValues);
        when(customPropertySetService.getUniqueValuesFor(eq(customPropertySet), eq(device), any(Instant.class))).thenReturn(customPropertySetValues);
        when(customPropertySetService.getAllVersionedValuesFor(customPropertySet, device)).thenReturn(Arrays.asList(customPropertySetValues, customPropertySetValues2));
        ValuesRangeConflict conflict1 = mock(ValuesRangeConflict.class);
        when(conflict1.getConflictingRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(startTimeFirst), Instant.ofEpochMilli(endTimeFirst)));
        when(conflict1.getMessage()).thenReturn("testMessage");
        when(conflict1.getType()).thenReturn(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
        when(conflict1.getValues()).thenReturn(customPropertySetValues);
        ValuesRangeConflict conflict2 = mock(ValuesRangeConflict.class);
        when(conflict2.getConflictingRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(startTimeNew), Instant.ofEpochMilli(endTimeNew)));
        when(conflict2.getMessage()).thenReturn("testMessage");
        when(conflict2.getType()).thenReturn(ValuesRangeConflictType.RANGE_INSERTED);
        when(conflict2.getValues()).thenReturn(CustomPropertySetValues.emptyDuring(Interval.of(Range.closedOpen(Instant.ofEpochMilli(startTimeNew), Instant.ofEpochMilli(endTimeNew)))));
        ValuesRangeConflict conflict3 = mock(ValuesRangeConflict.class);
        when(conflict3.getConflictingRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(endTimeFirst), Instant.ofEpochMilli(endTimeSecond)));
        when(conflict3.getMessage()).thenReturn("testMessage");
        when(conflict3.getType()).thenReturn(ValuesRangeConflictType.RANGE_OVERLAP_DELETE);
        when(conflict3.getValues()).thenReturn(customPropertySetValues2);
        OverlapCalculatorBuilder overlapCalculatorBuilder = mock(OverlapCalculatorBuilder.class);
        when(overlapCalculatorBuilder.whenCreating(any(Range.class))).thenReturn(Arrays.asList(conflict1, conflict2, conflict3));
        when(overlapCalculatorBuilder.whenUpdating(any(Instant.class), any(Range.class))).thenReturn(Arrays.asList(conflict1, conflict2, conflict3));
        when(customPropertySetService.calculateOverlapsFor(anyObject(), any(Device.class))).thenReturn(overlapCalculatorBuilder);
        return customPropertySet;
    }

    @Test
    public void testGetDeviceCustomProperties() throws Exception {
        CustomPropertySet customPropertySet = mockCustomPropertySet();
        when(customPropertySet.isVersioned()).thenReturn(false);

        String response = target("devices/1/customproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.customproperties")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.customproperties[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.customproperties[0].name")).isEqualTo("testCps");
        assertThat(jsonModel.<Boolean>get("$.customproperties[0].timesliced")).isEqualTo(false);
    }

    @Test
    public void testGetDeviceCustomPropertiesVersioned() throws Exception {
        mockCustomPropertySet();
        String response = target("devices/1/customproperties/1/versions").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<?>>get("$.versions")).hasSize(2);
        assertThat(jsonModel.<Integer>get("$.versions[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.versions[0].name")).isEqualTo("testCps");
        assertThat(jsonModel.<Boolean>get("$.versions[0].timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.versions[0].versionId")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[0].startTime")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[0].endTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Integer>get("$.versions[1].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.versions[1].name")).isEqualTo("testCps");
        assertThat(jsonModel.<Boolean>get("$.versions[1].timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.versions[0].versionId")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[1].startTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[1].endTime")).isEqualTo(endTimeSecond);
    }

    @Test
    public void testGetCurrentTimeInterval() throws Exception {
        mockCustomPropertySet();
        String response = target("devices/1/customproperties/1/currentinterval").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Long>get("$.start")).isGreaterThan(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.end")).isEqualTo(endTimeFirst);
    }

    @Test
    public void testGetConflictsCreate() throws Exception {
        mockCustomPropertySet();
        String response = target("devices/1/customproperties/1/conflicts").queryParam("startTime", startTimeNew).queryParam("endTime", endTimeNew).request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<?>>get("$.conflicts")).hasSize(3);
        assertThat(jsonModel.<Integer>get("$.conflicts[0].customPropertySet.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.conflicts[0].customPropertySet.name")).isEqualTo("testCps");
        assertThat(jsonModel.<String>get("$.conflicts[0].conflictType")).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END.name());
        assertThat(jsonModel.<Boolean>get("$.conflicts[0].customPropertySet.timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.conflicts[0].conflictAtStart")).isEqualTo(false);
        assertThat(jsonModel.<Boolean>get("$.conflicts[0].conflictAtEnd")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.conflicts[0].customPropertySet.versionId")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[0].customPropertySet.startTime")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[0].customPropertySet.endTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[1].customPropertySet.startTime")).isEqualTo(startTimeNew);
        assertThat(jsonModel.<Long>get("$.conflicts[1].customPropertySet.endTime")).isEqualTo(endTimeNew);
        assertThat(jsonModel.<Integer>get("$.conflicts[2].customPropertySet.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.conflicts[2].customPropertySet.name")).isEqualTo("testCps");
        assertThat(jsonModel.<String>get("$.conflicts[2].conflictType")).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_DELETE.name());
        assertThat(jsonModel.<Boolean>get("$.conflicts[2].customPropertySet.timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.conflicts[2].customPropertySet.versionId")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[2].customPropertySet.startTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[2].customPropertySet.endTime")).isEqualTo(endTimeSecond);
        assertThat(jsonModel.<Boolean>get("$.conflicts[2].conflictAtStart")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.conflicts[2].conflictAtEnd")).isEqualTo(true);
    }

    @Test
    public void testEditDeviceCustomAttribute() throws Exception {
        CustomPropertySet customPropertySet = mockCustomPropertySet();
        when(customPropertySet.isVersioned()).thenReturn(false);
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        info.id = 1L;
        info.isActive = true;
        info.parent = 1L;
        info.version = 5L;
        info.objectTypeId = 1L;
        info.objectTypeVersion = 1L;
        info.timesliced = false;
        info.properties = new ArrayList<>();
        Response response = target("devices/1/customproperties/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testEditDeviceCustomAttributeVersioned() throws Exception {
        mockCustomPropertySet();
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        info.id = 1L;
        info.isActive = true;
        info.startTime = endTimeFirst;
        info.endTime = startTimeFirst;
        info.parent = 1L;
        info.version = 5L;
        info.objectTypeId = 1L;
        info.objectTypeVersion = 1L;
        info.timesliced = true;
        info.versionId = info.startTime;
        info.properties = new ArrayList<>();
        Response response = target("devices/1/customproperties/1/versions/1416403197000").queryParam("forced", true).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(400);
        info.startTime = startTimeNew;
        info.endTime = endTimeFirst;
        info.versionId = info.startTime;
        info.objectTypeId = 1L;
        info.objectTypeVersion = 1L;
        response = target("devices/1/customproperties/1/versions/1416403197000").queryParam("forced", true).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testGetDeviceLocation() throws Exception {
        when(locationService.findLocationById(anyLong())).thenReturn(Optional.empty());
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

        Response response = target("devices/locations/1").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }


    @Test
    public void createWithShipmentDateTest() {
        Instant shipmentDate = Instant.ofEpochMilli(1467019262000L);
        long deviceConfigId = 12L;
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfigurationService.findDeviceConfiguration(deviceConfigId)).thenReturn(Optional.of(deviceConfiguration));
        Device device = mock(Device.class, RETURNS_DEEP_STUBS);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getBatch()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getStart()).thenReturn(shipmentDate);
        doReturn(Optional.of(meterActivation)).when(device).getCurrentMeterActivation();
        when(device.getUsagePoint()).thenReturn(Optional.empty());
        CIMLifecycleDates cimLifecycleDates = mock(CIMLifecycleDates.class);
        when(cimLifecycleDates.setReceivedDate(any(Instant.class))).thenReturn(cimLifecycleDates);
        when(device.getLifecycleDates()).thenReturn(cimLifecycleDates);
        when(device.getLocation()).thenReturn(Optional.empty());
        when(device.getSpatialCoordinates()).thenReturn(Optional.empty());
        when(device.getCreateTime()).thenReturn(Instant.EPOCH);
        String deviceName = "name";
        when(deviceService.newDevice(deviceConfiguration, deviceName, shipmentDate)).thenReturn(device);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.name = deviceName;
        deviceInfo.deviceConfigurationId = deviceConfigId;
        deviceInfo.serialNumber = "MySerialNumber";
        deviceInfo.yearOfCertification = 1970;
        deviceInfo.shipmentDate = shipmentDate;
        when(cimLifecycleDates.getReceivedDate()).thenReturn(Optional.of(shipmentDate));
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(device)).thenReturn(topologyTimeLine);
        Response response = target("/devices/").request().post(Entity.json(deviceInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(cimLifecycleDates, times(1)).setReceivedDate(shipmentDate);
    }

    @Test
    public void getWithEstimationRulesTest() {
        Instant shipmentDate = Instant.ofEpochMilli(1467019262000L);
        long deviceConfigId = 12L;
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        EstimationRuleSet mockedEstimationRuleSet = mock(EstimationRuleSet.class);
        when(deviceConfiguration.getEstimationRuleSets()).thenReturn(Arrays.asList(mockedEstimationRuleSet));
        when(deviceConfiguration.getValidationRuleSets()).thenReturn(Collections.emptyList());
        when(deviceConfigurationService.findDeviceConfiguration(deviceConfigId)).thenReturn(Optional.of(deviceConfiguration));
        Device device = mock(Device.class, RETURNS_DEEP_STUBS);
        when(deviceService.findDeviceByName("theDevice")).thenReturn(Optional.of(device));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getBatch()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(device.getUsagePoint()).thenReturn(Optional.empty());
        CIMLifecycleDates cimLifecycleDates = mock(CIMLifecycleDates.class);
        when(cimLifecycleDates.setReceivedDate(any(Instant.class))).thenReturn(cimLifecycleDates);
        when(device.getLifecycleDates()).thenReturn(cimLifecycleDates);
        when(device.getLocation()).thenReturn(Optional.empty());
        when(device.getSpatialCoordinates()).thenReturn(Optional.empty());
        String name = "Great name";
        when(deviceService.newDevice(deviceConfiguration, name, "batch", shipmentDate)).thenReturn(device);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.name = name;
        deviceInfo.deviceConfigurationId = deviceConfigId;
        deviceInfo.serialNumber = "MySerialNumber";
        deviceInfo.yearOfCertification = 1970;
        deviceInfo.shipmentDate = shipmentDate;
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(device)).thenReturn(topologyTimeLine);
        when(cimLifecycleDates.getReceivedDate()).thenReturn(Optional.of(shipmentDate));
        Map<String, Object> response = target("/devices/theDevice/").request().get(Map.class);
        assertThat(response).contains(MapEntry.entry("hasEstimationRules", true));
        assertThat(response).contains(MapEntry.entry("hasValidationRules", false));
    }

    @Test
    public void getWithValidationRulesTest() {
        Instant shipmentDate = Instant.ofEpochMilli(1467019262000L);
        long deviceConfigId = 12L;
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        ValidationRuleSet mockedValidationRuleSet = mock(ValidationRuleSet.class);
        when(deviceConfiguration.getEstimationRuleSets()).thenReturn(Collections.emptyList());
        when(deviceConfiguration.getValidationRuleSets()).thenReturn(Collections.singletonList(mockedValidationRuleSet));
        when(deviceConfigurationService.findDeviceConfiguration(deviceConfigId)).thenReturn(Optional.of(deviceConfiguration));
        Device device = mock(Device.class, RETURNS_DEEP_STUBS);
        when(deviceService.findDeviceByName("theDevice")).thenReturn(Optional.of(device));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getBatch()).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(device.getUsagePoint()).thenReturn(Optional.empty());
        CIMLifecycleDates cimLifecycleDates = mock(CIMLifecycleDates.class);
        when(cimLifecycleDates.setReceivedDate(any(Instant.class))).thenReturn(cimLifecycleDates);
        when(device.getLifecycleDates()).thenReturn(cimLifecycleDates);
        when(device.getLocation()).thenReturn(Optional.empty());
        when(device.getSpatialCoordinates()).thenReturn(Optional.empty());
        String name = "Great name";
        when(deviceService.newDevice(deviceConfiguration, name, "batch", shipmentDate)).thenReturn(device);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.name = name;
        deviceInfo.deviceConfigurationId = deviceConfigId;
        deviceInfo.serialNumber = "MySerialNumber";
        deviceInfo.yearOfCertification = 1970;
        deviceInfo.shipmentDate = shipmentDate;
        when(cimLifecycleDates.getReceivedDate()).thenReturn(Optional.of(shipmentDate));
        TopologyTimeline topologyTimeLine = mock(TopologyTimeline.class);
        when(topologyTimeLine.getAllDevices()).thenReturn(Collections.emptySet());
        when(topologyService.getPysicalTopologyTimeline(device)).thenReturn(topologyTimeLine);
        Map<String, Object> response = target("/devices/theDevice/").request().get(Map.class);
        assertThat(response).contains(MapEntry.entry("hasEstimationRules", false));
        assertThat(response).contains(MapEntry.entry("hasValidationRules", true));
    }
}
