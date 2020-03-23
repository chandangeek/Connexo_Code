/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.prepayment.impl.servicecall.ServiceCallCommands;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

/**
 * @author sva
 * @since 16/06/2016 - 16:00
 */
@RunWith(MockitoJUnitRunner.class)
public class UsagePointResourceTest {

    private static final String USAGE_POINT_MRID = "usagePointMRID";
    private static final String INVALID_USAGE_POINT_MRID = "invalidUsagePointMRID";
    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    MeteringService meteringService;
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
    private UsagePointResource usagePointResource;

    @Before
    public void setUp() throws Exception {
        setUpThesaurus();
        exceptionFactory = new ExceptionFactory(thesaurus);
        usagePointResource = new UsagePointResource(meteringService, exceptionFactory, transactionService, serviceCallCommands, headEndController);

        when(transactionService.getContext()).thenReturn(mock(TransactionContext.class));
        when(meteringService.findUsagePointByMRID(INVALID_USAGE_POINT_MRID)).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(serviceCallCommands.createContactorOperationServiceCall(any(), any())).thenReturn(serviceCall);
        when(usagePoint.getCurrentMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
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
    public void testInvalidUsagePoint() throws Exception {
        // Business method
        Response response = usagePointResource.updateContactor(INVALID_USAGE_POINT_MRID, new ContactorInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.NO_SUCH_USAGE_POINT.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testNoCurrentMeterActivation() throws Exception {
        when(usagePoint.getCurrentMeterActivations()).thenReturn(Collections.emptyList());

        // Business method
        Response response = usagePointResource.updateContactor(USAGE_POINT_MRID, new ContactorInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.NO_CURRENT_METER_ACTIVATION.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testNoMeterInActivation() throws Exception {
        when(meterActivation.getMeter()).thenReturn(Optional.empty());

        // Business method
        Response response = usagePointResource.updateContactor(USAGE_POINT_MRID, new ContactorInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.NO_METER_IN_ACTIVATION.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testIncompleteContactorInfo() throws Exception {
        // Business method
        Response response = usagePointResource.updateContactor(USAGE_POINT_MRID, new ContactorInfo(), uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.INCOMPLETE_CONTACTOR_INFO.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testLoadToleranceWithoutLoadLimitContactorInfo() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.status = BreakerStatus.connected;
        contactorInfo.loadTolerance = 30;

        // Business method
        Response response = usagePointResource.updateContactor(USAGE_POINT_MRID, contactorInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.TOLERANCE_WITHOUT_LOAD_LIMIT.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testContactorInfoWithLoadLimitValueMissing() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.loadLimit = contactorInfo.new LoadLimit();

        // Business method
        Response response = usagePointResource.updateContactor(USAGE_POINT_MRID, contactorInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.INCOMPLETE_LOADLIMIT.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testContactorInfoWithLoadLimitUnitMissing() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.loadLimit = contactorInfo.new LoadLimit(BigDecimal.ONE, null);

        // Business method
        Response response = usagePointResource.updateContactor(USAGE_POINT_MRID, contactorInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.INCOMPLETE_LOADLIMIT.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testContactorInfoWithUnknownUnitCode() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.loadLimit = contactorInfo.new LoadLimit(BigDecimal.ONE, "unknownUnit");

        // Business method
        Response response = usagePointResource.updateContactor(USAGE_POINT_MRID, contactorInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).rejectServiceCall(serviceCall, MessageSeeds.UNKNOWN_UNIT_CODE.getDefaultFormat());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testContactorInfoWithDisabelLoadLimit() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.loadLimit = contactorInfo.new LoadLimit(BigDecimal.ZERO, null);   // Should not complain about missing unit (as the load limit should be disabled in this case)

        // Business method
        Response response = usagePointResource.updateContactor(USAGE_POINT_MRID, contactorInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.WAITING);
        verify(headEndController).performContactorOperations(meter, serviceCall, contactorInfo);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testMultipleMeterActivations() throws Exception {
        MeterActivation otherMeterActivation = mock(MeterActivation.class);
        Meter otherMeter = mock(Meter.class);
        when(otherMeterActivation.getMeter()).thenReturn(Optional.of(otherMeter));
        List<MeterActivation> meterActivations = new ArrayList<>();
        meterActivations.add(meterActivation);
        meterActivations.add(otherMeterActivation);
        when(usagePoint.getCurrentMeterActivations()).thenReturn(meterActivations);


        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.loadLimit = contactorInfo.new LoadLimit(BigDecimal.ZERO, null);

        // Business method
        Response response = usagePointResource.updateContactor(USAGE_POINT_MRID, contactorInfo, uriInfo);

        // Asserts
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.PENDING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.ONGOING);
        verify(serviceCallCommands).requestTransition(serviceCall, DefaultState.WAITING);
        verify(headEndController).performContactorOperations(meter, serviceCall, contactorInfo);
        verify(headEndController).performContactorOperations(otherMeter, serviceCall, contactorInfo);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
    }
}