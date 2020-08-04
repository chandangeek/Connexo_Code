/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.usagepointconfig.ConfigurationEvent;
import ch.iec.tc57._2011.usagepointconfig.Name;
import ch.iec.tc57._2011.usagepointconfig.PhaseCode;
import ch.iec.tc57._2011.usagepointconfig.ServiceCategory;
import ch.iec.tc57._2011.usagepointconfig.ServiceKind;
import ch.iec.tc57._2011.usagepointconfig.UsagePoint.MetrologyRequirements;
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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CreateUsagePointTest extends AbstractMockActivator {

    private static final String USAGE_POINT_MRID = UUID.randomUUID().toString();
    private static final String USAGE_POINT_NAME = "Zef_usage_point";
    private static final String METROLOGY_CONFIGURATION_NAME = "Residential net metering (consumption)";
    private static final String BILLING_NAME = "Billing";
    private static final String CHECK_NAME = "Check";
    private static final String STATE_NAME = "Created";
    private static final Instant CREATION_DATE = ZonedDateTime.of(2017, 3, 17, 7, 11, 0, 0, TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final Instant REQUEST_DATE = ZonedDateTime.of(2017, 3, 30, 9, 5, 0, 0, TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final Instant NOW = ZonedDateTime.of(2017, 7, 5, 10, 27, 58, 0, TimeZoneNeutral.getMcMurdo()).toInstant();

    private final ObjectFactory usagePointConfigMessageFactory = new ObjectFactory();
    private final ch.iec.tc57._2011.schema.message.ObjectFactory objectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private State state;
    @Mock
    private com.elster.jupiter.metering.ServiceCategory serviceCategory;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC;
    @Mock
    private MetrologyContract billing, information, check;
    @Mock
    private MetrologyContractChannelsContainer billingContainer;
    @Mock
    private MetrologyPurpose billingPurpose, checkPurpose;
    @Mock
    private ReadingTypeDeliverable deliverable1, deliverable2;
    @Mock
    private ReadingType readingType1, readingType2;
    private UsagePointBuilder usagePointBuilder;
    private UsagePointDetailBuilder usagePointDetailBuilder;
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

    private static void assertReadingType1(ch.iec.tc57._2011.usagepointconfig.ReadingType rt) {
        assertThat(rt.getMRID()).isEqualTo("MRID1");
        assertThat(rt.getAccumulation()).isEqualTo("Delta data");
        assertThat(rt.getAggregate()).isEqualTo("Sum");
        assertThat(rt.getArgument()).isNull();
        assertThat(rt.getCommodity()).isEqualTo("Electricity secondary metered");
        assertThat(rt.getConsumptionTier().longValue()).isEqualTo(1L);
        assertThat(rt.getCpp().longValue()).isEqualTo(1L);
        assertThat(rt.getCurrency()).isEqualTo("ZAR");
        assertThat(rt.getFlowDirection()).isEqualTo("Forward");
        assertThat(rt.getInterharmonic().getNumerator().longValue()).isEqualTo(1L);
        assertThat(rt.getInterharmonic().getDenominator().longValue()).isEqualTo(3L);
        assertThat(rt.getMacroPeriod()).isEqualTo("Daily");
        assertThat(rt.getMeasurementKind()).isEqualTo("Energy");
        assertThat(rt.getMeasuringPeriod()).isEqualTo("Not applicable");
        assertThat(rt.getMultiplier()).isEqualTo("*10^3");
        assertThat(rt.getPhases()).isEqualTo("Phase-NotApplicable");
        assertThat(rt.getTou().longValue()).isEqualTo(1L);
        assertThat(rt.getUnit()).isEqualTo("Watt hours");
        assertThat(rt.getNames().get(0).getName()).isEqualTo("FullAliasName1");
    }

    private static void assertReadingType2(ch.iec.tc57._2011.usagepointconfig.ReadingType rt) {
        assertThat(rt.getMRID()).isEqualTo("MRID2");
        assertThat(rt.getAccumulation()).isEqualTo("Bulk quantity");
        assertThat(rt.getAggregate()).isEqualTo("Average");
        assertThat(rt.getArgument().getNumerator().longValue()).isEqualTo(2L);
        assertThat(rt.getArgument().getDenominator().longValue()).isEqualTo(3L);
        assertThat(rt.getCommodity()).isEqualTo("Electricity primary metered");
        assertThat(rt.getConsumptionTier().longValue()).isEqualTo(2L);
        assertThat(rt.getCpp().longValue()).isEqualTo(2L);
        assertThat(rt.getCurrency()).isEqualTo("RUB");
        assertThat(rt.getFlowDirection()).isEqualTo("Reverse");
        assertThat(rt.getInterharmonic()).isNull();
        assertThat(rt.getMacroPeriod()).isEqualTo("Not applicable");
        assertThat(rt.getMeasurementKind()).isEqualTo("Power");
        assertThat(rt.getMeasuringPeriod()).isEqualTo(TimeAttribute.MINUTE15.getDescription());
        assertThat(rt.getMultiplier()).isEqualTo("*10^0");
        assertThat(rt.getPhases()).isEqualTo("Phase-A");
        assertThat(rt.getTou().longValue()).isEqualTo(2L);
        assertThat(rt.getUnit()).isEqualTo("Watt hour per mile");
        assertThat(rt.getNames().get(0).getName()).isEqualTo("FullAliasName2");
    }

    @Before
    public void setUp() throws Exception {
        executeUsagePointConfigEndpoint = getInstance(ExecuteUsagePointConfigEndpoint.class);
        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(executeUsagePointConfigEndpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1l);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, executeUsagePointConfigEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, executeUsagePointConfigEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, executeUsagePointConfigEndpoint, "transactionService", transactionService);
        when(transactionService.execute(any())).then(new Answer(){
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((ExceptionThrowingSupplier)invocationOnMock.getArguments()[0]).get();
            }
        });
        when(webServicesService.getOngoingOccurrence(1l)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));

        mockUsagePoint();
        mockReadingType1();
        mockReadingType2();
        when(clock.instant()).thenReturn(NOW);
        when(meteringService.getServiceCategory(com.elster.jupiter.metering.ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.getKind()).thenReturn(com.elster.jupiter.metering.ServiceKind.ELECTRICITY);
        when(serviceCategory.isActive()).thenReturn(true);
        when(metrologyConfigurationService.findMetrologyConfiguration(METROLOGY_CONFIGURATION_NAME)).thenReturn(Optional.of(metrologyConfiguration));
        when(metrologyConfiguration.getName()).thenReturn(METROLOGY_CONFIGURATION_NAME);
        when(metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(billing, information, check));
        when(metrologyConfiguration.isActive()).thenReturn(true);
        when(effectiveMC.getChannelsContainer(billing, NOW)).thenReturn(Optional.of(billingContainer));
        when(effectiveMC.getChannelsContainer(information, NOW)).thenReturn(Optional.empty());
        when(effectiveMC.getChannelsContainer(check, NOW)).thenReturn(Optional.of(billingContainer));
        when(billing.getMetrologyPurpose()).thenReturn(billingPurpose);
        when(billingPurpose.getName()).thenReturn(BILLING_NAME);
        when(billing.getDeliverables()).thenReturn(Collections.singletonList(deliverable1));
        when(check.getMetrologyPurpose()).thenReturn(checkPurpose);
        when(checkPurpose.getName()).thenReturn(CHECK_NAME);
        when(check.getDeliverables()).thenReturn(Arrays.asList(deliverable1, deliverable2));
        when(deliverable1.getReadingType()).thenReturn(readingType1);
        when(deliverable2.getReadingType()).thenReturn(readingType2);
        usagePointBuilder = FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class);
        when(serviceCategory.newUsagePoint(any(String.class), any(Instant.class))).thenReturn(usagePointBuilder);
        usagePointDetailBuilder = FakeBuilder.initBuilderStub(null, UsagePointDetailBuilder.class,
                ElectricityDetailBuilder.class, GasDetailBuilder.class, WaterDetailBuilder.class, HeatDetailBuilder.class);
        when(usagePoint.newElectricityDetailBuilder(any(Instant.class))).thenReturn((ElectricityDetailBuilder) usagePointDetailBuilder);
    }

    private void mockReadingType1() {
        when(readingType1.getMRID()).thenReturn("MRID1");
        when(readingType1.getAccumulation()).thenReturn(Accumulation.DELTADELTA);
        when(readingType1.getAggregate()).thenReturn(Aggregate.SUM);
        when(readingType1.getArgument()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType1.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(readingType1.getConsumptionTier()).thenReturn(1);
        when(readingType1.getCpp()).thenReturn(1);
        when(readingType1.getCurrency()).thenReturn(Currency.getInstance("ZAR"));
        when(readingType1.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType1.getInterharmonic()).thenReturn(new RationalNumber(1, 3));
        when(readingType1.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType1.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(readingType1.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType1.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType1.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType1.getTou()).thenReturn(1);
        when(readingType1.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType1.getFullAliasName()).thenReturn("FullAliasName1");
    }

    private void mockReadingType2() {
        when(readingType2.getMRID()).thenReturn("MRID2");
        when(readingType2.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType2.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType2.getArgument()).thenReturn(new RationalNumber(2, 3));
        when(readingType2.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(readingType2.getConsumptionTier()).thenReturn(2);
        when(readingType2.getCpp()).thenReturn(2);
        when(readingType2.getCurrency()).thenReturn(Currency.getInstance("RUB"));
        when(readingType2.getFlowDirection()).thenReturn(FlowDirection.REVERSE);
        when(readingType2.getInterharmonic()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType2.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType2.getMeasurementKind()).thenReturn(MeasurementKind.POWER);
        when(readingType2.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType2.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType2.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType2.getTou()).thenReturn(2);
        when(readingType2.getUnit()).thenReturn(ReadingTypeUnit.WATTHOURPERMILE);
        when(readingType2.getFullAliasName()).thenReturn("FullAliasName2");
    }

    private void mockUsagePoint() {
        when(usagePoint.getMRID()).thenReturn(USAGE_POINT_MRID);
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
        when(usagePoint.isSdp()).thenReturn(true);
        when(usagePoint.isVirtual()).thenReturn(false);
        doReturn(Optional.of(electricityDetail)).when(usagePoint).getDetail(NOW);
        when(electricityDetail.getPhaseCode()).thenReturn(com.elster.jupiter.cbo.PhaseCode.S1);
        when(usagePoint.getEffectiveMetrologyConfiguration(NOW)).thenReturn(Optional.of(effectiveMC));
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getState()).thenReturn(state);
        when(state.getName()).thenReturn(STATE_NAME);
        when(usagePoint.getInstallationTime()).thenReturn(CREATION_DATE);
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.of(usagePointConnectionState));
        when(usagePointConnectionState.getConnectionState()).thenReturn(ConnectionState.CONNECTED);
        when(usagePoint.forCustomProperties()).thenReturn(usagePointCustomPropertySetExtension);
        when(usagePointCustomPropertySetExtension.getAllPropertySets()).thenReturn(Collections.emptyList());
    }

    @Test
    public void testNoPayload() throws Exception {
        // Prepare request
        UsagePointConfigRequestMessageType usagePointConfigRequest = usagePointConfigMessageFactory.createUsagePointConfigRequestMessageType();
        usagePointConfigRequest.setHeader(new HeaderType());

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'Payload' is required.");
    }

    @Test
    public void testNoHeader() throws Exception {
        // Prepare request
        UsagePointConfigRequestMessageType usagePointConfigRequest = usagePointConfigMessageFactory.createUsagePointConfigRequestMessageType();

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'Header' is required.");
    }

    @Test
    public void testNoUsagePointConfigInPayload() throws Exception {
        // Prepare request
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(null);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig' is required.");
    }

    @Test
    public void testNoUsagePointsInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'UsagePointConfig.UsagePoint' cannot be empty.");
    }

    @Test
    public void testNoServiceCategoryInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        usagePointInfo.setServiceCategory(null);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].ServiceCategory' is required.");
    }

    @Test
    public void testNoServiceKindInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, null, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].ServiceCategory.kind' is required.");
    }

    @Test
    public void testNoServiceCategoryFoundByServiceKind() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.SEWERAGE, null, UsagePointConnectedKind.CONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        when(meteringService.getServiceCategory(com.elster.jupiter.metering.ServiceKind.SEWERAGE)).thenReturn(Optional.empty());

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_SERVICE_CATEGORY_FOUND.getErrorCode(),
                "No active service category is found for 'Sewerage'.");
    }

    @Test
    public void testFoundServiceCategoryIsInactive() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        when(serviceCategory.isActive()).thenReturn(false);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_SERVICE_CATEGORY_FOUND.getErrorCode(),
                "No active service category is found for 'Electricity'.");
    }

    @Test
    public void testNoNameInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, null, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].Names[0].name' is empty or contains only white spaces.");
    }

    @Test
    public void testEmptyNameInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, "\n \t", CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].Names[0].name' is empty or contains only white spaces.");
    }

    @Test
    public void testNoIsSDPInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                null, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].isSdp' is required.");
    }

    @Test
    public void testNoIsVirtualInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, null, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].isVirtual' is required.");
    }

    @Test
    public void testNoMetrologyConfigurationNameInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        usagePointInfo.getMetrologyRequirements().add(new MetrologyRequirements());
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].MetrologyRequirements[0].Names[0].name' is empty or contains only white spaces.");
    }

    @Test
    public void testEmptyMetrologyConfigurationNameInUsagePointConfig() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        setMetrologyConfiguration(usagePointInfo, "");
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'UsagePointConfig.UsagePoint[0].MetrologyRequirements[0].Names[0].name' is empty or contains only white spaces.");
    }


    @Test
    public void testNoMetrologyConfigurationFoundByName() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        setMetrologyConfiguration(usagePointInfo, METROLOGY_CONFIGURATION_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        when(metrologyConfigurationService.findMetrologyConfiguration(METROLOGY_CONFIGURATION_NAME)).thenReturn(Optional.empty());

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_METROLOGY_CONFIGURATION_WITH_NAME.getErrorCode(),
                "No metrology configuration suitable for usage point is found by name '" + METROLOGY_CONFIGURATION_NAME + "'.");
    }

    @Test
    public void testUnsuitableMetrologyConfigurationFoundByName() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        setMetrologyConfiguration(usagePointInfo, METROLOGY_CONFIGURATION_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        MetrologyConfiguration mc = mock(MetrologyConfiguration.class);
        when(metrologyConfigurationService.findMetrologyConfiguration(METROLOGY_CONFIGURATION_NAME)).thenReturn(Optional.of(mc));

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.NO_METROLOGY_CONFIGURATION_WITH_NAME.getErrorCode(),
                "No metrology configuration suitable for usage point is found by name '" + METROLOGY_CONFIGURATION_NAME + "'.");
    }

    @Test
    public void testSuccessfulCreationWithMetrologyConfigurationInResponse() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        setMetrologyConfiguration(usagePointInfo, METROLOGY_CONFIGURATION_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(serviceCategory).newUsagePoint(USAGE_POINT_NAME, CREATION_DATE);
        verify(usagePointBuilder).withIsSdp(true);
        verify(usagePointBuilder).withIsVirtual(false);
        verify(usagePointBuilder).create();
//        verifyNoMoreInteractions(usagePointBuilder);

        verify(usagePoint).newElectricityDetailBuilder(CREATION_DATE);
        verify((ElectricityDetailBuilder) usagePointDetailBuilder).withPhaseCode(com.elster.jupiter.cbo.PhaseCode.S1);
        verify(usagePointDetailBuilder).create();
        verifyNoMoreInteractions(usagePointDetailBuilder);

        verify(usagePoint).apply(metrologyConfiguration, CREATION_DATE);

        verify(usagePoint).setConnectionState(ConnectionState.CONNECTED, CREATION_DATE);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
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
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(STATE_NAME);
        //assertThat(responseUsagePointInfo.getPhaseCode()).isEqualTo(PhaseCode.S_1); // fails because of: UsagePointCOnfigFactory - line 72
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.CONNECTED);

        List<MetrologyRequirements> metrologyRequirements = responseUsagePointInfo.getMetrologyRequirements();
        assertThat(metrologyRequirements).hasSize(2);
        assertThat(metrologyRequirements.stream()
                .map(MetrologyRequirements::getReason)
                .collect(Collectors.toList())).containsOnly(BILLING_NAME, CHECK_NAME);
        MetrologyRequirements req = metrologyRequirements.stream()
                .filter(any -> any.getReason().equals(BILLING_NAME))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
        assertThat(req.getNames().get(0).getName()).isEqualTo(METROLOGY_CONFIGURATION_NAME);
        assertThat(req.getReadingTypes().stream()
                .map(MetrologyRequirements.ReadingTypes::getRef)
                .collect(Collectors.toList())).containsOnly(readingType1.getMRID());
        req = metrologyRequirements.stream()
                .filter(any -> any.getReason().equals(CHECK_NAME))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
        assertThat(req.getNames().get(0).getName()).isEqualTo(METROLOGY_CONFIGURATION_NAME);
        assertThat(req.getReadingTypes().stream()
                .map(MetrologyRequirements.ReadingTypes::getRef)
                .collect(Collectors.toList())).containsOnly(readingType1.getMRID(), readingType2.getMRID());

        List<ch.iec.tc57._2011.usagepointconfig.ReadingType> readingTypes = responseUsagePointConfig.getReadingType();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes.stream()
                .map(ch.iec.tc57._2011.usagepointconfig.ReadingType::getMRID)
                .collect(Collectors.toList())).containsOnly(readingType1.getMRID(), readingType2.getMRID());
        ch.iec.tc57._2011.usagepointconfig.ReadingType rt = readingTypes.stream()
                .filter(any -> any.getMRID().equals(readingType1.getMRID()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
        assertReadingType1(rt);
        rt = readingTypes.stream()
                .filter(any -> any.getMRID().equals(readingType2.getMRID()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
        assertReadingType2(rt);

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(CREATION_DATE);
    }

    @Test
    public void testSuccessfulCreationOfElectricityUsagePointWithNoPhaseCode() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, null, null);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Re-mock usage point
        when(electricityDetail.getPhaseCode()).thenReturn(null);
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.empty());

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(serviceCategory).newUsagePoint(USAGE_POINT_NAME, CREATION_DATE);
        verify(usagePointBuilder).withIsSdp(true);
        verify(usagePointBuilder).withIsVirtual(false);
        verify(usagePointBuilder).create();
//        verifyNoMoreInteractions(usagePointBuilder);

        verify(usagePoint).newElectricityDetailBuilder(CREATION_DATE);
        verify(usagePointDetailBuilder).create();
        verifyNoMoreInteractions(usagePointDetailBuilder);

        verify(usagePoint, never()).setConnectionState(any(ConnectionState.class), any(Instant.class));
        verify(usagePoint, never()).setConnectionState(any(ConnectionState.class));

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
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
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(STATE_NAME);
        assertThat(responseUsagePointInfo.getPhaseCode()).isNull();
        assertThat(responseUsagePointInfo.getConnectionState()).isNull();

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(CREATION_DATE);
    }

    @Test
    public void testSuccessfulCreationWithOtherParameters() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID + 1, USAGE_POINT_NAME + 1, null,
                false, true, ServiceKind.GAS, null, UsagePointConnectedKind.LOGICALLY_DISCONNECTED);
        setMetrologyConfiguration(usagePointInfo, METROLOGY_CONFIGURATION_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Re-mock usage point
        when(usagePoint.getMRID()).thenReturn(USAGE_POINT_MRID + 1);
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME + 1);
        when(usagePoint.isSdp()).thenReturn(false);
        when(usagePoint.isVirtual()).thenReturn(true);
        when(usagePoint.getEffectiveMetrologyConfiguration(NOW)).thenReturn(Optional.empty());
        when(usagePoint.newGasDetailBuilder(any(Instant.class))).thenReturn((GasDetailBuilder) usagePointDetailBuilder);
        when(meteringService.getServiceCategory(com.elster.jupiter.metering.ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.getKind()).thenReturn(com.elster.jupiter.metering.ServiceKind.GAS);
        when(usagePoint.getInstallationTime()).thenReturn(REQUEST_DATE);
        when(usagePointConnectionState.getConnectionState()).thenReturn(ConnectionState.LOGICALLY_DISCONNECTED);
        when(state.getName()).thenReturn(DefaultState.UNDER_CONSTRUCTION.getKey());

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(serviceCategory).newUsagePoint(USAGE_POINT_NAME + 1, REQUEST_DATE);
        verify(usagePointBuilder).withIsSdp(false);
        verify(usagePointBuilder).withIsVirtual(true);
        verify(usagePointBuilder).create();
//        verifyNoMoreInteractions(usagePointBuilder);

        verify(usagePoint).newGasDetailBuilder(REQUEST_DATE);
        verify(usagePointDetailBuilder).create();
//        verifyNoMoreInteractions(usagePointDetailBuilder);

        verify(usagePoint).apply(metrologyConfiguration, REQUEST_DATE);

        verify(usagePoint).setConnectionState(ConnectionState.LOGICALLY_DISCONNECTED, REQUEST_DATE);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
        assertThat(response.getHeader().getNoun()).isEqualTo("UsagePointConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        UsagePointConfig responseUsagePointConfig = response.getPayload().getUsagePointConfig();
        assertThat(responseUsagePointConfig.getUsagePoint()).hasSize(1);

        ch.iec.tc57._2011.usagepointconfig.UsagePoint responseUsagePointInfo = responseUsagePointConfig.getUsagePoint().get(0);
        assertThat(responseUsagePointInfo.getMRID()).isEqualTo(USAGE_POINT_MRID + 1);
        assertThat(responseUsagePointInfo.getNames().get(0).getName()).isEqualTo(USAGE_POINT_NAME + 1);
        assertThat(responseUsagePointInfo.isIsSdp()).isEqualTo(false);
        assertThat(responseUsagePointInfo.isIsVirtual()).isEqualTo(true);
        assertThat(responseUsagePointInfo.getServiceCategory().getKind()).isEqualTo(ServiceKind.GAS);
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(DefaultState.UNDER_CONSTRUCTION.getTranslation().getDefaultFormat());
        assertThat(responseUsagePointInfo.getPhaseCode()).isNull();
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.LOGICALLY_DISCONNECTED);

        List<MetrologyRequirements> metrologyRequirements = responseUsagePointInfo.getMetrologyRequirements();
        assertThat(metrologyRequirements).isEmpty();

        List<ch.iec.tc57._2011.usagepointconfig.ReadingType> readingTypes = responseUsagePointConfig.getReadingType();
        assertThat(readingTypes).isEmpty();

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(REQUEST_DATE);
    }

    @Test
    public void testSuccessfulCreationWithNoDateProvided() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, null,
                true, false, ServiceKind.WATER, null, UsagePointConnectedKind.PHYSICALLY_DISCONNECTED);
        setMetrologyConfiguration(usagePointInfo, METROLOGY_CONFIGURATION_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);
        usagePointConfigRequest.getHeader().setTimestamp(null);

        // Re-mock usage point
        when(usagePoint.getEffectiveMetrologyConfiguration(NOW)).thenReturn(Optional.empty());
        when(usagePoint.newWaterDetailBuilder(any(Instant.class))).thenReturn((WaterDetailBuilder) usagePointDetailBuilder);
        when(meteringService.getServiceCategory(com.elster.jupiter.metering.ServiceKind.WATER)).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.getKind()).thenReturn(com.elster.jupiter.metering.ServiceKind.WATER);
        when(usagePoint.getInstallationTime()).thenReturn(NOW);
        when(usagePointConnectionState.getConnectionState()).thenReturn(ConnectionState.PHYSICALLY_DISCONNECTED);

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(serviceCategory).newUsagePoint(USAGE_POINT_NAME, NOW);
        verify(usagePointBuilder).withIsSdp(true);
        verify(usagePointBuilder).withIsVirtual(false);
        verify(usagePointBuilder).create();
