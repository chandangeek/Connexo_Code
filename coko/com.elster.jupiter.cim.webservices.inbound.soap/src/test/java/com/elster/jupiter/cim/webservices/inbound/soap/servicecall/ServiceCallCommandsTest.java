/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.DataLinkageConfigChecklist;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageAction;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageFaultMessageFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.meterreadings.MeterReadingFaultMessageFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.ConfigEventInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigMasterDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.Action;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.impl.NlsModule.FakeThesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent;
import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.Name;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigFaultMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigRequestMessageType;
import oracle.sql.TIMESTAMP;

import java.math.BigDecimal;
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
    private static final String SERIALIZED_USAGE_POINT_OBJECT_FOR_USAGE_POINT_CONFIG = "SERIALIZED_USAGE_POINT_OBJECT_FOR_USAGE_POINT_CONFIG";

    private static final Instant CREATED_DATE_TIME = Instant.now();
    private static final Instant EFFECTIVE_DATE_TIME = CREATED_DATE_TIME.plus(10, ChronoUnit.HOURS);
    private static final Instant UP_REQUEST_TIMESTAMP = Instant.now().minus(10, ChronoUnit.DAYS);

    private static final String END_DEVICE_MRID = "END_DEVICE_MRID";
    private static final String READING_TYPE_MRID = "READING_TYPE_MRID";

    private static final String SOURCE = "SOURCE";
    private static final String REPLY_ADDRESS = "REPLY_ADDRESS";
    private static final DateTimeInterval DATE_TIME_INTERVAL = new DateTimeInterval();
    private static final Instant START = Instant.now().minus(16, ChronoUnit.MINUTES);
    private static final Instant END = Instant.now().plus(16, ChronoUnit.MINUTES);
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
    private MessageService messageService;

    @Mock
    private MeterReadingFaultMessageFactory faultMessageFactory;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UsagePointConfigRequestMessageType upConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointObj;

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
        when(serviceCallService.findServiceCallType(ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeName(),
                ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeVersion()))
                        .thenReturn(Optional.of(masterServiceCallType));
        when(serviceCallService.findServiceCallType(ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeName(),
                ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeVersion())).thenReturn(Optional.of(childServiceCallType));
        when(endPointConfiguration.getUrl()).thenReturn(CALLBACK_URL);
        when(masterServiceCallType.newServiceCall()).thenReturn(parentServiceCallBuilder);
        when(parentServiceCallBuilder.origin(DataLinkageConfigChecklist.APPLICATION_NAME))
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
        when(serviceCallService.findServiceCallType(ServiceCallTypes.MASTER_USAGE_POINT_CONFIG.getTypeName(),
                ServiceCallTypes.MASTER_USAGE_POINT_CONFIG.getTypeVersion()))
                        .thenReturn(Optional.of(masterServiceCallType));
        when(serviceCallService.findServiceCallType(ServiceCallTypes.USAGE_POINT_CONFIG.getTypeName(),
                ServiceCallTypes.USAGE_POINT_CONFIG.getTypeVersion())).thenReturn(Optional.of(childServiceCallType));
        when(upConfig.getPayload().getUsagePointConfig().getUsagePoint()).thenReturn(Arrays.asList(usagePointObj));
        when(upConfig.getHeader().getTimestamp()).thenReturn(UP_REQUEST_TIMESTAMP);
        when(jsonService.serialize(usagePointObj)).thenReturn(SERIALIZED_USAGE_POINT_OBJECT_FOR_USAGE_POINT_CONFIG);
        when(parentServiceCallBuilder.extendedWith(Mockito.any(UsagePointConfigMasterDomainExtension.class)))
                .thenReturn(parentServiceCallBuilder);
        when(childServiceCallBuilder.extendedWith(Mockito.any(UsagePointConfigDomainExtension.class)))
                .thenReturn(childServiceCallBuilder);
        when(serviceCallService.findServiceCallType(ServiceCallTypes.PARENT_GET_METER_READINGS.getTypeName(),
                ServiceCallTypes.PARENT_GET_METER_READINGS.getTypeVersion()))
                        .thenReturn(Optional.of(masterServiceCallType));
        when(parentServiceCallBuilder.extendedWith(Mockito.any(ParentGetMeterReadingsDomainExtension.class)))
                .thenReturn(parentServiceCallBuilder);
        when(endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(readingType.getMRID()).thenReturn(READING_TYPE_MRID);
        testable = new ServiceCallCommands(serviceCallService, jsonService, FakeThesaurus.INSTANCE, faultMessageFactory,
                messageService);
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
        when(serviceCallService.findServiceCallType(ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeName(),
                ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeVersion())).thenReturn(Optional.empty());
        try {
            testable.createMasterDataLinkageConfigMasterServiceCall(linkageConfig, Optional.of(endPointConfiguration),
                    MasterDataLinkageAction.CREATE, masterDataLinkageFaultMessageFactory);
            fail("Exception should be thrown");
        } catch (IllegalStateException e) {
            assertEquals(FakeThesaurus.INSTANCE.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE).format(
                    ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeName(),
                    ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG.getTypeVersion()), e.getLocalizedMessage());
        }
    }

    @Test
    public void testCreateMasterDataLinkageConfigMasterServiceCall_ChildServiceCallTypeNotFound() throws FaultMessage {
        when(serviceCallService.findServiceCallType(ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeName(),
                ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeVersion())).thenReturn(Optional.empty());
        try {
            testable.createMasterDataLinkageConfigMasterServiceCall(linkageConfig, Optional.of(endPointConfiguration),
                    MasterDataLinkageAction.CREATE, masterDataLinkageFaultMessageFactory);
            fail("Exception should be thrown");
        } catch (IllegalStateException e) {
            assertEquals(FakeThesaurus.INSTANCE.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE).format(
                    ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeName(),
                    ServiceCallTypes.DATA_LINKAGE_CONFIG.getTypeVersion()), e.getLocalizedMessage());
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

    @Test
    public void testCreateParentGetMeterReadingsServiceCall_Success()
            throws ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        testable.createParentGetMeterReadingsServiceCall(SOURCE, REPLY_ADDRESS, DATE_TIME_INTERVAL,
                Arrays.asList(endDevice), Arrays.asList(readingType));
        ArgumentCaptor<ParentGetMeterReadingsDomainExtension> captor = ArgumentCaptor
                .forClass(ParentGetMeterReadingsDomainExtension.class);
        verify(parentServiceCallBuilder).extendedWith(captor.capture());
        ParentGetMeterReadingsDomainExtension value = captor.getValue();
        assertEquals(SOURCE, value.getSource());
        assertEquals(REPLY_ADDRESS, value.getCallbackUrl());
        assertEquals(START, value.getTimePeriodStart());
        assertEquals(END, value.getTimePeriodEnd());
        assertEquals(END_DEVICE_MRID, value.getEndDevices());
        assertEquals(READING_TYPE_MRID, value.getReadingTypes());
    }

    @Test
    public void testCreateUsagePointConfigMasterServiceCall_Success() {
        testable.createUsagePointConfigMasterServiceCall(upConfig, Optional.of(endPointConfiguration), Action.CREATE);
        ArgumentCaptor<UsagePointConfigMasterDomainExtension> masterCaptor = ArgumentCaptor
                .forClass(UsagePointConfigMasterDomainExtension.class);
        verify(parentServiceCallBuilder).extendedWith(masterCaptor.capture());
        verify(parentServiceCallBuilder).create();
        UsagePointConfigMasterDomainExtension masterValue = masterCaptor.getValue();
        assertEquals(BigDecimal.ZERO, masterValue.getActualNumberOfSuccessfulCalls());
        assertEquals(BigDecimal.ZERO, masterValue.getActualNumberOfFailedCalls());
        assertEquals(BigDecimal.ONE, masterValue.getExpectedNumberOfCalls());
        assertEquals(CALLBACK_URL, masterValue.getCallbackURL());
        ArgumentCaptor<UsagePointConfigDomainExtension> childCaptor = ArgumentCaptor
                .forClass(UsagePointConfigDomainExtension.class);
        verify(childServiceCallBuilder).extendedWith(childCaptor.capture());
        verify(childServiceCallBuilder).create();
        UsagePointConfigDomainExtension childValue = childCaptor.getValue();
        assertEquals(PARENT_SERVICE_CALL_ID, childValue.getParentServiceCallId().longValue());
        assertEquals(SERIALIZED_USAGE_POINT_OBJECT_FOR_USAGE_POINT_CONFIG, childValue.getUsagePoint());
        assertEquals(Action.CREATE.name(), childValue.getOperation());
        assertEquals(UP_REQUEST_TIMESTAMP, childValue.getRequestTimestamp());
    }

    @Test
    public void testCreateUsagePointConfigMasterServiceCall_MasterServiceCallTypeNotFound() {
        when(serviceCallService.findServiceCallType(ServiceCallTypes.MASTER_USAGE_POINT_CONFIG.getTypeName(),
                ServiceCallTypes.MASTER_USAGE_POINT_CONFIG.getTypeVersion())).thenReturn(Optional.empty());
        try {
            testable.createUsagePointConfigMasterServiceCall(upConfig, Optional.of(endPointConfiguration),
                    Action.CREATE);
            fail("Exception should be thrown");
        } catch (IllegalStateException e) {
            assertEquals(FakeThesaurus.INSTANCE.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE).format(
                    ServiceCallTypes.MASTER_USAGE_POINT_CONFIG.getTypeName(),
                    ServiceCallTypes.MASTER_USAGE_POINT_CONFIG.getTypeVersion()), e.getLocalizedMessage());
        }
    }

    @Test
    public void testCreateUsagePointConfigMasterServiceCall_ChildServiceCallTypeNotFound() {
        when(serviceCallService.findServiceCallType(ServiceCallTypes.USAGE_POINT_CONFIG.getTypeName(),
                ServiceCallTypes.USAGE_POINT_CONFIG.getTypeVersion())).thenReturn(Optional.empty());
        try {
            testable.createUsagePointConfigMasterServiceCall(upConfig, Optional.of(endPointConfiguration),
                    Action.CREATE);
            fail("Exception should be thrown");
        } catch (IllegalStateException e) {
            assertEquals(FakeThesaurus.INSTANCE.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE).format(
                    ServiceCallTypes.USAGE_POINT_CONFIG.getTypeName(),
                    ServiceCallTypes.USAGE_POINT_CONFIG.getTypeVersion()), e.getLocalizedMessage());
        }
    }
}
