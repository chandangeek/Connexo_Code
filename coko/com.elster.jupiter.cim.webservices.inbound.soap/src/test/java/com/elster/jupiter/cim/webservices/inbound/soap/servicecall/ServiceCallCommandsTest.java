/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.DataLinkageConfigChecklist;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigMasterDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.Action;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.impl.NlsModule.FakeThesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigRequestMessageType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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
    private static final String CORRELATION_ID = "CorrelationID";
    static {
        DATE_TIME_INTERVAL.setStart(START);
        DATE_TIME_INTERVAL.setEnd(END);
    }

    @Mock
    private ServiceCallService serviceCallService;

    @Mock
    private JsonService jsonService;

    @Mock
    private EndPointConfiguration endPointConfiguration;

    @Mock
    private ServiceCallType masterServiceCallType, childServiceCallType;

    @Mock
    private ServiceCall parentServiceCall, childServiceCall;

    @Mock
    private ServiceCallBuilder parentServiceCallBuilder, childServiceCallBuilder;

    private ServiceCallCommands testable;

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
        when(endPointConfiguration.getUrl()).thenReturn(CALLBACK_URL);
        when(masterServiceCallType.newServiceCall()).thenReturn(parentServiceCallBuilder);
        when(parentServiceCallBuilder.origin(DataLinkageConfigChecklist.APPLICATION_NAME))
                .thenReturn(parentServiceCallBuilder);
        when(parentServiceCallBuilder.create()).thenReturn(parentServiceCall);
        when(parentServiceCall.getId()).thenReturn(PARENT_SERVICE_CALL_ID);
        when(parentServiceCall.newChildCall(childServiceCallType)).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.create()).thenReturn(childServiceCall);
        when(serviceCallService.findServiceCallType(ServiceCallTypes.MASTER_USAGE_POINT_CONFIG.getTypeName(),
                ServiceCallTypes.MASTER_USAGE_POINT_CONFIG.getTypeVersion()))
                        .thenReturn(Optional.of(masterServiceCallType));
        when(serviceCallService.findServiceCallType(ServiceCallTypes.USAGE_POINT_CONFIG.getTypeName(),
                ServiceCallTypes.USAGE_POINT_CONFIG.getTypeVersion())).thenReturn(Optional.of(childServiceCallType));
        when(upConfig.getPayload().getUsagePointConfig().getUsagePoint()).thenReturn(Arrays.asList(usagePointObj));
        when(upConfig.getHeader().getTimestamp()).thenReturn(UP_REQUEST_TIMESTAMP);
        when(upConfig.getHeader().getCorrelationID()).thenReturn(CORRELATION_ID);
        when(jsonService.serialize(usagePointObj)).thenReturn(SERIALIZED_USAGE_POINT_OBJECT_FOR_USAGE_POINT_CONFIG);
        when(parentServiceCallBuilder.extendedWith(Mockito.any(UsagePointConfigMasterDomainExtension.class)))
                .thenReturn(parentServiceCallBuilder);
        when(childServiceCallBuilder.extendedWith(Mockito.any(UsagePointConfigDomainExtension.class)))
                .thenReturn(childServiceCallBuilder);
        when(endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(readingType.getMRID()).thenReturn(READING_TYPE_MRID);
        testable = new ServiceCallCommands(serviceCallService, jsonService, FakeThesaurus.INSTANCE);
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
        assertEquals(CORRELATION_ID, masterValue.getCorrelationId());
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
