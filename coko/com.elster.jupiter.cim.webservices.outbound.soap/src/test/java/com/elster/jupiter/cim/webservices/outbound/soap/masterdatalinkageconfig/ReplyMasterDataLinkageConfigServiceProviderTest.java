/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.outbound.soap.FailedLinkageOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.LinkageOperation;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

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
import com.google.common.collect.ImmutableMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ReplyMasterDataLinkageConfigServiceProvider provider;
    @Mock
    private EndPointConfiguration endPointConfiguration;
    @Mock
    private LinkageOperation successfulLinkage;
    @Mock
    private MasterDataLinkageConfigPort masterDataLinkageConfigPort;
    @Mock
    private FailedLinkageOperation failedLinkage;

    @Before
    public void setup() {
        provider = spy(new ReplyMasterDataLinkageConfigServiceProvider());
        String url = "some url";
        provider.addMasterDataLinkageConfigPort(masterDataLinkageConfigPort, ImmutableMap.of("url", url));

        when(endPointConfiguration.getUrl()).thenReturn(url);

        when(failedLinkage.getErrorCode()).thenReturn(ERROR_CODE);
        when(failedLinkage.getErrorMessage()).thenReturn(ERROR_MESSAGE);
        when(failedLinkage.getMeterMrid()).thenReturn(FAILURE_METER_MRID);
        when(failedLinkage.getMeterName()).thenReturn(FAILURE_METER_NAME);

        when(successfulLinkage.getMeterMrid()).thenReturn(SUCCESS_METER_MRID);
        when(successfulLinkage.getMeterName()).thenReturn(SUCCESS_METER_NAME);
        when(successfulLinkage.getUsagePointMrid()).thenReturn(SUCCESS_USAGE_POINT_MRID);
        when(successfulLinkage.getUsagePointName()).thenReturn(SUCCESS_USAGE_POINT_NAME);
    }

    @Test
    public void testCallCreateForSingleSuccess() throws FaultMessage {
        String operation = "CREATE";
        List<FailedLinkageOperation> failedLinkages = Collections.emptyList();
        List<LinkageOperation> successfulLinkages = Arrays.asList(successfulLinkage);
        BigDecimal expectedNumberOfCalls = BigDecimal.ONE;
        when(masterDataLinkageConfigPort
                .createdMasterDataLinkageConfig(any(MasterDataLinkageConfigEventMessageType.class)))
                        .thenAnswer(new Answer<MasterDataLinkageConfigResponseMessageType>() {
                            @Override
                            public MasterDataLinkageConfigResponseMessageType answer(InvocationOnMock invocation)
                                    throws Throwable {
                                MasterDataLinkageConfigEventMessageType message = invocation.getArgumentAt(0,
                                        MasterDataLinkageConfigEventMessageType.class);

                                StringWriter stringWriter = new StringWriter();

                                JAXBContext jaxbContext = JAXBContext.newInstance(MasterDataLinkageConfigEventMessageType.class);
                                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

                                // format the XML output
                                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                                        true);

                                QName qName = new QName("com.codenotfound.jaxb.model", "car");
                                JAXBElement<MasterDataLinkageConfigEventMessageType> root = new JAXBElement<>(qName, MasterDataLinkageConfigEventMessageType.class, message);

                                jaxbMarshaller.marshal(root, stringWriter);

                                String result = stringWriter.toString();

                                System.out.println("MESSAGE = "+result);



                                /* UNMARSHAAAAALLLLL */
                                JAXBContext jaxbContext1 = JAXBContext.newInstance(MasterDataLinkageConfigEventMessageType.class);
                                Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();

                                System.out.println("UNMURSHALLLL");
                                JAXBElement<MasterDataLinkageConfigEventMessageType> root1 =
                                                jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader( result )), MasterDataLinkageConfigEventMessageType.class);

                                MasterDataLinkageConfigEventMessageType msg = root1.getValue();



                                assertNotNull(message);
                                assertNotNull(message.getReply());
                                assertEquals(ReplyType.Result.OK, message.getReply().getResult());
                                assertEquals(0, message.getReply().getError().size());
                                MasterDataLinkageConfigPayloadType payload = message.getPayload();
                                assertNotNull(payload);
                                MasterDataLinkageConfig config = payload.getMasterDataLinkageConfig();
                                assertNotNull(config);
                                List<Meter> meters = config.getMeter();
                                List<UsagePoint> usagePoints = config.getUsagePoint();
                                assertEquals(1, meters.size());
                                assertEquals(1, usagePoints.size());
                                Meter meter = meters.get(0);
                                UsagePoint usagePoint = usagePoints.get(0);
                                verifyLinkage(meter, usagePoint);

                                return null;
                            }

                        });

        provider.call(endPointConfiguration, operation, successfulLinkages, failedLinkages, expectedNumberOfCalls);

        verify(masterDataLinkageConfigPort)
                .createdMasterDataLinkageConfig(any(MasterDataLinkageConfigEventMessageType.class));
    }

    @Test
    public void testCallCloseForSingleFailure() throws FaultMessage {
        String operation = "CLOSE";
        List<FailedLinkageOperation> failedLinkages = Arrays.asList(failedLinkage);
        List<LinkageOperation> successfulLinkages = Collections.emptyList();
        BigDecimal expectedNumberOfCalls = BigDecimal.ONE;
        when(masterDataLinkageConfigPort
                .closedMasterDataLinkageConfig(any(MasterDataLinkageConfigEventMessageType.class)))
                        .thenAnswer(new Answer<MasterDataLinkageConfigResponseMessageType>() {
                            @Override
                            public MasterDataLinkageConfigResponseMessageType answer(InvocationOnMock invocation)
                                    throws Throwable {
                                MasterDataLinkageConfigEventMessageType message = invocation.getArgumentAt(0,
                                        MasterDataLinkageConfigEventMessageType.class);
                                assertNotNull(message);
                                assertNotNull(message.getReply());
                                assertEquals(ReplyType.Result.FAILED, message.getReply().getResult());
                                assertEquals(1, message.getReply().getError().size());
                                ErrorType failure = message.getReply().getError().get(0);
                                verifyFailure(failure);
                                return null;
                            }
                        });

        provider.call(endPointConfiguration, operation, successfulLinkages, failedLinkages, expectedNumberOfCalls);

        verify(masterDataLinkageConfigPort)
                .closedMasterDataLinkageConfig(any(MasterDataLinkageConfigEventMessageType.class));
    }

    @Test
    public void testCallCloseForPartialSuccess() throws FaultMessage {
        String operation = "CLOSE";
        List<FailedLinkageOperation> failedLinkages = Arrays.asList(failedLinkage, failedLinkage, failedLinkage);
        List<LinkageOperation> successfulLinkages = Arrays.asList(successfulLinkage, successfulLinkage);
        BigDecimal expectedNumberOfCalls = new BigDecimal(failedLinkages.size() + successfulLinkages.size());
        when(masterDataLinkageConfigPort
                .closedMasterDataLinkageConfig(any(MasterDataLinkageConfigEventMessageType.class)))
                        .thenAnswer(new Answer<MasterDataLinkageConfigResponseMessageType>() {
                            @Override
                            public MasterDataLinkageConfigResponseMessageType answer(InvocationOnMock invocation)
                                    throws Throwable {
                                MasterDataLinkageConfigEventMessageType message = invocation.getArgumentAt(0,
                                        MasterDataLinkageConfigEventMessageType.class);
                                assertNotNull(message);
                                assertNotNull(message.getReply());
                                assertEquals(ReplyType.Result.PARTIAL, message.getReply().getResult());
                                assertEquals(failedLinkages.size(), message.getReply().getError().size());
                                for (ErrorType failure : message.getReply().getError()) {
                                    verifyFailure(failure);
                                }

                                MasterDataLinkageConfigPayloadType payload = message.getPayload();
                                assertNotNull(payload);
                                MasterDataLinkageConfig config = payload.getMasterDataLinkageConfig();
                                assertNotNull(config);
                                List<Meter> meters = config.getMeter();
                                List<UsagePoint> usagePoints = config.getUsagePoint();
                                assertEquals(successfulLinkages.size(), meters.size());
                                assertEquals(successfulLinkages.size(), usagePoints.size());
                                for (int index = 0; index < successfulLinkages.size(); index++) {
                                    verifyLinkage(meters.get(index), usagePoints.get(index));
                                }
                                return null;
                            }

                        });

        provider.call(endPointConfiguration, operation, successfulLinkages, failedLinkages, expectedNumberOfCalls);

        verify(masterDataLinkageConfigPort)
                .closedMasterDataLinkageConfig(any(MasterDataLinkageConfigEventMessageType.class));
    }

    private void verifyFailure(ErrorType failure) {
        assertEquals(ERROR_CODE, failure.getCode());
        assertEquals(ERROR_MESSAGE, failure.getDetails());
        ObjectType failureObject = failure.getObject();
        assertNotNull(failureObject);
        assertEquals(FAILURE_METER_MRID, failureObject.getMRID());
        List<Name> names = failureObject.getName();
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals(FAILURE_METER_NAME, names.get(0).getName());
        assertEquals("Meter", failureObject.getObjectType());
    }

    private void verifyLinkage(Meter meter, UsagePoint usagePoint) {
        {
            assertNotNull(meter);
            assertEquals(SUCCESS_METER_MRID, meter.getMRID());
            List<ch.iec.tc57._2011.masterdatalinkageconfig.Name> names = meter.getNames();
            assertNotNull(names);
            assertEquals(1, names.size());
            assertEquals(SUCCESS_METER_NAME, names.get(0).getName());
        }
        {

            assertNotNull(usagePoint);
            assertEquals(SUCCESS_USAGE_POINT_MRID, usagePoint.getMRID());
            List<ch.iec.tc57._2011.masterdatalinkageconfig.Name> names = usagePoint.getNames();
            assertNotNull(names);
            assertEquals(1, names.size());
            assertEquals(SUCCESS_USAGE_POINT_NAME, names.get(0).getName());
        }
    }

}
