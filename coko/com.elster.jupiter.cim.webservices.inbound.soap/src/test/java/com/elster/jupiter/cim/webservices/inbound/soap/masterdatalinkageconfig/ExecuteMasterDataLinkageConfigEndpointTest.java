/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigResponseMessageType;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
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
    private EndPointHelper endPointHelper;
    @Mock
    private MasterDataLinkageConfigResponseMessageType response;
    @Mock
    private VerboseConstraintViolationException violationException;
    @Mock
    private LocalizedException localizedException;
    @Mock
    private MasterDataLinkageConfigRequestMessageType message;
    @Mock
    private MasterDataLinkageMessageValidator validator;
    @Mock
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private WebServicesService webServicesService;

    @Before
    public void setUp() throws Exception {
        endpoint = new ExecuteMasterDataLinkageConfigEndpoint(
                getInstance(MasterDataLinkageFaultMessageFactory.class),
                transactionService,
                endPointHelper,
                () -> linkageHandler,
                () -> validator, endPointConfigurationService, webServicesService);


        //common mocks
        when(transactionService.getContext()).thenReturn(transactionContext);
        when(linkageHandler.forMessage(message)).thenReturn(linkageHandler);
        when(violationException.getLocalizedMessage()).thenReturn(VIOLATION_EXCEPTION_MESSAGE);
        when(localizedException.getErrorCode()).thenReturn(LOCALIZED_EXCEPTION_CODE);
        when(localizedException.getLocalizedMessage()).thenReturn(LOCALIZED_EXCEPTION_MESSAGE);
    }

    @Test
    public void testCreateMasterDataLinkageConfig() throws Exception {
        //Prepare
        when(linkageHandler.createLinkage()).thenReturn(response);

        //Act
        MasterDataLinkageConfigResponseMessageType actualResponse = endpoint.createMasterDataLinkageConfig(message);

        //Verify
        assertThat(actualResponse).isNotNull().isSameAs(response);
        verify(linkageHandler).forMessage(message);
        verify(endPointHelper, times(1)).setSecurityContext();
        verify(transactionContext, times(1)).commit();
        verify(transactionContext, times(1)).close();
    }

    @Test
    public void testCreateMasterDataLinkageConfig_verboseConstraintViolationException() throws Exception {
        //Prepare
        doThrow(violationException).when(linkageHandler).createLinkage();

        //Act and verify
        try {
            endpoint.createMasterDataLinkageConfig(message);
            failNoException();
        } catch (FaultMessage e) {
            verify(linkageHandler).forMessage(message);
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, null, VIOLATION_EXCEPTION_MESSAGE);
        }
    }

    @Test
    public void testCreateMasterDataLinkageConfig_localizedException() throws Exception {
        //Prepare
        doThrow(localizedException).when(linkageHandler).createLinkage();

        //Act and verify
        try {
            endpoint.createMasterDataLinkageConfig(message);
            failNoException();
        } catch (FaultMessage e) {
            verify(linkageHandler).forMessage(message);
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, LOCALIZED_EXCEPTION_CODE, LOCALIZED_EXCEPTION_MESSAGE);
        }
    }

    @Test
    public void testCloseMasterDataLinkageConfig() throws Exception {
        //Prepare
        when(linkageHandler.closeLinkage()).thenReturn(response);

        //Act
        MasterDataLinkageConfigResponseMessageType actualResponse = endpoint.closeMasterDataLinkageConfig(message);

        //Verify
        assertThat(actualResponse).isNotNull().isSameAs(response);
        verify(linkageHandler).forMessage(message);
        verify(endPointHelper, times(1)).setSecurityContext();
        verify(transactionContext, times(1)).commit();
        verify(transactionContext, times(1)).close();
    }

    @Test
    public void testCloseMasterDataLinkageConfig_verboseConstraintViolationException() throws Exception {
        //Prepare
        doThrow(violationException).when(linkageHandler).closeLinkage();

        //Act and verify
        try {
            endpoint.closeMasterDataLinkageConfig(message);
            failNoException();
        } catch (FaultMessage e) {
            verify(linkageHandler).forMessage(message);
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, null, VIOLATION_EXCEPTION_MESSAGE);
        }
    }

    @Test
    public void testCloseMasterDataLinkageConfig_localizedException() throws Exception {
        //Prepare
        doThrow(localizedException).when(linkageHandler).closeLinkage();

        //Act and verify
        try {
            endpoint.closeMasterDataLinkageConfig(message);
            failNoException();
        } catch (FaultMessage e) {
            verify(linkageHandler).forMessage(message);
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, LOCALIZED_EXCEPTION_CODE, LOCALIZED_EXCEPTION_MESSAGE);
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
