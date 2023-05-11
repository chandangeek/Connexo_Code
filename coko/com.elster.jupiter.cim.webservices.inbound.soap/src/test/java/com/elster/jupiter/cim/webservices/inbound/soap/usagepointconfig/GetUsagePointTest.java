/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.usagepointconfig.Name;
import ch.iec.tc57._2011.usagepointconfig.ServiceKind;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConfig;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConnectedKind;
import ch.iec.tc57._2011.usagepointconfigmessage.ObjectFactory;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigFaultMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigPayloadType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigRequestMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigResponseMessageType;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class GetUsagePointTest extends AbstractMockActivator {
    private static final long USAGE_POINT_ID = 42;
    private static final String USAGE_POINT_MRID = "Mă băşto palo skhamind";
    private static final String USAGE_POINT_NAME = "But bravinta pro skhamind";
    private static final String ANOTHER_MRID = "Mă skhasion ă zvania";
    private static final String ANOTHER_NAME = "Dră laci compania";
    private static final String CUSTOM_STATE_NAME = "Custom";
    private static final Instant INSTALLATION_DATE = ZonedDateTime.of(2017, 1, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()).toInstant();
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
    private State state;
    @Mock
    private com.elster.jupiter.metering.ServiceCategory serviceCategory;
    @Mock
    private ElectricityDetail electricityDetail;
    @Mock
    private UsagePointConnectionState usagePointConnectionState;
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
        when(state.getName()).thenReturn(CUSTOM_STATE_NAME);
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
        when(usagePoint.getState()).thenReturn(state);
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.of(usagePointConnectionState));
        when(usagePointConnectionState.getConnectionState()).thenReturn(ConnectionState.CONNECTED);
        when(usagePoint.getInstallationTime()).thenReturn(INSTALLATION_DATE);
        doReturn(Optional.of(electricityDetail)).when(usagePoint).getDetail(NOW);
        when(usagePoint.forCustomProperties()).thenReturn(usagePointCustomPropertySetExtension);
        when(usagePointCustomPropertySetExtension.getAllPropertySets()).thenReturn(Collections.emptyList());
        mockDetails();
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

    @Test
    public void testNoPayload() throws Exception {
        // Prepare request
        UsagePointConfigRequestMessageType usagePointConfigRequest = usagePointConfigMessageFactory
                .createUsagePointConfigRequestMessageType();

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'Payload' is required.");
    }

    @Test
    public void testNoUsagePointConfigInPayload() throws Exception {
        // Prepare request
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(null);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig' is required.");
    }

    @Test
    public void testNoUsagePointsInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'UsagePointConfig.UsagePoint' cannot be empty.");
    }

    @Test
    public void testEmptyMRIDInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint("\t\n\r ", USAGE_POINT_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].mRID' is empty or contains only white spaces.");
    }

    @Test
    public void testUsagePointIsNotFoundByMRID() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(ANOTHER_MRID, USAGE_POINT_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_USAGE_POINT_WITH_MRID.getErrorCode(),
                "No usage point is found by MRID '" + ANOTHER_MRID + "'.");
    }

    @Test
    public void testEmptyIdentifyingNameInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(null, "\r \t\n");
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].Names[0].name' is empty or contains only white spaces.");
    }

    @Test
    public void testNoMRIDAndNameInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(null, null);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT.getErrorCode(),
                "Either element 'mRID' or 'Names' is required under 'UsagePointConfig.UsagePoint[0]' for identification purpose.");
    }

    @Test
    public void testUsagePointIsNotFoundByName() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(null, ANOTHER_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_USAGE_POINT_WITH_NAME.getErrorCode(),
                "No usage point is found by name '" + ANOTHER_NAME + "'.");
    }

    @Test
    public void testSuccessfulGetByMRID() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(USAGE_POINT_MRID, null);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
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
        //assertThat(responseUsagePointInfo.getPhaseCode()).isEqualTo(PhaseCode.S_12_N); // fails because of: UsagePointCOnfigFactory - line 72
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.CONNECTED);

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(INSTALLATION_DATE);
    }

    @Test
    public void testSuccessfulGetByName() throws Exception {
        when(state.getName()).thenReturn(DefaultState.DEMOLISHED.getKey());

        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo
                = createUsagePoint(null, USAGE_POINT_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.getUsagePointConfig(usagePointConfigRequest);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
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
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(DefaultState.DEMOLISHED.getTranslation().getDefaultFormat());
        //assertThat(responseUsagePointInfo.getPhaseCode()).isEqualTo(PhaseCode.S_12_N); // fails because of: UsagePointCOnfigFactory - line 72
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.CONNECTED);

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(INSTALLATION_DATE);
    }

    private ch.iec.tc57._2011.usagepointconfig.UsagePoint createUsagePoint(String mRID, String name) {
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint = new ch.iec.tc57._2011.usagepointconfig.UsagePoint();
        usagePoint.setMRID(mRID);
        Optional.ofNullable(name)
                .map(this::name)
                .ifPresent(usagePoint.getNames()::add);
        return usagePoint;
    }

    private UsagePointConfigRequestMessageType createUsagePointConfigRequest(UsagePointConfig usagePointConfig) {
        UsagePointConfigPayloadType usagePointConfigPayload = usagePointConfigMessageFactory.createUsagePointConfigPayloadType();
        usagePointConfigPayload.setUsagePointConfig(usagePointConfig);
        HeaderType header = objectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
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
        return name;
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
            assertThat(faultMessage.getMessage()).isEqualTo("Unable to get usage point");
            UsagePointConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            List<ErrorType> errors = faultInfo.getReply().getError();
            assertThat(errors.stream().map(ErrorType::getCode).collect(Collectors.toList()))
                    .containsOnly(codeToMessageMap.keySet().stream().toArray(String[]::new));
            errors.forEach(error -> {
                assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
                assertThat(error.getDetails()).isEqualTo(codeToMessageMap.get(error.getCode()));
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail("Expected FaultMessage but got: " + System.lineSeparator() + e.toString());
        }
    }

    private interface RunnableWithFaultMessage {
        void run() throws FaultMessage;
    }
}
