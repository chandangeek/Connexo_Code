/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.processes.keyrenewal.api.servicecall.ServiceCallCommands;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceResourceTest {

    private static final String DEVICE_MRID = "deviceMRID";
    private static final String INVALID_DEVICE_MRID = "invalidDeviceMRID";
    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    MeteringService meteringService;
    @Mock
    DeviceService deviceService;
    @Mock
    TransactionService transactionService;
    @Mock
    ServiceCallCommands serviceCallCommands;
    @Mock
    HeadEndController headEndController;
    @Mock
    UriInfo uriInfo;
    @Mock
    UsagePoint usagePoint;
    @Mock
    ServiceCall serviceCall;
    @Mock
    Thesaurus thesaurus;
    @Mock
    MeterActivation meterActivation;
    @Mock
    Meter meter;

    ExceptionFactory exceptionFactory;
    private DeviceResource deviceResource;

    @Before
    public void setUp() throws Exception {
        setUpThesaurus();
        exceptionFactory = new ExceptionFactory(thesaurus);
        deviceResource = new DeviceResource(deviceService, exceptionFactory, transactionService, serviceCallCommands, headEndController, meteringService);

        when(transactionService.getContext()).thenReturn(mock(TransactionContext.class));
        when(meteringService.findEndDeviceByMRID(INVALID_DEVICE_MRID)).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByMRID(DEVICE_MRID)).thenReturn(Optional.of(usagePoint));
        when(serviceCallCommands.createRenewKeyServiceCall(any(), any())).thenReturn(serviceCall);
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
    public void testInvalidDevice() throws Exception {
        // Business method
        Response response = deviceResource.renewKey(INVALID_DEVICE_MRID, new DeviceCommandInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.NO_SUCH_DEVICE.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }


    @Test
    public void testIncompleteDeviceCommandInfo() throws Exception {
        // Business method
        Response response = deviceResource.renewKey(DEVICE_MRID, new DeviceCommandInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.CALL_BACK_ERROR_URI_NOT_SPECIFIED.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }


}