/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.task;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.EndDeviceControlsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.MasterEndDeviceControlsDomainExtension;
import com.energyict.mdc.device.data.ami.ICommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceControlsCancellationTest {
    private static final Instant PAST_DATE = ZonedDateTime.of(2020, 6, 22, 9, 0, 0, 0,
            TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final Instant NOW_DATE = ZonedDateTime.of(2020, 6, 22, 9, 35, 0, 0,
            TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final Instant FUTURE_DATE = ZonedDateTime.of(2020, 6, 22, 10, 10, 0, 0,
            TimeZoneNeutral.getMcMurdo()).toInstant();

    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private ServiceCall edcServiceCall, headEndServiceCall, masterServiceCall, subMasterServiceCall;
    @Mock
    private EndDeviceControlsDomainExtension edcDomainExtension;
    @Mock
    private Clock clock;
    @Mock
    private Finder<ServiceCall> serviceCallFinder, childCallFinder;
    @Mock
    private ICommandServiceCallDomainExtension headEndDomainExtension;
    @Mock
    private MasterEndDeviceControlsDomainExtension masterDomainExtension;
    @Mock
    private MultiSenseHeadEndInterface multiSenseHeadEndInterface;

    private EndDeviceControlsCancellationHandler endDeviceControlsCancellationHandler;

    @Before
    public void setUp() {
        when(serviceCallService.getServiceCallFinder(any(ServiceCallFilter.class))).thenReturn(serviceCallFinder);
        when(serviceCallFinder.find()).thenReturn(Collections.singletonList(edcServiceCall));
        when(serviceCallFinder.stream()).then((i) -> Stream.of(edcServiceCall));
        when(edcServiceCall.getExtension(EndDeviceControlsDomainExtension.class)).thenReturn(Optional.of(edcDomainExtension));
        when(edcServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(edcServiceCall.getId()).thenReturn(1001L);
        when(serviceCallService.lockServiceCall(1001L)).thenReturn(Optional.of(edcServiceCall));

        when(childCallFinder.stream()).then((i) -> Stream.of(headEndServiceCall));
        when(edcServiceCall.findChildren()).thenReturn(childCallFinder);
        when(edcServiceCall.findChildren().paged(0, 0)).thenReturn(childCallFinder);
        when(edcServiceCall.findChildren().paged(0, 0).find()).thenReturn(Collections.singletonList(headEndServiceCall));
        doReturn(Optional.of(headEndDomainExtension)).when(multiSenseHeadEndInterface).getCommandServiceCallDomainExtension(headEndServiceCall);
        when(edcServiceCall.getState()).thenReturn(DefaultState.WAITING);

        when(edcServiceCall.getParent()).thenReturn(Optional.of(subMasterServiceCall));
        when(subMasterServiceCall.getParent()).thenReturn(Optional.of(masterServiceCall));
        when(masterServiceCall.getExtension(MasterEndDeviceControlsDomainExtension.class)).thenReturn(Optional.of(masterDomainExtension));

        when(clock.instant()).thenReturn(NOW_DATE);

        endDeviceControlsCancellationHandler = new EndDeviceControlsCancellationHandler(serviceCallService, clock, multiSenseHeadEndInterface);
    }

    @Test
    public void cancelEndDeviceControlByTimeout() {
        when(headEndDomainExtension.getReleaseDate()).thenReturn(PAST_DATE);

        endDeviceControlsCancellationHandler.execute(null);

        verify(edcServiceCall).requestTransition(DefaultState.CANCELLED);
    }

    @Test
    public void noCancelEndDeviceControlByTimeout() {
        when(headEndDomainExtension.getReleaseDate()).thenReturn(FUTURE_DATE);

        endDeviceControlsCancellationHandler.execute(null);

        verify(edcServiceCall, never()).requestTransition(DefaultState.CANCELLED);
    }

    @Test
    public void noDomainExtensionFailTest() {
        when(masterServiceCall.getExtension(MasterEndDeviceControlsDomainExtension.class)).thenReturn(Optional.empty());
        try {
            endDeviceControlsCancellationHandler.execute(null);
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assert (e.getMessage().equals("Unable to get domain extension for master service call"));
        }
    }
}
