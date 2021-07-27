/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import com.energyict.mdc.common.device.data.Register;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.MasterEndDeviceControlsServiceCallHandler;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.TextReading;
import com.energyict.obis.ObisCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MasterEndDeviceControlsServiceCallHandlerTest {
    private MasterEndDeviceControlsServiceCallHandler testable;
    @Mock
    private ServiceCall serviceCall;

    @Mock
    private EndPointConfigurationService endPointConfigurationService;

    @Mock
    private ServiceCallService serviceCallService;

    @Mock
    private Clock clock;

    @Mock
    private Thesaurus thesaurus;

    @Mock
    private ReplyTypeFactory replyTypeFactory;

    @Mock
    private Device device;

    @Mock
    private EndDeviceEventsServiceProvider endDeviceEventsServiceProvider;
    @Mock
    private Register register;

    @Mock
    private NumericalReading numericalReading;

    @Mock
    private TextReading textReading;


    @Before
    public void setUp() {
        testable = new MasterEndDeviceControlsServiceCallHandler(endPointConfigurationService, endDeviceEventsServiceProvider,
                serviceCallService, clock, thesaurus, replyTypeFactory);
    }

    @Test
    public void testCreateEndDeviceDetails_WithEmptyRegisters() {
        when(((Optional<Device>) serviceCall.getTargetObject())).thenReturn(Optional.of(device));
        ArrayList<Register> registers = new ArrayList<>();
        when(device.getRegisters()).thenReturn(registers);
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForContactorStatus(serviceCall);
        assertThat(endDeviceEventDetail.isPresent()).isFalse();
    }

    @Test
    public void testCreateEndDeviceDetails_Success() {
        when(register.getLastReading()).thenReturn(Optional.of(textReading));
        when(textReading.getValue()).thenReturn("Disconnected");
        when(register.getRegisterTypeObisCode()).thenReturn(ObisCode.fromString("0.0.96.3.10.255"));
        when(((Optional<Device>) serviceCall.getTargetObject())).thenReturn(Optional.of(device));
        ArrayList<Register> registers = new ArrayList<>();
        registers.add(register);
        when(device.getRegisters()).thenReturn(registers);
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForContactorStatus(serviceCall);
        assertThat(endDeviceEventDetail.get().getValue()).isEqualTo("Opened");
    }

    @Test
    public void testCreateEndDeviceDetails_WrongObisCode() {
        when(((Optional<Device>) serviceCall.getTargetObject())).thenReturn(Optional.of(device));
        when(register.getRegisterTypeObisCode()).thenReturn(ObisCode.fromString("0.0.96.3.10.300"));
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForContactorStatus(serviceCall);
        assertThat(endDeviceEventDetail.isPresent()).isFalse();
    }

    @Test
    public void testCreateEndDeviceDetailsBreakerStatus_WithoutLastReading() {
        when(register.getLastReading()).thenReturn(Optional.empty());
        when(numericalReading.getValue()).thenReturn(BigDecimal.valueOf(0));
        when(register.getRegisterTypeObisCode()).thenReturn(ObisCode.fromString("0.0.96.3.10.255"));
        when(((Optional<Device>) serviceCall.getTargetObject())).thenReturn(Optional.of(device));
        ArrayList<Register> registers = new ArrayList<>();
        registers.add(register);
        when(device.getRegisters()).thenReturn(registers);
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForContactorStatus(serviceCall);
        assertThat(endDeviceEventDetail.isPresent()).isFalse();
    }

    @Test
    public void testCreateEndDeviceDetailsCreditUpdate_WithoutLastReading() {
        when(register.getLastReading()).thenReturn(Optional.empty());
        when(numericalReading.getValue()).thenReturn(BigDecimal.valueOf(0));
        when(register.getRegisterTypeObisCode()).thenReturn(ObisCode.fromString("0.0.19.10.0.255"));
        when(((Optional<Device>) serviceCall.getTargetObject())).thenReturn(Optional.of(device));
        ArrayList<Register> registers = new ArrayList<>();
        registers.add(register);
        when(device.getRegisters()).thenReturn(registers);
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForCreditStatus(serviceCall);
        assertThat(endDeviceEventDetail.isPresent()).isFalse();
    }
}