//        verifyNoMoreInteractions(usagePointBuilder);

        verify(usagePoint).newWaterDetailBuilder(NOW);
        verify(usagePointDetailBuilder).create();
//        verifyNoMoreInteractions(usagePointDetailBuilder);

        verify(usagePoint).apply(metrologyConfiguration, NOW);

        verify(usagePoint).setConnectionState(ConnectionState.PHYSICALLY_DISCONNECTED, NOW);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
        assertThat(response.getHeader().getNoun()).isEqualTo("UsagePointConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        UsagePointConfig responseUsagePointConfig = response.getPayload().getUsagePointConfig();
        assertThat(responseUsagePointConfig.getUsagePoint()).hasSize(1);

        ch.iec.tc57._2011.usagepointconfig.UsagePoint responseUsagePointInfo = responseUsagePointConfig.getUsagePoint().get(0);
        assertThat(responseUsagePointInfo.getMRID()).isEqualTo(USAGE_POINT_MRID);
        assertThat(responseUsagePointInfo.getNames().get(0).getName()).isEqualTo(USAGE_POINT_NAME);
        assertThat(responseUsagePointInfo.isIsSdp()).isEqualTo(true);
        assertThat(responseUsagePointInfo.isIsVirtual()).isEqualTo(false);
        assertThat(responseUsagePointInfo.getServiceCategory().getKind()).isEqualTo(ServiceKind.WATER);
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(STATE_NAME);
        assertThat(responseUsagePointInfo.getPhaseCode()).isNull();
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.PHYSICALLY_DISCONNECTED);

        List<MetrologyRequirements> metrologyRequirements = responseUsagePointInfo.getMetrologyRequirements();
        assertThat(metrologyRequirements).isEmpty();

        List<ch.iec.tc57._2011.usagepointconfig.ReadingType> readingTypes = responseUsagePointConfig.getReadingType();
        assertThat(readingTypes).isEmpty();

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(NOW);
    }

    @Test
    public void testWarningIfMoreThanOneUsagePointSpecified() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.HEAT, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Re-mock usage point
        when(usagePoint.getEffectiveMetrologyConfiguration(NOW)).thenReturn(Optional.empty());
        when(usagePoint.newHeatDetailBuilder(any(Instant.class))).thenReturn((HeatDetailBuilder) usagePointDetailBuilder);
        when(meteringService.getServiceCategory(com.elster.jupiter.metering.ServiceKind.HEAT)).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.getKind()).thenReturn(com.elster.jupiter.metering.ServiceKind.HEAT);

        // Business method
        UsagePointConfigResponseMessageType response = executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest);

        // Assert invocations
        verify(serviceCategory).newUsagePoint(USAGE_POINT_NAME, CREATION_DATE);
        verify(usagePointBuilder).withIsSdp(true);
        verify(usagePointBuilder).withIsVirtual(false);
        verify(usagePointBuilder).create();
