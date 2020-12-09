/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.ServiceCallCommands;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceResourceTest {

    private static final String DEVICE_MRID = "deviceMRID";
    private static final String INVALID_DEVICE_MRID = "invalidDeviceMRID";
    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    private MeteringService meteringService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ServiceCallCommands serviceCallCommands;
    @Mock
    private HeadEndController headEndController;
    @Mock
    private JsonService jsonService;
    @Mock
    private SecurityManagementService securityManagementService;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private MessageService messageService;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private Device device;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Meter endDevice;

    private ExceptionFactory exceptionFactory;
    private DeviceResource deviceResource;

    @Before
    public void setUp() throws Exception {
        setUpThesaurus();
        exceptionFactory = new ExceptionFactory(thesaurus);
        deviceResource = new DeviceResource(deviceService, exceptionFactory, transactionService, serviceCallCommands, headEndController, meteringService, securityManagementService, serviceCallService, messageService, jsonService);
        when(transactionService.getContext()).thenReturn(mock(TransactionContext.class));
        when(serviceCallCommands.createRenewKeyServiceCall(any(), any())).thenReturn(serviceCall);
        when(serviceCall.canTransitionTo(DefaultState.WAITING)).thenReturn(true);
    }

    private void setUpThesaurus() {
        when(thesaurus.getFormat(any(TranslationKey.class))).thenAnswer(invocationOnMock -> {
            TranslationKey translationKey = (TranslationKey) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }
            };
        });
        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocationOnMock -> {
            MessageSeed messageSeed = (MessageSeed) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }
            };
        });
    }


    @Test
    public void testIncompleteDeviceCommandInfoRenew() throws Exception {
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        // Business method
        Response response = deviceResource.renewKey(DEVICE_MRID, new DeviceCommandInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, com.energyict.mdc.processes.keyrenewal.api.impl.MessageSeeds.CALL_BACK_ERROR_URI_NOT_SPECIFIED.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testIncompleteDeviceCommandInfoNoSuccessUriRenew() throws Exception {
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();
        deviceCommandInfo.callbackError = "success";
        // Business method
        Response response = deviceResource.renewKey(DEVICE_MRID, deviceCommandInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, com.energyict.mdc.processes.keyrenewal.api.impl.MessageSeeds.CALL_BACK_SUCCESS_URI_NOT_SPECIFIED.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testIncompleteDeviceCommandInfoTestCom() throws Exception {
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        // Business method
        Response response = deviceResource.testCommunication(DEVICE_MRID, new DeviceCommandInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, com.energyict.mdc.processes.keyrenewal.api.impl.MessageSeeds.CALL_BACK_ERROR_URI_NOT_SPECIFIED.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testIncompleteDeviceCommandInfoNoSuccessUriTestCom() throws Exception {
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();
        deviceCommandInfo.callbackError = "success";
        // Business method
        Response response = deviceResource.testCommunication(DEVICE_MRID, deviceCommandInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, com.energyict.mdc.processes.keyrenewal.api.impl.MessageSeeds.CALL_BACK_SUCCESS_URI_NOT_SPECIFIED.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testInvalidDeviceRenew() throws Exception {
        when(deviceService.findDeviceByMrid(INVALID_DEVICE_MRID)).thenReturn(Optional.empty());
        // Business method
        Response response = deviceResource.renewKey(INVALID_DEVICE_MRID, new DeviceCommandInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.NO_SUCH_DEVICE.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testInvalidEndDeviceRenew() throws Exception {
        when(deviceService.findDeviceByMrid(INVALID_DEVICE_MRID)).thenReturn(Optional.empty());
        // Business method
        Response response = deviceResource.renewKey(INVALID_DEVICE_MRID, new DeviceCommandInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).rejectServiceCall(serviceCall, com.energyict.mdc.processes.keyrenewal.api.impl.MessageSeeds.NO_SUCH_DEVICE.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testInvalidDeviceTestCom() throws Exception {
        when(deviceService.findDeviceByMrid(INVALID_DEVICE_MRID)).thenReturn(Optional.empty());
        // Business method
        Response response = deviceResource.testCommunication(INVALID_DEVICE_MRID, new DeviceCommandInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).rejectServiceCall(serviceCall, com.energyict.mdc.processes.keyrenewal.api.impl.MessageSeeds.NO_SUCH_DEVICE.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testInvalidEndDeviceTestCom() throws Exception {
        when(deviceService.findDeviceByMrid(INVALID_DEVICE_MRID)).thenReturn(Optional.empty());
        // Business method
        Response response = deviceResource.testCommunication(INVALID_DEVICE_MRID, new DeviceCommandInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).rejectServiceCall(serviceCall, com.energyict.mdc.processes.keyrenewal.api.impl.MessageSeeds.NO_SUCH_DEVICE.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testRenewKey() throws Exception {
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        when(device.getMeter()).thenReturn(endDevice);
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();
        deviceCommandInfo.callbackError = "errorURL";
        deviceCommandInfo.command = Command.RENEW_KEY;
        deviceCommandInfo.callbackSuccess = "successURL";
        deviceCommandInfo.keyAccessorType = "AK";
        when(serviceCall.getState()).thenReturn(DefaultState.CREATED);
        // Business method
        Response response = deviceResource.renewKey(DEVICE_MRID, deviceCommandInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.WAITING);
        verify(headEndController).performOperations(serviceCall, deviceCommandInfo, device);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCommunicationTest() throws Exception {
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();
        deviceCommandInfo.callbackError = "errorURL";
        deviceCommandInfo.command = Command.RENEW_KEY;
        deviceCommandInfo.callbackSuccess = "successURL";
        deviceCommandInfo.keyAccessorType = "AK";

        // Business method
        Response response = deviceResource.testCommunication(DEVICE_MRID, deviceCommandInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.WAITING);
        verify(headEndController).performTestCommunication(serviceCall, deviceCommandInfo, device);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCommunicationForSecuritySetTest() throws Exception {
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();
        deviceCommandInfo.callbackError = "errorURL";
        deviceCommandInfo.command = Command.RENEW_KEY;
        deviceCommandInfo.callbackSuccess = "successURL";
        deviceCommandInfo.securityPropertySet = "SET";

        // Business method
        Response response = deviceResource.testCommunicationForSecuritySet(DEVICE_MRID, deviceCommandInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.WAITING);
        verify(headEndController).performTestCommunicationForSecuritySet(serviceCall, deviceCommandInfo, device);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
    }
}
