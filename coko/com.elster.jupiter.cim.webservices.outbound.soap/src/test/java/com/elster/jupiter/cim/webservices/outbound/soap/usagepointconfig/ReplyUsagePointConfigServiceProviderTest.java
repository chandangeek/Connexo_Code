/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.outbound.soap.FailedUsagePointOperation;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.util.exception.MessageSeed;

import ch.iec.tc57._2011.replyusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.replyusagepointconfig.UsagePointConfigPort;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigEventMessageType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReplyUsagePointConfigServiceProviderTest {

    private static final String URL = "url";
    private static final String UP_MRID = "UP_MRID";
    private static final String UP_NAME = "UP_NAME";
    private static final String ENDPOINT_CONFIG_URL = "ENDPOINT_CONFIG_URL";
    private static final Instant NOW = Instant.now();
    private static final Instant INSTALLATION_TIME = Instant.now().minus(10, ChronoUnit.HOURS);
    private static final String OPERATION_CREATE = "CREATE";
    private static final String OPERATION_UPDATE = "UPDATE";
    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    private static final String ERR_UP_MRID = "ERR_UP_MRID";
    private static final String ERR_UP_NAME = "ERR_UP_NAME";
    private static final FailedUsagePointOperation FAILED_OP = new FailedUsagePointOperation();
    private static final Map<String, Object> PROPS = new HashMap<>();
    static {
        PROPS.put(URL, ENDPOINT_CONFIG_URL);
        PROPS.put("epcId", 1L);
        FAILED_OP.setErrorCode(ERROR_CODE);
        FAILED_OP.setErrorMessage(ERROR_MESSAGE);
        FAILED_OP.setUsagePointMrid(ERR_UP_MRID);
        FAILED_OP.setUsagePointName(ERR_UP_NAME);
    }
    private static final BigDecimal EXPECTED_NUMBER_OF_CALLS = BigDecimal.valueOf(2);
    private static final String UP_STATE_KEY = DefaultState.UNDER_CONSTRUCTION.getKey();
    private static final String CORRELATION_ID = "CorrelationID";

    private ReplyUsagePointConfigServiceProvider testable;

    @Mock
    private WebServicesService webServicesService;
    @Mock
    private WebServiceCallOccurrenceService webServiceCallOccurrenceService;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private EndPointConfiguration endPointConfiguration;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private UsagePointConfigPort usagePointConfigPort;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private State state;
    @Mock
    private UsagePointCustomPropertySetExtension usagepointCustomPropertySetExtension;
    @Mock
    private WebServiceCallOccurrence webServiceCallOccurrence;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private OutboundEndPointProvider.RequestSender requestSender;

    @Before
    public void setUp() {
        testable = spy(new ReplyUsagePointConfigServiceProvider());
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(webServiceCallOccurrenceService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        when(thesaurus.getSimpleFormat(any(MessageSeed.class))).thenReturn(mock(NlsMessageFormat.class));
        inject(AbstractOutboundEndPointProvider.class, testable, "thesaurus", thesaurus);
        inject(AbstractOutboundEndPointProvider.class, testable, "webServicesService", webServicesService);
        inject(AbstractOutboundEndPointProvider.class, testable, "webServiceCallOccurrenceService", webServiceCallOccurrenceService);
        testable.setClock(Clock.fixed(NOW, ZoneId.systemDefault()));
        testable.setCustomPropertySetService(customPropertySetService);
        testable.addUsagePointConfigPort(usagePointConfigPort, PROPS);
        testable.onActivate();
        when(endPointConfiguration.isActive()).thenReturn(true);
        when(endPointConfiguration.getUrl()).thenReturn(ENDPOINT_CONFIG_URL);
        when(webServicesService.isPublished(endPointConfiguration)).thenReturn(false);
        when(usagePoint.getMRID()).thenReturn(UP_MRID);
        when(usagePoint.getName()).thenReturn(UP_NAME);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(usagePoint.getEffectiveMetrologyConfiguration(NOW)).thenReturn(Optional.empty());
        when(usagePoint.getState()).thenReturn(state);
        when(state.getName()).thenReturn(UP_STATE_KEY);
        when(usagePoint.getInstallationTime()).thenReturn(INSTALLATION_TIME);
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.empty());
        when(usagePoint.forCustomProperties()).thenReturn(usagepointCustomPropertySetExtension);
        when(usagepointCustomPropertySetExtension.getAllPropertySets()).thenReturn(Collections.emptyList());
        when(testable.using(anyString())).thenReturn(requestSender);
        when(requestSender.withRelatedAttributes(any(SetMultimap.class ))).thenReturn(requestSender);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
    }

    @Test
    public void testCallCreatePartial() throws FaultMessage {
        ArgumentCaptor<UsagePointConfigEventMessageType> responseMessageCaptor = ArgumentCaptor
                .forClass(UsagePointConfigEventMessageType.class);
        testable.call(endPointConfiguration, OPERATION_CREATE, Arrays.asList(usagePoint), Arrays.asList(FAILED_OP),
                EXPECTED_NUMBER_OF_CALLS, CORRELATION_ID);
        //verify(webServicesService).publishEndPoint(endPointConfiguration);
        SetMultimap<String, String> values = HashMultimap.create();
        Arrays.asList(usagePoint).forEach(up->{
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), up.getMRID());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), up.getName());
        });
        Arrays.asList(FAILED_OP).forEach(upF->{
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), upF.getUsagePointMrid());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), upF.getUsagePointName());
        });
        verify(testable).using("createdUsagePointConfig");
        verify(requestSender).withRelatedAttributes(values);
        verify(requestSender).send(responseMessageCaptor.capture());
        UsagePointConfigEventMessageType value = responseMessageCaptor.getValue();
        assertEquals(ReplyType.Result.PARTIAL, value.getReply().getResult());
        assertEquals(1, value.getReply().getError().size());
        assertEquals(ERROR_CODE, value.getReply().getError().get(0).getCode());
        assertEquals(ERROR_MESSAGE, value.getReply().getError().get(0).getDetails());
        assertEquals(ERR_UP_MRID, value.getReply().getError().get(0).getObject().getMRID());
        assertEquals(ERR_UP_NAME, value.getReply().getError().get(0).getObject().getName().get(0).getName());
        assertEquals(1, value.getPayload().getUsagePointConfig().getUsagePoint().size());
        assertEquals(UP_MRID, value.getPayload().getUsagePointConfig().getUsagePoint().get(0).getMRID());
        assertEquals(UP_NAME,
                value.getPayload().getUsagePointConfig().getUsagePoint().get(0).getNames().get(0).getName());
        assertEquals(CORRELATION_ID, value.getHeader().getCorrelationID());
    }

    @Test
    public void testCallCreateSuccess() throws FaultMessage {
        ArgumentCaptor<UsagePointConfigEventMessageType> responseMessageCaptor = ArgumentCaptor
                .forClass(UsagePointConfigEventMessageType.class);
        testable.call(endPointConfiguration, OPERATION_CREATE, Arrays.asList(usagePoint), Collections.emptyList(),
                BigDecimal.ONE, CORRELATION_ID);
        SetMultimap<String, String> values = HashMultimap.create();
        Arrays.asList(usagePoint).forEach(up->{
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), up.getMRID());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), up.getName());
        });
        //verify(webServicesService).publishEndPoint(endPointConfiguration);
        verify(testable).using("createdUsagePointConfig");
        verify(requestSender).withRelatedAttributes(values);
        verify(requestSender).send(responseMessageCaptor.capture());
        UsagePointConfigEventMessageType value = responseMessageCaptor.getValue();
        assertEquals(ReplyType.Result.OK, value.getReply().getResult());
        assertTrue(value.getReply().getError().isEmpty());
        assertEquals(1, value.getPayload().getUsagePointConfig().getUsagePoint().size());
        assertEquals(UP_MRID, value.getPayload().getUsagePointConfig().getUsagePoint().get(0).getMRID());
        assertEquals(UP_NAME,
                value.getPayload().getUsagePointConfig().getUsagePoint().get(0).getNames().get(0).getName());
        assertEquals(CORRELATION_ID, value.getHeader().getCorrelationID());
    }

    @Test
    public void testCallCreateFailed() throws FaultMessage {
        ArgumentCaptor<UsagePointConfigEventMessageType> responseMessageCaptor = ArgumentCaptor
                .forClass(UsagePointConfigEventMessageType.class);
        testable.call(endPointConfiguration, OPERATION_CREATE, Collections.emptyList(), Arrays.asList(FAILED_OP),
                BigDecimal.ONE, CORRELATION_ID);
        SetMultimap<String, String> values = HashMultimap.create();
        Arrays.asList(FAILED_OP).forEach(upF->{
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), upF.getUsagePointMrid());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), upF.getUsagePointName());
        });
        //verify(webServicesService).publishEndPoint(endPointConfiguration);
        verify(testable).using("createdUsagePointConfig");
        verify(requestSender).withRelatedAttributes(values);
        verify(requestSender).send(responseMessageCaptor.capture());
        UsagePointConfigEventMessageType value = responseMessageCaptor.getValue();
        assertEquals(ReplyType.Result.FAILED, value.getReply().getResult());
        assertEquals(1, value.getReply().getError().size());
        assertEquals(ERROR_CODE, value.getReply().getError().get(0).getCode());
        assertEquals(ERROR_MESSAGE, value.getReply().getError().get(0).getDetails());
        assertEquals(ERR_UP_MRID, value.getReply().getError().get(0).getObject().getMRID());
        assertEquals(ERR_UP_NAME, value.getReply().getError().get(0).getObject().getName().get(0).getName());
        assertTrue(value.getPayload().getUsagePointConfig().getUsagePoint().isEmpty());
        assertEquals(CORRELATION_ID, value.getHeader().getCorrelationID());
    }

    @Test
    public void testCallUpdatePartial() throws FaultMessage {
        ArgumentCaptor<UsagePointConfigEventMessageType> responseMessageCaptor = ArgumentCaptor
                .forClass(UsagePointConfigEventMessageType.class);
        testable.call(endPointConfiguration, OPERATION_UPDATE, Arrays.asList(usagePoint), Arrays.asList(FAILED_OP),
                EXPECTED_NUMBER_OF_CALLS, CORRELATION_ID);
        SetMultimap<String, String> values = HashMultimap.create();
        Arrays.asList(usagePoint).forEach(up->{
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), up.getMRID());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), up.getName());
        });
        Arrays.asList(FAILED_OP).forEach(upF->{
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), upF.getUsagePointMrid());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), upF.getUsagePointName());
        });
        //verify(webServicesService).publishEndPoint(endPointConfiguration);
        verify(testable).using("changedUsagePointConfig");
        verify(requestSender).withRelatedAttributes(values);
        verify(requestSender).send(responseMessageCaptor.capture());
        UsagePointConfigEventMessageType value = responseMessageCaptor.getValue();
        assertEquals(ReplyType.Result.PARTIAL, value.getReply().getResult());
        assertEquals(1, value.getReply().getError().size());
        assertEquals(ERROR_CODE, value.getReply().getError().get(0).getCode());
        assertEquals(ERROR_MESSAGE, value.getReply().getError().get(0).getDetails());
        assertEquals(ERR_UP_MRID, value.getReply().getError().get(0).getObject().getMRID());
        assertEquals(ERR_UP_NAME, value.getReply().getError().get(0).getObject().getName().get(0).getName());
        assertEquals(1, value.getPayload().getUsagePointConfig().getUsagePoint().size());
        assertEquals(UP_MRID, value.getPayload().getUsagePointConfig().getUsagePoint().get(0).getMRID());
        assertEquals(UP_NAME,
                value.getPayload().getUsagePointConfig().getUsagePoint().get(0).getNames().get(0).getName());
        assertEquals(CORRELATION_ID, value.getHeader().getCorrelationID());
    }

    private static void inject(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