//        verifyNoMoreInteractions(usagePointBuilder);

        verify(usagePoint).newHeatDetailBuilder(CREATION_DATE);
        verify(usagePointDetailBuilder).create();
        verifyNoMoreInteractions(usagePointDetailBuilder);

        verify(usagePoint, never()).apply(any(UsagePointMetrologyConfiguration.class), any(Instant.class));

        verify(usagePoint).setConnectionState(ConnectionState.CONNECTED, CREATION_DATE);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
        assertThat(response.getHeader().getNoun()).isEqualTo("UsagePointConfig");
        ReplyType reply = response.getReply();
        assertThat(reply.getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertThat(reply.getError()).hasSize(1);
        assertThat(reply.getError().get(0).getCode()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.getErrorCode());
        assertThat(reply.getError().get(0).getLevel()).isEqualTo(ErrorType.Level.WARNING);
        assertThat(reply.getError().get(0).getDetails())
                .isEqualTo("Bulk operation isn't supported for 'UsagePointConfig.UsagePoint', only first element is processed.");

        UsagePointConfig responseUsagePointConfig = response.getPayload().getUsagePointConfig();
        assertThat(responseUsagePointConfig.getUsagePoint()).hasSize(1);

        ch.iec.tc57._2011.usagepointconfig.UsagePoint responseUsagePointInfo = responseUsagePointConfig.getUsagePoint().get(0);
        assertThat(responseUsagePointInfo.getMRID()).isEqualTo(USAGE_POINT_MRID);
        assertThat(responseUsagePointInfo.getNames().get(0).getName()).isEqualTo(USAGE_POINT_NAME);
        assertThat(responseUsagePointInfo.isIsSdp()).isEqualTo(true);
        assertThat(responseUsagePointInfo.isIsVirtual()).isEqualTo(false);
        assertThat(responseUsagePointInfo.getServiceCategory().getKind()).isEqualTo(ServiceKind.HEAT);
        assertThat(responseUsagePointInfo.getStatus().getValue()).isEqualTo(STATE_NAME);
        assertThat(responseUsagePointInfo.getPhaseCode()).isNull();
        assertThat(responseUsagePointInfo.getConnectionState()).isEqualTo(UsagePointConnectedKind.CONNECTED);

        List<MetrologyRequirements> metrologyRequirements = responseUsagePointInfo.getMetrologyRequirements();
        assertThat(metrologyRequirements).isEmpty();

        List<ch.iec.tc57._2011.usagepointconfig.ReadingType> readingTypes = responseUsagePointConfig.getReadingType();
        assertThat(readingTypes).isEmpty();

        assertThat(responseUsagePointInfo.getConfigurationEvents()).isNotNull();
        assertThat(responseUsagePointInfo.getConfigurationEvents().getCreatedDateTime()).isEqualTo(CREATION_DATE);
    }

    @Test
    public void testFailureWithLocalizedException() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        setMetrologyConfiguration(usagePointInfo, METROLOGY_CONFIGURATION_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Mock exception
        LocalizedException localizedException = mock(LocalizedException.class);
        when(localizedException.getLocalizedMessage()).thenReturn("ErrorMessage");
        when(localizedException.getErrorCode()).thenReturn("ERRORCODE");
        when(serviceCategory.newUsagePoint(USAGE_POINT_NAME, CREATION_DATE)).thenThrow(localizedException);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                "ERRORCODE",
                "ErrorMessage");
    }

    @Test
    public void testFailureWithVerboseConstraintViolationException() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CREATION_DATE,
                true, false, ServiceKind.ELECTRICITY, PhaseCode.S_1, UsagePointConnectedKind.CONNECTED);
        setMetrologyConfiguration(usagePointInfo, METROLOGY_CONFIGURATION_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);

        // Mock exception
        VerboseConstraintViolationException exception = mock(VerboseConstraintViolationException.class);
        when(exception.getLocalizedMessage()).thenReturn("ErrorMessage");
        doThrow(exception).when(usagePoint).apply(metrologyConfiguration, CREATION_DATE);

        // Business method & assertions
        assertFaultMessage(() -> executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest),
                MessageSeeds.DUPLICATE_USAGE_POINT_NAME.getErrorCode(),
                MessageSeeds.DUPLICATE_USAGE_POINT_NAME.getDefaultFormat());
    }

    @Test
    public void testAsync() throws Exception {
        // Prepare request
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = createUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, null,
                true, false, ServiceKind.WATER, null, UsagePointConnectedKind.PHYSICALLY_DISCONNECTED);
        setMetrologyConfiguration(usagePointInfo, METROLOGY_CONFIGURATION_NAME);
        usagePointConfig.getUsagePoint().add(usagePointInfo);
        UsagePointConfigRequestMessageType usagePointConfigRequest = createUsagePointConfigRequest(usagePointConfig);
        usagePointConfigRequest.getHeader().setTimestamp(null);
        usagePointConfigRequest.getHeader().setAsyncReplyFlag(true);

        // Execute
        executeUsagePointConfigEndpoint.createUsagePointConfig(usagePointConfigRequest);

        // Assert service call
        verify(serviceCall).requestTransition(com.elster.jupiter.servicecall.DefaultState.PENDING);
    }

    private ch.iec.tc57._2011.usagepointconfig.UsagePoint createUsagePoint(String mRID, String name, Instant creationDate,
                                                                           Boolean isSDP, Boolean isVirtual, ServiceKind serviceKind,
                                                                           PhaseCode phaseCode, UsagePointConnectedKind connectionState) {
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint = new ch.iec.tc57._2011.usagepointconfig.UsagePoint();
        usagePoint.setMRID(mRID);
        Optional.ofNullable(name)
                .map(this::name)
                .ifPresent(usagePoint.getNames()::add);
        if (creationDate != null) {
            ConfigurationEvent configuration = new ConfigurationEvent();
            configuration.setCreatedDateTime(creationDate);
            usagePoint.setConfigurationEvents(configuration);
        }
        usagePoint.setIsSdp(isSDP);
        usagePoint.setIsVirtual(isVirtual);
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setKind(serviceKind);
        usagePoint.setServiceCategory(serviceCategory);
        usagePoint.setPhaseCode(phaseCode);
        usagePoint.setConnectionState(connectionState);
        return usagePoint;
    }

    private void setMetrologyConfiguration(ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint, String metrologyConfigurationName) {
        MetrologyRequirements metrologyRequirement = new MetrologyRequirements();
        metrologyRequirement.getNames().add(metrologyConfigurationName(metrologyConfigurationName));
        metrologyRequirement.getReadingTypes().add(new MetrologyRequirements.ReadingTypes());
        usagePoint.getMetrologyRequirements().add(metrologyRequirement);
    }

    private UsagePointConfigRequestMessageType createUsagePointConfigRequest(UsagePointConfig usagePointConfig) {
        UsagePointConfigPayloadType usagePointConfigPayload = usagePointConfigMessageFactory.createUsagePointConfigPayloadType();
        usagePointConfigPayload.setUsagePointConfig(usagePointConfig);
        HeaderType header = objectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.CREATE);
        header.setTimestamp(REQUEST_DATE);
        UsagePointConfigRequestMessageType usagePointConfigRequestMessage = usagePointConfigMessageFactory.createUsagePointConfigRequestMessageType();
        usagePointConfigRequestMessage.setPayload(usagePointConfigPayload);
        usagePointConfigRequestMessage.setHeader(header);
        return usagePointConfigRequestMessage;
    }

    private Name name(String value) {
        Name name = new Name();
        name.setName(value);
        return name;
    }

    private MetrologyRequirements.Names metrologyConfigurationName(String value) {
        MetrologyRequirements.Names name = new MetrologyRequirements.Names();
        name.setName(value);
        MetrologyRequirements.Names.NameType nameType = new MetrologyRequirements.Names.NameType();
        nameType.setName("MetrologyConfiguration");
        name.setNameType(nameType);
        return name;
    }

    private void assertFaultMessage(RunnableWithFaultMessage action, String expectedCode, String expectedDetailedMessage) {
        try {
            // Business method
            action.run();
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo("Unable to create usage point");
            UsagePointConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getDetails()).isEqualTo(expectedDetailedMessage);
            assertThat(error.getCode()).isEqualTo(expectedCode);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);

            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Expected FaultMessage but got: " + System.lineSeparator() + e.toString());
        }
    }

    private interface RunnableWithFaultMessage {
        void run() throws FaultMessage;
    }
}
