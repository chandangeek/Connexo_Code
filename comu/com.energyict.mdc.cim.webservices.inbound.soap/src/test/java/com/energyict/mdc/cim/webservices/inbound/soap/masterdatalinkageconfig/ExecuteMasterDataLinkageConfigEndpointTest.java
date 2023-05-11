/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigResponseMessageType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExecuteMasterDataLinkageConfigEndpointTest extends AbstractMasterDataLinkageTest {
    private ExecuteMasterDataLinkageConfigEndpoint endpoint;

    private static final String VIOLATION_EXCEPTION_MESSAGE = "VerboseConstraintViolationException error";
    private static final String LOCALIZED_EXCEPTION_MESSAGE = "LocalizedException error";
    private static final String LOCALIZED_EXCEPTION_CODE = "LocalizedException code";

    @Mock
    private MasterDataLinkageHandler linkageHandler;
    @Mock
    private MasterDataLinkageConfigResponseMessageType response;
    @Mock
    private VerboseConstraintViolationException violationException;
    @Mock
    private LocalizedException localizedException;

    private MasterDataLinkageConfigRequestMessageType message;
    @Mock
    private MasterDataLinkageMessageValidator validator;
    @Mock
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private WebServicesService webServicesService;
    @Mock
    private ServiceCallCommands serviceCallCommands;
    @Mock
    private WebServiceContext webServiceContext;
    @Mock
    private MessageContext messageContext;
    @Mock
    private WebServiceCallOccurrence webServiceCallOccurrence;

    private SetMultimap<String, String> values = HashMultimap.create();

    @Before
    public void setUp() throws Exception {
        endpoint = new ExecuteMasterDataLinkageConfigEndpoint(getInstance(MasterDataLinkageFaultMessageFactory.class),
                () -> linkageHandler, () -> validator, endPointConfigurationService,
                webServicesService, serviceCallCommands);

        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(endpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1L);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, endpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, endpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, endpoint, "webServiceCallOccurrenceService", webServiceCallOccurrenceService);
        inject(AbstractInboundEndPoint.class, endpoint, "transactionService", transactionService);
        when(webServiceCallOccurrenceService.getOngoingOccurrence(1L)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));
        message = getValidMessage().build();
        // common mocks
        when(linkageHandler.forMessage(any(MasterDataLinkageConfigRequestMessageType.class)))
                .thenReturn(linkageHandler);
        when(violationException.getLocalizedMessage()).thenReturn(VIOLATION_EXCEPTION_MESSAGE);
        when(localizedException.getErrorCode()).thenReturn(LOCALIZED_EXCEPTION_CODE);
        when(localizedException.getLocalizedMessage()).thenReturn(LOCALIZED_EXCEPTION_MESSAGE);

        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), "mtnm");
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), "mtmr");
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), "upnm");
        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), "upmr");
    }

    @Test
    public void testCreateMasterDataLinkageConfig() throws Exception {
        // Prepare
        when(linkageHandler.createLinkage()).thenReturn(response);
        message = getValidMessage()
                .eraseEndDeviceList()
                .build();

        // Act
        MasterDataLinkageConfigResponseMessageType actualResponse = endpoint.createMasterDataLinkageConfig(message);

        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        // Verify
        assertThat(actualResponse).isNotNull().isSameAs(response);
        verify(linkageHandler).forMessage(message);
    }

    @Test
    public void testCreateMasterDataLinkageConfigEndDevice() throws Exception {
        // Prepare
        when(linkageHandler.createLinkage()).thenReturn(response);
        message = getValidMessage()
                .withEndDeviceMRID("edmr")
                .withEndDeviceName("ednm")
                .build();
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), "ednm");
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), "edmr");

        // Act
        MasterDataLinkageConfigResponseMessageType actualResponse = endpoint.createMasterDataLinkageConfig(message);

        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        // Verify
        assertThat(actualResponse).isNotNull().isSameAs(response);
        verify(linkageHandler).forMessage(message);
    }

    @Test
    public void testCreateMasterDataLinkageConfig_verboseConstraintViolationException() throws Exception {
        // Prepare
        doThrow(violationException).when(linkageHandler).createLinkage();

        // Act and verify
        try {
            endpoint.createMasterDataLinkageConfig(message);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
            failNoException();
        } catch (FaultMessage e) {
            verify(linkageHandler).forMessage(message);
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, null, VIOLATION_EXCEPTION_MESSAGE);
        }
    }

    @Test
    public void testCreateMasterDataLinkageConfig_localizedException() throws Exception {
        // Prepare
        doThrow(localizedException).when(linkageHandler).createLinkage();

        // Act and verify
        try {
            endpoint.createMasterDataLinkageConfig(message);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
            failNoException();
        } catch (FaultMessage e) {
            verify(linkageHandler).forMessage(message);
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, LOCALIZED_EXCEPTION_CODE,
                    LOCALIZED_EXCEPTION_MESSAGE);
        }
    }

    @Test
    public void testCloseMasterDataLinkageConfig() throws Exception {
        // Prepare
        when(linkageHandler.closeLinkage()).thenReturn(response);
        message = getValidMessage()
                .eraseEndDeviceList()
                .build();

        // Act
        MasterDataLinkageConfigResponseMessageType actualResponse = endpoint.closeMasterDataLinkageConfig(message);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        // Verify
        assertThat(actualResponse).isNotNull().isSameAs(response);
        verify(linkageHandler).forMessage(message);
    }

    @Test
    public void testCloseMasterDataLinkageConfigEndDevice() throws Exception {
        // Prepare
        when(linkageHandler.closeLinkage()).thenReturn(response);
        message = getValidMessage()
                .withEndDeviceMRID("edmr")
                .withEndDeviceName("ednm")
                .build();
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), "ednm");
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), "edmr");

        // Act
        MasterDataLinkageConfigResponseMessageType actualResponse = endpoint.closeMasterDataLinkageConfig(message);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        // Verify
        assertThat(actualResponse).isNotNull().isSameAs(response);
        verify(linkageHandler).forMessage(message);
    }

    @Test
    public void testCloseMasterDataLinkageConfig_verboseConstraintViolationException() throws Exception {
        // Prepare
        doThrow(violationException).when(linkageHandler).closeLinkage();

        // Act and verify
        try {
            endpoint.closeMasterDataLinkageConfig(message);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);

            failNoException();
        } catch (FaultMessage e) {
            verify(linkageHandler).forMessage(message);
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, null, VIOLATION_EXCEPTION_MESSAGE);
        }
    }

    @Test
    public void testCloseMasterDataLinkageConfig_localizedException() throws Exception {
        // Prepare
        doThrow(localizedException).when(linkageHandler).closeLinkage();

        // Act and verify
        try {
            endpoint.closeMasterDataLinkageConfig(message);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);

            failNoException();
        } catch (FaultMessage e) {
            verify(linkageHandler).forMessage(message);
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, LOCALIZED_EXCEPTION_CODE,
                    LOCALIZED_EXCEPTION_MESSAGE);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testChangeMasterDataLinkageConfig() throws Exception {
        endpoint.changeMasterDataLinkageConfig(message);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCancelMasterDataLinkageConfig() throws Exception {
        endpoint.cancelMasterDataLinkageConfig(message);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDeleteMasterDataLinkageConfig() throws Exception {
        endpoint.deleteMasterDataLinkageConfig(message);
    }

}
