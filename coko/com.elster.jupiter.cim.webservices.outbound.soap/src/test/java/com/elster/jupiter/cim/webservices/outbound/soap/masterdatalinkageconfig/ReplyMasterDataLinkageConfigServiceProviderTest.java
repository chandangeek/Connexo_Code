/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.outbound.soap.FailedLinkageOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.LinkageOperation;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.exception.MessageSeed;

import ch.iec.tc57._2011.masterdatalinkageconfig.EndDevice;
import ch.iec.tc57._2011.masterdatalinkageconfig.MasterDataLinkageConfig;
import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigEventMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigPayloadType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigResponseMessageType;
import ch.iec.tc57._2011.replymasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.replymasterdatalinkageconfig.MasterDataLinkageConfigPort;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ReplyMasterDataLinkageConfigServiceProviderTest {

    private static final String FAILURE_METER_NAME = "my failure meter name";
    private static final String FAILURE_METER_MRID = "my failure meter mrid";
    private static final String SUCCESS_METER_NAME = "my success meter name";
    private static final String SUCCESS_METER_MRID = "my success meter mrid";
    private static final String SUCCESS_USAGE_POINT_NAME = "my success usagepoint name";
    private static final String SUCCESS_USAGE_POINT_MRID = "my success usagepoint mrid";
    private static final String ERROR_CODE = "my error code";
    private static final String ERROR_MESSAGE = "my error message";
    private static final String CORRELATION_ID = "CorrelationID";
    private static final String FAILURE_END_DEVICE_MRID = "my failure end device mrid";
    private static final String FAILURE_END_DEVICE_NAME = "my failure end device name";
    private static final String SUCCESS_END_DEVICE_MRID = "my success end device mrid";
    private static final String SUCCESS_END_DEVICE_NAME = "my success end device name";
    private ReplyMasterDataLinkageConfigServiceProvider provider;
    @Mock
    private EndPointConfiguration endPointConfiguration;
    @Mock
    private LinkageOperation successfulLinkage;
    @Mock
    private MasterDataLinkageConfigPort masterDataLinkageConfigPort;
    @Mock
    private FailedLinkageOperation failedLinkage;
    @Mock
    private WebServicesService webServicesService;
    @Mock
    private WebServiceCallOccurrence webServiceCallOccurrence;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    protected OutboundEndPointProvider.RequestSender requestSender;


    @Before
    public void setup() {
        provider = Mockito.spy(new ReplyMasterDataLinkageConfigServiceProvider());
        String url = "some url";
        Mockito.when(webServiceCallOccurrence.getId()).thenReturn(1l);
        Mockito.when(webServicesService.startOccurrence(Matchers.any(EndPointConfiguration.class), Matchers.anyString(), Matchers.anyString())).thenReturn(webServiceCallOccurrence);
        Mockito.when(thesaurus.getSimpleFormat(Matchers.any(MessageSeed.class))).thenReturn(Mockito.mock(NlsMessageFormat.class));
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", thesaurus);
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        provider.addMasterDataLinkageConfigPort(masterDataLinkageConfigPort, ImmutableMap.of("url", url, "epcId", 1l));
        Mockito.when(provider.using(Matchers.anyString())).thenReturn(requestSender);
        Mockito.when(requestSender.toEndpoints(Matchers.any(EndPointConfiguration.class))).thenReturn(requestSender);
        Mockito.when(requestSender.withRelatedAttributes(Matchers.anyObject())).thenReturn(requestSender);
        Mockito.when(endPointConfiguration.getUrl()).thenReturn(url);

        Mockito.when(failedLinkage.getErrorCode()).thenReturn(ERROR_CODE);
        Mockito.when(failedLinkage.getErrorMessage()).thenReturn(ERROR_MESSAGE);
        Mockito.when(failedLinkage.getMeterMrid()).thenReturn(FAILURE_METER_MRID);
        Mockito.when(failedLinkage.getMeterName()).thenReturn(FAILURE_METER_NAME);
        Mockito.when(failedLinkage.getEndDeviceMrid()).thenReturn(FAILURE_END_DEVICE_MRID);
        Mockito.when(failedLinkage.getEndDeviceName()).thenReturn(FAILURE_END_DEVICE_NAME);

        Mockito.when(successfulLinkage.getMeterMrid()).thenReturn(SUCCESS_METER_MRID);
        Mockito.when(successfulLinkage.getMeterName()).thenReturn(SUCCESS_METER_NAME);
        Mockito.when(successfulLinkage.getEndDeviceMrid()).thenReturn(SUCCESS_END_DEVICE_MRID);
        Mockito.when(successfulLinkage.getEndDeviceName()).thenReturn(SUCCESS_END_DEVICE_NAME);
        Mockito.when(successfulLinkage.getUsagePointMrid()).thenReturn(SUCCESS_USAGE_POINT_MRID);
        Mockito.when(successfulLinkage.getUsagePointName()).thenReturn(SUCCESS_USAGE_POINT_NAME);
    }

    @Test
    public void testCallCreateForSingleSuccess() throws FaultMessage {
        String operation = "CREATE";
        List<FailedLinkageOperation> failedLinkages = Collections.emptyList();
        List<LinkageOperation> successfulLinkages = Arrays.asList(successfulLinkage);
        BigDecimal expectedNumberOfCalls = BigDecimal.ONE;
        Mockito.when(masterDataLinkageConfigPort
                .createdMasterDataLinkageConfig(Matchers.any(MasterDataLinkageConfigEventMessageType.class)))
                        .thenAnswer(new Answer<MasterDataLinkageConfigResponseMessageType>() {
                            @Override
                            public MasterDataLinkageConfigResponseMessageType answer(InvocationOnMock invocation)
                                    throws Throwable {
                                MasterDataLinkageConfigEventMessageType message = invocation.getArgumentAt(0,
                                        MasterDataLinkageConfigEventMessageType.class);

                                Assert.assertNotNull(message);
                                Assert.assertNotNull(message.getReply());
                                Assert.assertEquals(ReplyType.Result.OK, message.getReply().getResult());
                                Assert.assertEquals(0, message.getReply().getError().size());
                                MasterDataLinkageConfigPayloadType payload = message.getPayload();
                                Assert.assertNotNull(payload);
                                MasterDataLinkageConfig config = payload.getMasterDataLinkageConfig();
                                Assert.assertNotNull(config);
                                List<Meter> meters = config.getMeter();
                                List<UsagePoint> usagePoints = config.getUsagePoint();
                                List<EndDevice> endDevices = config.getEndDevice();
                                Assert.assertEquals(1, meters.size());
                                Assert.assertEquals(1, usagePoints.size());
                                Assert.assertEquals(1, endDevices.size());
                                Meter meter = meters.get(0);
                                UsagePoint usagePoint = usagePoints.get(0);
                                EndDevice endDevice = endDevices.get(0);
                                verifyLinkage(meter, usagePoint, endDevice);
                                Assert.assertEquals(CORRELATION_ID, message.getHeader().getCorrelationID());
                                return null;
                            }

                        });

        provider.call(endPointConfiguration, operation, successfulLinkages, failedLinkages, expectedNumberOfCalls, CORRELATION_ID);

        SetMultimap<String,String> values = HashMultimap.create();
        successfulLinkages.forEach(link->{
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getMeterMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getMeterName());
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getEndDeviceMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getEndDeviceName());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), link.getUsagePointMrid());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), link.getUsagePointName());
        });

        failedLinkages.forEach(link->{
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getMeterMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getMeterName());
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getEndDeviceMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getEndDeviceName());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), link.getUsagePointMrid());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), link.getUsagePointName());
        });

        Mockito.verify(provider).using("createdMasterDataLinkageConfig");
        Mockito.verify(requestSender).toEndpoints(endPointConfiguration);
        Mockito.verify(requestSender).withRelatedAttributes(values);
        Mockito.verify(requestSender).send(Matchers.any(MasterDataLinkageConfigEventMessageType.class));
    }

    @Test
    public void testCallCloseForSingleFailure() throws FaultMessage {
        String operation = "CLOSE";
        List<FailedLinkageOperation> failedLinkages = Arrays.asList(failedLinkage);
        List<LinkageOperation> successfulLinkages = Collections.emptyList();
        BigDecimal expectedNumberOfCalls = BigDecimal.ONE;
        Mockito.when(masterDataLinkageConfigPort
                .closedMasterDataLinkageConfig(Matchers.any(MasterDataLinkageConfigEventMessageType.class)))
                        .thenAnswer(new Answer<MasterDataLinkageConfigResponseMessageType>() {
                            @Override
                            public MasterDataLinkageConfigResponseMessageType answer(InvocationOnMock invocation)
                                    throws Throwable {
                                MasterDataLinkageConfigEventMessageType message = invocation.getArgumentAt(0,
                                        MasterDataLinkageConfigEventMessageType.class);
                                Assert.assertNotNull(message);
                                Assert.assertNotNull(message.getReply());
                                Assert.assertEquals(ReplyType.Result.FAILED, message.getReply().getResult());
                                Assert.assertEquals(1, message.getReply().getError().size());
                                ErrorType failure = message.getReply().getError().get(0);
                                verifyFailure(failure);
                                Assert.assertEquals(CORRELATION_ID ,message.getHeader().getCorrelationID());

                                return null;
                            }
                        });

        provider.call(endPointConfiguration, operation, successfulLinkages, failedLinkages, expectedNumberOfCalls, CORRELATION_ID);
        SetMultimap<String,String> values = HashMultimap.create();
        successfulLinkages.forEach(link->{
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getMeterMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getMeterName());
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getEndDeviceMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getEndDeviceName());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), link.getUsagePointMrid());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), link.getUsagePointName());
        });

        failedLinkages.forEach(link->{
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getMeterMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getMeterName());
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getEndDeviceMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getEndDeviceName());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), link.getUsagePointMrid());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), link.getUsagePointName());
        });

        Mockito.verify(provider).using("closedMasterDataLinkageConfig");
        Mockito.verify(requestSender).toEndpoints(endPointConfiguration);
        Mockito.verify(requestSender).withRelatedAttributes(values);
        Mockito.verify(requestSender).send(Matchers.any(MasterDataLinkageConfigEventMessageType.class));
    }

    @Test
    public void testCallCloseForPartialSuccess() throws FaultMessage {
        String operation = "CLOSE";
        List<FailedLinkageOperation> failedLinkages = Arrays.asList(failedLinkage, failedLinkage, failedLinkage);
        List<LinkageOperation> successfulLinkages = Arrays.asList(successfulLinkage, successfulLinkage);
        BigDecimal expectedNumberOfCalls = new BigDecimal(failedLinkages.size() + successfulLinkages.size());
        Mockito.when(masterDataLinkageConfigPort
                .closedMasterDataLinkageConfig(Matchers.any(MasterDataLinkageConfigEventMessageType.class)))
                        .thenAnswer(new Answer<MasterDataLinkageConfigResponseMessageType>() {
                            @Override
                            public MasterDataLinkageConfigResponseMessageType answer(InvocationOnMock invocation)
                                    throws Throwable {
                                MasterDataLinkageConfigEventMessageType message = invocation.getArgumentAt(0,
                                        MasterDataLinkageConfigEventMessageType.class);
                                Assert.assertNotNull(message);
                                Assert.assertNotNull(message.getReply());
                                Assert.assertEquals(ReplyType.Result.PARTIAL, message.getReply().getResult());
                                Assert.assertEquals(failedLinkages.size(), message.getReply().getError().size());
                                for (ErrorType failure : message.getReply().getError()) {
                                    verifyFailure(failure);
                                }

                                MasterDataLinkageConfigPayloadType payload = message.getPayload();
                                Assert.assertNotNull(payload);
                                MasterDataLinkageConfig config = payload.getMasterDataLinkageConfig();
                                Assert.assertNotNull(config);
                                List<Meter> meters = config.getMeter();
                                List<EndDevice> endDevices = config.getEndDevice();
                                List<UsagePoint> usagePoints = config.getUsagePoint();
                                Assert.assertEquals(successfulLinkages.size(), meters.size());
                                Assert.assertEquals(successfulLinkages.size(), usagePoints.size());
                                for (int index = 0; index < successfulLinkages.size(); index++) {
                                    verifyLinkage(meters.get(index), usagePoints.get(index), endDevices.get(index));
                                }

                                Assert.assertEquals(CORRELATION_ID, message.getHeader().getCorrelationID());

                                return null;
                            }

                        });

        provider.call(endPointConfiguration, operation, successfulLinkages, failedLinkages, expectedNumberOfCalls, CORRELATION_ID);
        SetMultimap<String,String> values = HashMultimap.create();
        successfulLinkages.forEach(link->{
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getMeterMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getMeterName());
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getEndDeviceMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getEndDeviceName());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), link.getUsagePointMrid());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), link.getUsagePointName());
        });

        failedLinkages.forEach(link->{
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getMeterMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getMeterName());
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getEndDeviceMrid());
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getEndDeviceName());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), link.getUsagePointMrid());
            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), link.getUsagePointName());
        });

        Mockito.verify(provider).using("closedMasterDataLinkageConfig");
        Mockito.verify(requestSender).toEndpoints(endPointConfiguration);
        Mockito.verify(requestSender).withRelatedAttributes(values);
        Mockito.verify(requestSender).send(Matchers.any(MasterDataLinkageConfigEventMessageType.class));
    }

    private void verifyFailure(ErrorType failure) {
        Assert.assertEquals(ERROR_CODE, failure.getCode());
        Assert.assertEquals(ERROR_MESSAGE, failure.getDetails());
        ObjectType failureObject = failure.getObject();
        Assert.assertNotNull(failureObject);
        Assert.assertEquals(FAILURE_METER_MRID, failureObject.getMRID());
        List<Name> names = failureObject.getName();
        Assert.assertNotNull(names);
        Assert.assertEquals(1, names.size());
        Assert.assertEquals(FAILURE_METER_NAME, names.get(0).getName());
        Assert.assertEquals("Meter", failureObject.getObjectType());
    }

    private void verifyLinkage(Meter meter, UsagePoint usagePoint, EndDevice endDevice) {
        {
            Assert.assertNotNull(meter);
            Assert.assertEquals(SUCCESS_METER_MRID, meter.getMRID());
            List<ch.iec.tc57._2011.masterdatalinkageconfig.Name> names = meter.getNames();
            Assert.assertNotNull(names);
            Assert.assertEquals(1, names.size());
            Assert.assertEquals(SUCCESS_METER_NAME, names.get(0).getName());
        }
        {
            Assert.assertNotNull(endDevice);
            Assert.assertEquals(SUCCESS_END_DEVICE_MRID, endDevice.getMRID());
            List<ch.iec.tc57._2011.masterdatalinkageconfig.Name> names = endDevice.getNames();
            Assert.assertNotNull(names);
            Assert.assertEquals(1, names.size());
            Assert.assertEquals(SUCCESS_END_DEVICE_NAME, names.get(0).getName());
        }
        {
            Assert.assertNotNull(usagePoint);
            Assert.assertEquals(SUCCESS_USAGE_POINT_MRID, usagePoint.getMRID());
            List<ch.iec.tc57._2011.masterdatalinkageconfig.Name> names = usagePoint.getNames();
            Assert.assertNotNull(names);
            Assert.assertEquals(1, names.size());
            Assert.assertEquals(SUCCESS_USAGE_POINT_NAME, names.get(0).getName());
        }
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
