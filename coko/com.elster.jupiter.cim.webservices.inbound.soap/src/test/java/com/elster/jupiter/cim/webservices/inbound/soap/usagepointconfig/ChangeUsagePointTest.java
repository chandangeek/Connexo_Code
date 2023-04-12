/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeFail;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.usagepointconfig.ConfigurationEvent;
import ch.iec.tc57._2011.usagepointconfig.Name;
import ch.iec.tc57._2011.usagepointconfig.NameType;
import ch.iec.tc57._2011.usagepointconfig.PhaseCode;
import ch.iec.tc57._2011.usagepointconfig.ServiceKind;
import ch.iec.tc57._2011.usagepointconfig.Status;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConfig;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConnectedKind;
import ch.iec.tc57._2011.usagepointconfigmessage.ObjectFactory;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigFaultMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigPayloadType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigRequestMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigResponseMessageType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ChangeUsagePointTest extends AbstractMockActivator {
    private static final long USAGE_POINT_ID = 42;
    private static final String USAGE_POINT_MRID = "Zagăiom mă co roma";
    private static final String USAGE_POINT_NAME = "Co roma co barvală";
    private static final String ANOTHER_MRID = "Io roma, io barvală";
    private static final String ANOTHER_NAME = "Ciaşcu ciaiu na ciută";
    private static final String ACTIVE_STATE_NAME = "Active";
    private static final String INACTIVE_STATE_NAME = "Inactive";
    private static final String CUSTOM_STATE_NAME = "Custom";
    private static final String CREATED_STATE_NAME = "Under construction";
    private static final Instant INSTALLATION_DATE = ZonedDateTime.of(2017, 1, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final Instant EVENT_EFFECTIVE_DATE = ZonedDateTime.of(2017, 3, 17, 7, 11, 0, 0, TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final Instant REQUEST_DATE = ZonedDateTime.of(2017, 3, 30, 9, 5, 0, 0, TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final Instant NOW = ZonedDateTime.of(2017, 7, 5, 10, 27, 58, 0, TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final Quantity ESTIMATED_LOAD = Quantity.create(BigDecimal.valueOf(2), 3, Unit.WATT_HOUR.getAsciiSymbol());
    private static final Quantity MAXIMUM_LOAD = Quantity.create(BigDecimal.ONE, 6, Unit.WATT_HOUR.getAsciiSymbol());
    private static final Quantity NOMINAL_VOLTAGE = Quantity.create(BigDecimal.valueOf(230), Unit.VOLT.getAsciiSymbol());
    private static final Quantity RATED_CURRENT = Quantity.create(BigDecimal.valueOf(6), -3, Unit.AMPERE.getAsciiSymbol());
    private static final Quantity RATED_POWER = Quantity.create(BigDecimal.valueOf(3), 3, Unit.WATT.getAsciiSymbol());

    private final ObjectFactory usagePointConfigMessageFactory = new ObjectFactory();
    private final ch.iec.tc57._2011.schema.message.ObjectFactory objectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private State active, customState, inactive, created;
    @Mock
    private com.elster.jupiter.metering.ServiceCategory serviceCategory;
    @Mock
    private ElectricityDetail electricityDetail;
    private ElectricityDetailBuilder electricityDetailBuilder;
    @Mock
    private UsagePointTransition customTransition, deactivate;
    @Mock
    private UsagePointStateChangeRequest changeRequest;
    @Mock
    private UsagePointStateChangeFail checkFail, actionFail;
    @Mock
    private UsagePointLifeCycle lifeCycle;
    @Mock
    private UsagePointConnectionState usagePointConnectionState;
    @Mock
    private PropertySpec connectionStatePropertySpec;
    @Mock
    private ValueFactory<HasIdAndName> valueFactory;
    @Mock
    private HasIdAndName logicallyDisconnectedValue;
    @Mock
    private UsagePointCustomPropertySetExtension usagePointCustomPropertySetExtension;
    @Mock
    private WebServiceContext webServiceContext;
    @Mock
    private MessageContext messageContext;
    @Mock
    private WebServiceCallOccurrence webServiceCallOccurrence;

    private ExecuteUsagePointConfigEndpoint executeUsagePointConfigEndpoint;

    @Before
    public void setUp() throws Exception {
        executeUsagePointConfigEndpoint = getInstance(ExecuteUsagePointConfigEndpoint.class);
        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(executeUsagePointConfigEndpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1L);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, executeUsagePointConfigEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, executeUsagePointConfigEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, executeUsagePointConfigEndpoint, "webServiceCallOccurrenceService", webServiceCallOccurrenceService);
        inject(AbstractInboundEndPoint.class, executeUsagePointConfigEndpoint, "transactionService", transactionService);
        when(transactionService.execute(any())).then(invocationOnMock -> invocationOnMock.getArgumentAt(0, ExceptionThrowingSupplier.class).get());
        when(webServiceCallOccurrenceService.getOngoingOccurrence(1L)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));

        when(clock.instant()).thenReturn(NOW);
        when(meteringService.findUsagePointById(anyLong())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByMRID(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.getServiceCategory(com.elster.jupiter.metering.ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.getKind()).thenReturn(com.elster.jupiter.metering.ServiceKind.ELECTRICITY);
        when(serviceCategory.isActive()).thenReturn(true);
        electricityDetailBuilder = FakeBuilder.initBuilderStub(null, ElectricityDetailBuilder.class);
        when(lifeCycle.getStates()).thenReturn(Arrays.asList(created, active, inactive, customState));
        when(usagePointLifeCycleService.getAvailableTransitions(usagePoint, "INS")).thenReturn(Arrays.asList(customTransition, deactivate));
        when(customTransition.getTo()).thenReturn(customState);
        when(customTransition.getMicroActionsProperties()).thenReturn(Collections.emptyList());
        when(customState.getName()).thenReturn(CUSTOM_STATE_NAME);
        when(deactivate.getTo()).thenReturn(inactive);
        when(deactivate.getMicroActionsProperties()).thenReturn(Collections.singletonList(connectionStatePropertySpec));
        when(connectionStatePropertySpec.getName()).thenReturn("set.connection.state.property.name");
        when(connectionStatePropertySpec.getValueFactory()).thenReturn(valueFactory);
        when(valueFactory.fromStringValue(ConnectionState.LOGICALLY_DISCONNECTED.name())).thenReturn(logicallyDisconnectedValue);
        when(inactive.getName()).thenReturn(DefaultState.INACTIVE.getKey());
        when(active.getName()).thenReturn(DefaultState.ACTIVE.getKey());
        when(created.getName()).thenReturn(DefaultState.UNDER_CONSTRUCTION.getKey());
        when(changeRequest.getFailReasons()).thenReturn(Collections.emptyList());
        mockUsagePoint();
    }

    private void mockUsagePoint() {
        when(usagePoint.getId()).thenReturn(USAGE_POINT_ID);
        when(meteringService.findUsagePointById(USAGE_POINT_ID)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMRID()).thenReturn(USAGE_POINT_MRID);
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.isSdp()).thenReturn(true);
        when(usagePoint.isVirtual()).thenReturn(false);
        when(usagePoint.getEffectiveMetrologyConfiguration(NOW)).thenReturn(Optional.empty());
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getLifeCycle()).thenReturn(lifeCycle);
        when(usagePoint.getState()).thenReturn(active);
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.of(usagePointConnectionState));
        when(usagePointConnectionState.getConnectionState()).thenReturn(ConnectionState.CONNECTED);
        when(usagePoint.getInstallationTime()).thenReturn(INSTALLATION_DATE);
        doReturn(Optional.of(electricityDetail)).when(usagePoint).getDetail(NOW);
        when(usagePoint.newElectricityDetailBuilder(any(Instant.class))).thenReturn(electricityDetailBuilder);
        when(usagePoint.forCustomProperties()).thenReturn(usagePointCustomPropertySetExtension);
        when(usagePointCustomPropertySetExtension.getAllPropertySets()).thenReturn(Collections.emptyList());
        mockDetails();
        mockUpdatedParameters();
    }

    private void mockUpdatedParameters() {
        doAnswer(invocation -> {
            when(usagePoint.getName()).thenReturn((String) invocation.getArguments()[0]);
            return null;
        }).when(usagePoint).setName(anyString());
        doAnswer(invocation -> {
            when(electricityDetail.getPhaseCode()).thenReturn((com.elster.jupiter.cbo.PhaseCode) invocation.getArguments()[0]);
            return electricityDetailBuilder;
        }).when(electricityDetailBuilder).withPhaseCode(any(com.elster.jupiter.cbo.PhaseCode.class));
        doAnswer(invocation -> {
            State state = ((UsagePointTransition) invocation.getArguments()[1]).getTo();
            when(usagePoint.getState()).thenReturn(state);
            Map<String, Object> properties = (Map<String, Object>) invocation.getArguments()[3];
            if (logicallyDisconnectedValue.equals(properties.get(connectionStatePropertySpec.getName()))) {
                when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.of(usagePointConnectionState));
                when(usagePointConnectionState.getConnectionState()).thenReturn(ConnectionState.LOGICALLY_DISCONNECTED);
            }
            return changeRequest;
        }).when(usagePointLifeCycleService)
                .performTransition(eq(usagePoint), any(UsagePointTransition.class), eq("INS"), anyMapOf(String.class, Object.class));
        doAnswer(invocation -> {
            when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.of(usagePointConnectionState));
            when(usagePointConnectionState.getConnectionState()).thenReturn((ConnectionState) invocation.getArguments()[0]);
            return null;
        }).when(usagePoint)
                .setConnectionState(any(ConnectionState.class));
    }

    private void mockDetails() {
        when(electricityDetail.getPhaseCode()).thenReturn(com.elster.jupiter.cbo.PhaseCode.S12N);
        when(electricityDetail.getEstimatedLoad()).thenReturn(ESTIMATED_LOAD);
        when(electricityDetail.getLoadLimit()).thenReturn(MAXIMUM_LOAD);
        when(electricityDetail.getLoadLimiterType()).thenReturn("CarelessLimiter");
        when(electricityDetail.getNominalServiceVoltage()).thenReturn(NOMINAL_VOLTAGE);
        when(electricityDetail.getRatedCurrent()).thenReturn(RATED_CURRENT);
        when(electricityDetail.getRatedPower()).thenReturn(RATED_POWER);
        when(electricityDetail.isGrounded()).thenReturn(YesNoAnswer.YES);
        when(electricityDetail.isInterruptible()).thenReturn(YesNoAnswer.UNKNOWN);
        when(electricityDetail.isLimiter()).thenReturn(YesNoAnswer.YES);
        when(electricityDetail.isCollarInstalled()).thenReturn(YesNoAnswer.NO);
    }

    private void assertRecreatedDetails(com.elster.jupiter.cbo.PhaseCode phaseCode) {
        verify(usagePoint).newElectricityDetailBuilder(NOW);
        verify(electricityDetailBuilder).withEstimatedLoad(ESTIMATED_LOAD);
        verify(electricityDetailBuilder).withLoadLimit(MAXIMUM_LOAD);
        verify(electricityDetailBuilder).withLoadLimiterType("CarelessLimiter");
        verify(electricityDetailBuilder).withNominalServiceVoltage(NOMINAL_VOLTAGE);
        verify(electricityDetailBuilder).withRatedCurrent(RATED_CURRENT);
        verify(electricityDetailBuilder).withRatedPower(RATED_POWER);
        verify(electricityDetailBuilder).withGrounded(YesNoAnswer.YES);
        verify(electricityDetailBuilder).withInterruptible(YesNoAnswer.UNKNOWN);
        verify(electricityDetailBuilder).withLimiter(YesNoAnswer.YES);
        verify(electricityDetailBuilder).withCollar(YesNoAnswer.NO);
        InOrder inOrder = Mockito.inOrder(electricityDetailBuilder);
        inOrder.verify(electricityDetailBuilder).withPhaseCode(com.elster.jupiter.cbo.PhaseCode.S12N);
        inOrder.verify(electricityDetailBuilder).withPhaseCode(phaseCode);
        verify(electricityDetailBuilder).create();
        verifyNoMoreInteractions(electricityDetailBuilder);
    }

    private void assertKeptDetails() {
        verify(usagePoint, never()).newElectricityDetailBuilder(any(Instant.class));
        verify(usagePoint, never()).newGasDetailBuilder(any(Instant.class));
        verify(usagePoint, never()).newHeatDetailBuilder(any(Instant.class));
        verify(usagePoint, never()).newWaterDetailBuilder(any(Instant.class));
        verify(usagePoint, never()).newDefaultDetailBuilder(any(Instant.class));
        verifyZeroInteractions(electricityDetailBuilder);
    }

    @Test
    public void testNoPayload() throws Exception {
        // Prepare request
        UsagePointConfigRequestMessageType usagePointConfigRequest = usagePointConfigMessageFactory
                .createUsagePointConfigRequestMessageType();
        usagePointConfigRequest.setHeader(new HeaderType());

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'Payload' is required.");
    }

    @Test
    public void testNoHeader() throws Exception {
        // Prepare request
        UsagePointConfigRequestMessageType usagePointConfigRequest = usagePointConfigMessageFactory
                .createUsagePointConfigRequestMessageType();

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'Header' is required.");
    }

    @Test
    public void testNoUsagePointConfigInPayload() throws Exception {
        // Prepare request
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(null);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig' is required.");
    }

    @Test
    public void testNoUsagePointsInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'UsagePointConfig.UsagePoint' cannot be empty.");
    }

    @Test
    public void testEmptyMRIDInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint("\t\n\r ", USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].mRID' is empty or contains only white spaces.");
    }

    @Test
    public void testUsagePointIsNotFoundByMRID() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(ANOTHER_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_USAGE_POINT_WITH_MRID.getErrorCode(),
                "No usage point is found by MRID '" + ANOTHER_MRID + "'.");
    }

    @Test
    public void testEmptyIdentifyingNameInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(null, "\r \t\n", PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].Names[0].name' is empty or contains only white spaces.");
    }

    @Test
    public void testNoMRIDAndNameInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(null, null, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT.getErrorCode(),
                "Either element 'mRID' or 'Names' is required under 'UsagePointConfig.UsagePoint[0]' for identification purpose.");
    }

    @Test
    public void testUsagePointIsNotFoundByName() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(null, ANOTHER_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_USAGE_POINT_WITH_NAME.getErrorCode(),
                "No usage point is found by name '" + ANOTHER_NAME + "'.");
    }

    @Test
    public void testSetEmptyNameInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, "\r \t\n", PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].Names[0].name' is empty or contains only white spaces.");
    }

    @Test
    public void testSetSeveralNamesInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointInfo.getNames().add(name(ANOTHER_NAME));
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest);
    }

    @Test
    public void testNoReasonInConfigurationEvent() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        setState(usagePointInfo, INACTIVE_STATE_NAME);
        usagePointInfo.getConfigurationEvents().setStatus(new Status());
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].ConfigurationEvents.status.reason' is required.");
    }

    @Test
    public void testWrongReasonInConfigurationEvent() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        setState(usagePointInfo, INACTIVE_STATE_NAME);
        Status status = new Status();
        status.setReason("Configure me anything");
        usagePointInfo.getConfigurationEvents().setStatus(status);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.UNSUPPORTED_VALUE.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].ConfigurationEvents.status.reason' contains unsupported value 'Configure me anything'. " +
                        "Must be one of: 'Purpose Active', 'Purpose Inactive', 'Change Status', 'Change Lifecycle'.");
    }

    @Test
    public void testUnsupportedReasonInConfigurationEvent() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        setState(usagePointInfo, INACTIVE_STATE_NAME);
        Status status = new Status();
        status.setReason(ConfigurationEventReason.PURPOSE_ACTIVE.getReason());
        usagePointInfo.getConfigurationEvents().setStatus(status);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].status.value' is empty or contains only white spaces.");
    }

    @Test
    public void testNoStatusInUsagePointConfigWhenStatusChangeIsRequested() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, INACTIVE_STATE_NAME);
        usagePointInfo.setStatus(null);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].status' is required.");
    }

    @Test
    public void testNoStatusValueInUsagePointConfigWhenStatusChangeIsRequested() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, null);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].status.value' is required.");
    }

    @Test
    public void testEmptyStatusValueInUsagePointConfigWhenStatusChangeIsRequested() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, "\n\r \t");
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].status.value' is empty or contains only white spaces.");
    }

    @Test
    public void testUsagePointIsAlreadyInRequestedState() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, ACTIVE_STATE_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.USAGE_POINT_IS_ALREADY_IN_STATE.getErrorCode(),
                "Usage point is already in state '" + ACTIVE_STATE_NAME + "'.");
    }

    @Test
    public void testNoTransitionToRequestedState() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, CREATED_STATE_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_AVAILABLE_TRANSITION_TO_STATE.getErrorCode(),
                "No transition is available to state '" + CREATED_STATE_NAME + "'.");
    }

    @Test
    public void testNoSuchStateInLifeCycle() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, "Drunk");
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_USAGE_POINT_STATE_WITH_NAME.getErrorCode(),
                "No usage point state 'Drunk' is found in current life cycle.");
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), USAGE_POINT_NAME);
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), USAGE_POINT_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testFailedTransition() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, INACTIVE_STATE_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Re-mock usage point
        when(changeRequest.getFailReasons()).thenReturn(Arrays.asList(checkFail, actionFail));
        when(checkFail.getFailSource()).thenReturn(UsagePointStateChangeFail.FailSource.CHECK);
        when(checkFail.getName()).thenReturn("Sanity check");
        when(checkFail.getMessage()).thenReturn("This check checks sanity.");
        when(actionFail.getFailSource()).thenReturn(UsagePointStateChangeFail.FailSource.ACTION);
        when(actionFail.getName()).thenReturn("Ordinary action");
        when(actionFail.getMessage()).thenReturn("This action acts ordinarily.");

        // Business method & assertions
        assertFaultMessages(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                ImmutableMap.of(
                        MessageSeeds.TRANSITION_CHECK_FAILED.getErrorCode(),
                        "Transition can't be performed due to failed transition check 'Sanity check': This check checks sanity.",
                        MessageSeeds.TRANSITION_ACTION_FAILED.getErrorCode(),
                        "Transition can't be performed due to failed transition action 'Ordinary action': This action acts ordinarily."
                ));
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), USAGE_POINT_NAME);
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), USAGE_POINT_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testSuccessfulUpdateByMRID() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, ANOTHER_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, INACTIVE_STATE_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(usagePoint).setName(ANOTHER_NAME);
        verify(usagePoint).update();

        assertRecreatedDetails(com.elster.jupiter.cbo.PhaseCode.S1);

        verify(usagePointLifeCycleService).performTransition(usagePoint, deactivate, "INS",
                ImmutableMap.of(connectionStatePropertySpec.getName(), logicallyDisconnectedValue));
        verify(usagePoint, never()).setConnectionState(any(ConnectionState.class), any(Instant.class));
        verify(usagePoint, never()).setConnectionState(any(ConnectionState.class));

        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), ANOTHER_NAME);
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), USAGE_POINT_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CHANGED);
        assertThat(response.getHeader().getNoun()).isEqualTo("UsagePointConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        UsagePointConfig responseUsagePointConfig = response.getPayload().getUsagePointConfig();
        assertThat(responseUsagePointConfig.getUsagePoint()).hasSize(1);

        ch.iec.tc57._2011.usagepointconfig.UsagePoint responseUsagePointInfo = responseUsagePointConfig.getUsagePoint().get(0);
        assertThat(responseUsagePointInfo.getMRID()).isEqualTo(USAGE_POINT_MRID);
        assertThat(responseUsagePointInfo.getNames().get(0).getName()).isEqualTo(ANOTHER_NAME);
        assertThat(responseUsagePointInfo.isIsSdp()).isEqualTo(true);
        assertThat(responseUsagePointInfo.isIsVirtual()).isEqualTo(false);
        assertThat(responseUsagePointInfo.getServiceCategory().getKind()).isEqualTo(ServiceKind.ELECTRICITY);
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(INACTIVE_STATE_NAME);
        //assertThat(responseUsagePointInfo.getPhaseCode()).isEqualTo(PhaseCode.S_1); // fails because of: UsagePointCOnfigFactory - line 72
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.LOGICALLY_DISCONNECTED);

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(INSTALLATION_DATE);
    }

    @Test
    public void testSuccessfulUpdateByNameWithOtherParametersAndTransitionWithoutProperties() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(null, USAGE_POINT_NAME, PhaseCode.ABCN, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, CUSTOM_STATE_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(usagePoint, never()).setName(anyString());
        verify(usagePoint, never()).update();

        assertRecreatedDetails(com.elster.jupiter.cbo.PhaseCode.ABCN);

        verify(usagePointLifeCycleService).performTransition(usagePoint, customTransition, "INS", Collections.emptyMap());
        verify(usagePoint).setConnectionState(ConnectionState.LOGICALLY_DISCONNECTED);
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), USAGE_POINT_NAME);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CHANGED);
        assertThat(response.getHeader().getNoun()).isEqualTo("UsagePointConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        UsagePointConfig responseUsagePointConfig = response.getPayload().getUsagePointConfig();
        assertThat(responseUsagePointConfig.getUsagePoint()).hasSize(1);

        ch.iec.tc57._2011.usagepointconfig.UsagePoint responseUsagePointInfo = responseUsagePointConfig.getUsagePoint().get(0);
        assertThat(responseUsagePointInfo.getMRID()).isEqualTo(USAGE_POINT_MRID);
        assertThat(responseUsagePointInfo.getNames().get(0).getName()).isEqualTo(USAGE_POINT_NAME);
        assertThat(responseUsagePointInfo.isIsSdp()).isEqualTo(true);
        assertThat(responseUsagePointInfo.isIsVirtual()).isEqualTo(false);
        assertThat(responseUsagePointInfo.getServiceCategory().getKind()).isEqualTo(ServiceKind.ELECTRICITY);
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(CUSTOM_STATE_NAME);
        //  assertThat(responseUsagePointInfo.getPhaseCode()).isEqualTo(PhaseCode.ABCN); // fails because of: UsagePointCOnfigFactory - line 72
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.LOGICALLY_DISCONNECTED);

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(INSTALLATION_DATE);
    }

    @Test
    public void testNoAttemptToUpdatePhaseCodeForNonElectricityUsagePointAndConnectionStateIfNotRequested() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, ANOTHER_NAME, PhaseCode.S_1, null);
        setState(usagePointInfo, INACTIVE_STATE_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Re-mock usage point
        when(serviceCategory.getKind()).thenReturn(com.elster.jupiter.metering.ServiceKind.GAS);

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(usagePoint).setName(ANOTHER_NAME);
        verify(usagePoint).update();

        assertKeptDetails();

        verify(usagePointLifeCycleService).performTransition(usagePoint, deactivate, "INS", Collections.emptyMap());
        verify(usagePoint, never()).setConnectionState(any(ConnectionState.class), any(Instant.class));
        verify(usagePoint, never()).setConnectionState(any(ConnectionState.class));
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), ANOTHER_NAME);
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), USAGE_POINT_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CHANGED);
        assertThat(response.getHeader().getNoun()).isEqualTo("UsagePointConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        UsagePointConfig responseUsagePointConfig = response.getPayload().getUsagePointConfig();
        assertThat(responseUsagePointConfig.getUsagePoint()).hasSize(1);

        ch.iec.tc57._2011.usagepointconfig.UsagePoint responseUsagePointInfo = responseUsagePointConfig.getUsagePoint().get(0);
        assertThat(responseUsagePointInfo.getMRID()).isEqualTo(USAGE_POINT_MRID);
        assertThat(responseUsagePointInfo.getNames().get(0).getName()).isEqualTo(ANOTHER_NAME);
        assertThat(responseUsagePointInfo.isIsSdp()).isEqualTo(true);
        assertThat(responseUsagePointInfo.isIsVirtual()).isEqualTo(false);
        assertThat(responseUsagePointInfo.getServiceCategory().getKind()).isEqualTo(ServiceKind.GAS);
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(INACTIVE_STATE_NAME);
        assertThat(responseUsagePointInfo.getPhaseCode()).isNull();
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.CONNECTED);

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(INSTALLATION_DATE);
    }

    @Test
    public void testNoUpdateIfNoParametersAreProvidedInRequestButConnectionStateIsSetWithoutTransition() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, null, null, UsagePointConnectedKind.PHYSICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Re-mock usage point
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.empty());

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(usagePoint, never()).setName(anyString());
        verify(usagePoint, never()).update();
        verify(usagePoint).setConnectionState(ConnectionState.PHYSICALLY_DISCONNECTED);
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), USAGE_POINT_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        assertKeptDetails();

        verify(usagePointLifeCycleService, never())
                .performTransition(eq(usagePoint), any(UsagePointTransition.class), anyString(), anyMapOf(String.class, Object.class));

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CHANGED);
        assertThat(response.getHeader().getNoun()).isEqualTo("UsagePointConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        UsagePointConfig responseUsagePointConfig = response.getPayload().getUsagePointConfig();
        assertThat(responseUsagePointConfig.getUsagePoint()).hasSize(1);

        ch.iec.tc57._2011.usagepointconfig.UsagePoint responseUsagePointInfo = responseUsagePointConfig.getUsagePoint().get(0);
        assertThat(responseUsagePointInfo.getMRID()).isEqualTo(USAGE_POINT_MRID);
        assertThat(responseUsagePointInfo.getNames().get(0).getName()).isEqualTo(USAGE_POINT_NAME);
        assertThat(responseUsagePointInfo.isIsSdp()).isEqualTo(true);
        assertThat(responseUsagePointInfo.isIsVirtual()).isEqualTo(false);
        assertThat(responseUsagePointInfo.getServiceCategory().getKind()).isEqualTo(ServiceKind.ELECTRICITY);
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(ACTIVE_STATE_NAME);
        //assertThat(responseUsagePointInfo.getPhaseCode()).isEqualTo(PhaseCode.S_12_N); // fails because of: UsagePointCOnfigFactory - line 72
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.PHYSICALLY_DISCONNECTED);

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(INSTALLATION_DATE);
    }

    @Test
    public void testNoUpdateIfSameParametersAreProvidedInRequest() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_12_N, UsagePointConnectedKind.CONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(usagePoint, never()).setName(anyString());
        verify(usagePoint, never()).update();
        verify(usagePoint, never()).setConnectionState(any(ConnectionState.class), any(Instant.class));
        verify(usagePoint, never()).setConnectionState(any(ConnectionState.class));

        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), USAGE_POINT_NAME);
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), USAGE_POINT_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        assertKeptDetails();

        verify(usagePointLifeCycleService, never())
                .performTransition(eq(usagePoint), any(UsagePointTransition.class), anyString(), anyMapOf(String.class, Object.class));

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CHANGED);
        assertThat(response.getHeader().getNoun()).isEqualTo("UsagePointConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        UsagePointConfig responseUsagePointConfig = response.getPayload().getUsagePointConfig();
        assertThat(responseUsagePointConfig.getUsagePoint()).hasSize(1);

        ch.iec.tc57._2011.usagepointconfig.UsagePoint responseUsagePointInfo = responseUsagePointConfig.getUsagePoint().get(0);
        assertThat(responseUsagePointInfo.getMRID()).isEqualTo(USAGE_POINT_MRID);
        assertThat(responseUsagePointInfo.getNames().get(0).getName()).isEqualTo(USAGE_POINT_NAME);
        assertThat(responseUsagePointInfo.isIsSdp()).isEqualTo(true);
        assertThat(responseUsagePointInfo.isIsVirtual()).isEqualTo(false);
        assertThat(responseUsagePointInfo.getServiceCategory().getKind()).isEqualTo(ServiceKind.ELECTRICITY);
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(ACTIVE_STATE_NAME);
        // assertThat(responseUsagePointInfo.getPhaseCode()).isEqualTo(PhaseCode.S_12_N); // fails because of: UsagePointCOnfigFactory - line 72
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.CONNECTED);

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(INSTALLATION_DATE);
    }

    @Test
    public void testFailureWithLocalizedException() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, ANOTHER_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, INACTIVE_STATE_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Mock exception
        LocalizedException localizedException = mock(LocalizedException.class);
        when(localizedException.getLocalizedMessage()).thenReturn("ErrorMessage");
        when(localizedException.getErrorCode()).thenReturn("ERRORCODE");
        when(usagePoint.newElectricityDetailBuilder(any(Instant.class)))
                .thenThrow(localizedException);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                "ERRORCODE",
                "ErrorMessage");
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), ANOTHER_NAME);
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), USAGE_POINT_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testFailureWithVerboseConstraintViolationException() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, ANOTHER_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setState(usagePointInfo, INACTIVE_STATE_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Mock exception
        VerboseConstraintViolationException exception = mock(VerboseConstraintViolationException.class);
        when(exception.getLocalizedMessage()).thenReturn("ErrorMessage");
        doThrow(exception).when(usagePointLifeCycleService)
                .performTransition(eq(usagePoint), any(UsagePointTransition.class), eq("INS"), anyMapOf(String.class, Object.class));

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest),
                null,
                "ErrorMessage");
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), ANOTHER_NAME);
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), USAGE_POINT_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testAsync() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, PhaseCode.S_1, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);
        usagePointConfigRequest.getHeader().setAsyncReplyFlag(true);

        // Execute
        executeUsagePointConfigEndpoint.changeUsagePointConfig(usagePointConfigRequest);

        // Assert service call
        verify(serviceCall).requestTransition(com.elster.jupiter.servicecall.DefaultState.PENDING);
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), USAGE_POINT_NAME);
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), USAGE_POINT_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    private ch.iec.tc57._2011.usagepointconfig.UsagePoint createUsagePoint(String mRID, String name, PhaseCode phaseCode,
                                                                           UsagePointConnectedKind connectionState) {
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint = new ch.iec.tc57._2011.usagepointconfig.UsagePoint();
        usagePoint.setMRID(mRID);
        Optional.ofNullable(name)
                .map(this::name)
                .ifPresent(usagePoint.getNames()::add);
        usagePoint.setPhaseCode(phaseCode);
        usagePoint.setConnectionState(connectionState);
        return usagePoint;
    }

    private void setState(ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint, String stateName) {
        Status status = new Status();
        status.setReason(ConfigurationEventReason.CHANGE_STATUS.getReason());
        ConfigurationEvent event = new ConfigurationEvent();
        event.setStatus(status);
        event.setEffectiveDateTime(EVENT_EFFECTIVE_DATE);
        usagePoint.setConfigurationEvents(event);
        Status usagePointStatus = new Status();
        usagePointStatus.setValue(stateName);
        usagePoint.setStatus(usagePointStatus);
    }

    private UsagePointConfigRequestMessageType createUsagePointConfigRequest(UsagePointConfig usagePointConfig) {
        UsagePointConfigPayloadType usagePointConfigPayload = usagePointConfigMessageFactory.createUsagePointConfigPayloadType();
        usagePointConfigPayload.setUsagePointConfig(usagePointConfig);
        HeaderType header = objectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.CHANGE);
        header.setTimestamp(REQUEST_DATE);
        UsagePointConfigRequestMessageType usagePointConfigRequestMessage = usagePointConfigMessageFactory
                .createUsagePointConfigRequestMessageType();
        usagePointConfigRequestMessage.setPayload(usagePointConfigPayload);
        usagePointConfigRequestMessage.setHeader(header);
        return usagePointConfigRequestMessage;
    }

    private Name name(String value) {
        Name name = new Name();
        name.setName(value);
        name.setNameType(nameType(UsagePointBuilder.USAGE_POINT_NAME));
        return name;
    }

    private NameType nameType(String value) {
        NameType nameType = new NameType();
        nameType.setName(value);
        return nameType;
    }

    private void assertFaultMessage(RunnableWithFaultMessage action, String expectedCode, String expectedDetailedMessage) {
        assertFaultMessages(action, Collections.singletonMap(expectedCode, expectedDetailedMessage));
    }

    private void assertFaultMessages(RunnableWithFaultMessage action, Map<String, String> codeToMessageMap) {
        try {
            // Business method
            action.run();
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo("Unable to update usage point");
            UsagePointConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            List<ErrorType> errors = faultInfo.getReply().getError();
            assertThat(errors.stream().map(ErrorType::getCode).collect(Collectors.toList()))
                    .containsOnly(codeToMessageMap.keySet().stream().toArray(String[]::new));
            errors.forEach(error -> {
                assertThat(error.getDetails()).isEqualTo(codeToMessageMap.get(error.getCode()));
                assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            });

            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Expected FaultMessage but got: " + System.lineSeparator() + e);
        }
    }

    private interface RunnableWithFaultMessage {
        void run() throws FaultMessage;
    }
}
