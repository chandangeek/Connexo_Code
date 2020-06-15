/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.impl.NlsModule.FakeThesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents.EndDeviceEventsBuilder;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageAction;
import com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigParser;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.ConfigEventInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.masterdata.MasterDataService;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent;
import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.Name;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigFaultMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigRequestMessageType;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCallCommandsTest {

    private static final long PARENT_SERVICE_CALL_ID = 123L;

    private static final String CALLBACK_URL = "CALLBACK_URL";
    private static final String METER_MRID = "METER_MRID";
    private static final String METER_NAME = "METER_NAME";
    private static final String METER_ROLE = "METER_ROLE";
    private static final String USAGE_POINT_MRID = "USAGE_POINT_MRID";
    private static final String USAGE_POINT_NAME = "USAGE_POINT_NAME";

    private static final String SERIALIZED_METER_INFO = "SERIALIZED_METER_INFO";
    private static final String SERIALIZED_USAGE_POINT_INFO = "SERIALIZED_USAGE_POINT_INFO";
    private static final String SERIALIZED_CONFIG_EVENT_INFO = "SERIALIZED_CONFIG_EVENT_INFO";

    private static final Instant CREATED_DATE_TIME = Instant.now();
    private static final Instant EFFECTIVE_DATE_TIME = CREATED_DATE_TIME.plus(10, ChronoUnit.HOURS);

    private static final String END_DEVICE_MRID = "END_DEVICE_MRID";
    private static final String READING_TYPE_MRID = "READING_TYPE_MRID";

    private static final String SOURCE = "SOURCE";
    private static final String REPLY_ADDRESS = "REPLY_ADDRESS";
    private static final DateTimeInterval DATE_TIME_INTERVAL = new DateTimeInterval();
    private static final Instant START = Instant.now().minus(16, ChronoUnit.MINUTES);
    private static final Instant END = Instant.now().plus(16, ChronoUnit.MINUTES);
    private static final String CORRELATION_ID = "CorrelationID";
    static {
        DATE_TIME_INTERVAL.setStart(START);
        DATE_TIME_INTERVAL.setEnd(END);
    }

    private Meter meter;

    private UsagePoint usagePoint;

    private ConfigurationEvent configurationEvent;

    @Mock
    private ServiceCallService serviceCallService;

    @Mock
    private JsonService jsonService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private MeterConfigParser meterConfigParser;
    @Mock
    private MeterConfigFaultMessageFactory meterConfigFaultMessageFactory;
    @Mock
    private EndDeviceEventsBuilder endDeviceEventsBuilder;
    @Mock
    private MeterReadingFaultMessageFactory faultMessageFactory;
    @Mock
    private Clock clock;
    @Mock
    private MasterDataService masterDataService;
    @Mock
    private CommunicationTaskService communicationTaskService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MasterDataLinkageConfigRequestMessageType linkageConfig;

    @Mock
    private EndPointConfiguration endPointConfiguration;

    @Mock
    private ServiceCallType masterServiceCallType, childServiceCallType;

    @Mock
    private ServiceCall parentServiceCall, childServiceCall;

    @Mock
    private MasterDataLinkageFaultMessageFactory masterDataLinkageFaultMessageFactory;

    @Mock
    private ServiceCallBuilder parentServiceCallBuilder, childServiceCallBuilder;

    private ServiceCallCommands testable;

    @Mock
    private com.elster.jupiter.metering.Meter endDevice;

    @Mock
    private ReadingType readingType;

    @Before
    public void setUp() {
        meter = new Meter();
        meter.setMRID(METER_MRID);
        Name meterName = new Name();
        meterName.setName(METER_NAME);
        meter.getNames().add(meterName);
        meter.setRole(METER_ROLE);
        usagePoint = new UsagePoint();
        usagePoint.setMRID(USAGE_POINT_MRID);
        Name upName = new Name();
        upName.setName(USAGE_POINT_NAME);
        usagePoint.getNames().add(upName);
        configurationEvent = new ConfigurationEvent();
        configurationEvent.setCreatedDateTime(CREATED_DATE_TIME);
        configurationEvent.setEffectiveDateTime(EFFECTIVE_DATE_TIME);
        when(linkageConfig.getPayload().getMasterDataLinkageConfig().getMeter()).thenReturn(Arrays.asList(meter));
        when(linkageConfig.getPayload().getMasterDataLinkageConfig().getUsagePoint())
                .thenReturn(Arrays.asList(usagePoint));
        when(linkageConfig.getPayload().getMasterDataLinkageConfig().getConfigurationEvent())
                .thenReturn(configurationEvent);
        when(serviceCallService.findServiceCallType(ServiceCallCommands.ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeName(),
                ServiceCallCommands.ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeVersion()))
                        .thenReturn(Optional.of(masterServiceCallType));
        when(serviceCallService.findServiceCallType(ServiceCallCommands.ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeName(),
                ServiceCallCommands.ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeVersion())).thenReturn(Optional.of(childServiceCallType));
        when(endPointConfiguration.getUrl()).thenReturn(CALLBACK_URL);
        when(masterServiceCallType.newServiceCall()).thenReturn(parentServiceCallBuilder);
        when(parentServiceCallBuilder.origin("MultiSense"))
                .thenReturn(parentServiceCallBuilder);
        when(parentServiceCallBuilder.extendedWith(Mockito.any(MasterDataLinkageConfigMasterDomainExtension.class)))
                .thenReturn(parentServiceCallBuilder);
        when(parentServiceCallBuilder.create()).thenReturn(parentServiceCall);
        when(parentServiceCall.getId()).thenReturn(PARENT_SERVICE_CALL_ID);
        when(jsonService.serialize(Mockito.anyObject())).thenAnswer(invocation -> {
            Object argument = invocation.getArguments()[0];
            if (argument instanceof MeterInfo) {
                return SERIALIZED_METER_INFO;
            } else if (argument instanceof UsagePointInfo) {
                return SERIALIZED_USAGE_POINT_INFO;
            } else if (argument instanceof ConfigEventInfo) {
                return SERIALIZED_CONFIG_EVENT_INFO;
            } else {
                return null;
            }
        });
        when(parentServiceCall.newChildCall(childServiceCallType)).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.extendedWith(Mockito.any(MasterDataLinkageConfigDomainExtension.class)))
                .thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.create()).thenReturn(childServiceCall);
        when(endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(readingType.getMRID()).thenReturn(READING_TYPE_MRID);
        testable = new ServiceCallCommands(deviceService, jsonService, meterConfigParser, meterConfigFaultMessageFactory,
                serviceCallService, endDeviceEventsBuilder, FakeThesaurus.INSTANCE, faultMessageFactory, clock,
                masterDataService, communicationTaskService);
    }

    @Test
    public void testCreateMasterDataLinkageConfigMasterServiceCall_Success() throws FaultMessage {
        testable.createMasterDataLinkageConfigMasterServiceCall(linkageConfig, Optional.of(endPointConfiguration),
                MasterDataLinkageAction.CREATE, masterDataLinkageFaultMessageFactory);
        ArgumentCaptor<MasterDataLinkageConfigMasterDomainExtension> parentDomainExtensionCaptor = ArgumentCaptor
                .forClass(MasterDataLinkageConfigMasterDomainExtension.class);
        verify(parentServiceCallBuilder).extendedWith(parentDomainExtensionCaptor.capture());
        final MasterDataLinkageConfigMasterDomainExtension parentDomainValue = parentDomainExtensionCaptor.getValue();
        assertEquals(BigDecimal.ZERO, parentDomainValue.getActualNumberOfSuccessfulCalls());
        assertEquals(BigDecimal.ZERO, parentDomainValue.getActualNumberOfFailedCalls());
        assertEquals(BigDecimal.ONE, parentDomainValue.getExpectedNumberOfCalls());
        assertEquals(CALLBACK_URL, parentDomainValue.getCallbackURL());
        verify(parentServiceCallBuilder).create();
        ArgumentCaptor<Object> jsonInfoCaptor = ArgumentCaptor.forClass(Object.class);
        verify(jsonService, times(3)).serialize(jsonInfoCaptor.capture());
        List<Object> objects = jsonInfoCaptor.getAllValues();
        MeterInfo meterInfoValue = (MeterInfo) objects.get(0);
        assertEquals(METER_MRID, meterInfoValue.getMrid());
        assertEquals(METER_NAME, meterInfoValue.getName());
        assertEquals(METER_ROLE, meterInfoValue.getRole());
        UsagePointInfo usagePontInfoValue = (UsagePointInfo) objects.get(1);
        assertEquals(USAGE_POINT_MRID, usagePontInfoValue.getMrid());
        assertEquals(USAGE_POINT_NAME, usagePontInfoValue.getName());
        ConfigEventInfo configEventInfoValue = (ConfigEventInfo) objects.get(2);
        assertEquals(CREATED_DATE_TIME, configEventInfoValue.getCreatedDateTime());
        assertEquals(EFFECTIVE_DATE_TIME, configEventInfoValue.getEffectiveDateTime());
        ArgumentCaptor<MasterDataLinkageConfigDomainExtension> childDomainExtensionCaptor = ArgumentCaptor
                .forClass(MasterDataLinkageConfigDomainExtension.class);
        verify(childServiceCallBuilder).extendedWith(childDomainExtensionCaptor.capture());
        MasterDataLinkageConfigDomainExtension childDomainValue = childDomainExtensionCaptor.getValue();
        assertEquals(PARENT_SERVICE_CALL_ID, childDomainValue.getParentServiceCallId().longValue());
        assertEquals(SERIALIZED_METER_INFO, childDomainValue.getMeter());
        assertEquals(SERIALIZED_USAGE_POINT_INFO, childDomainValue.getUsagePoint());
        assertEquals(SERIALIZED_CONFIG_EVENT_INFO, childDomainValue.getConfigurationEvent());
        assertEquals(MasterDataLinkageAction.CREATE.name(), childDomainValue.getOperation());
    }

    @Test
    public void testCreateMasterDataLinkageConfigMasterServiceCall_MasterServiceCallTypeNotFound() throws FaultMessage {
        when(serviceCallService.findServiceCallType(ServiceCallCommands.ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeName(),
                ServiceCallCommands.ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeVersion())).thenReturn(Optional.empty());
        try {
            testable.createMasterDataLinkageConfigMasterServiceCall(linkageConfig, Optional.of(endPointConfiguration),
                    MasterDataLinkageAction.CREATE, masterDataLinkageFaultMessageFactory);
            fail("Exception should be thrown");
        } catch (IllegalStateException e) {
            assertEquals(FakeThesaurus.INSTANCE.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE).format(
                    ServiceCallCommands.ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeName(),
                    ServiceCallCommands.ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeVersion()), e.getLocalizedMessage());
        }
    }

    @Test
    public void testCreateMasterDataLinkageConfigMasterServiceCall_ChildServiceCallTypeNotFound() throws FaultMessage {
        when(serviceCallService.findServiceCallType(ServiceCallCommands.ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeName(),
                ServiceCallCommands.ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeVersion())).thenReturn(Optional.empty());
        try {
            testable.createMasterDataLinkageConfigMasterServiceCall(linkageConfig, Optional.of(endPointConfiguration),
                    MasterDataLinkageAction.CREATE, masterDataLinkageFaultMessageFactory);
            fail("Exception should be thrown");
        } catch (IllegalStateException e) {
            assertEquals(FakeThesaurus.INSTANCE.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE).format(
                    ServiceCallCommands.ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeName(),
                    ServiceCallCommands.ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeVersion()), e.getLocalizedMessage());
        }
    }

    @Test
    public void testCreateMasterDataLinkageConfigMasterServiceCall_DifferentNumberOfMetersAndUsagePoints() {
        when(linkageConfig.getPayload().getMasterDataLinkageConfig().getUsagePoint())
                .thenReturn(Arrays.asList(usagePoint, new UsagePoint()));
        when(masterDataLinkageFaultMessageFactory.createMasterDataLinkageFaultMessage(MasterDataLinkageAction.CREATE,
                MessageSeeds.DIFFERENT_NUMBER_OF_METERS_AND_USAGE_POINTS, 1, 2))
                        .thenReturn(new FaultMessage("", new MasterDataLinkageConfigFaultMessageType()));
        try {
            testable.createMasterDataLinkageConfigMasterServiceCall(linkageConfig, Optional.of(endPointConfiguration),
                    MasterDataLinkageAction.CREATE, masterDataLinkageFaultMessageFactory);
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
            verify(masterDataLinkageFaultMessageFactory).createMasterDataLinkageFaultMessage(
                    MasterDataLinkageAction.CREATE, MessageSeeds.DIFFERENT_NUMBER_OF_METERS_AND_USAGE_POINTS, 1, 2);
        }
    }
}
