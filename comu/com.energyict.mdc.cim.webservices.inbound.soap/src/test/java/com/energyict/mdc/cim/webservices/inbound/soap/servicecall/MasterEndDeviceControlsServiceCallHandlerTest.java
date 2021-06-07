package com.energyict.mdc.cim.webservices.inbound.soap.servicecall;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import com.energyict.mdc.common.device.data.Register;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallImpl;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.MasterEndDeviceControlsServiceCallHandler;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.NumericalReadingImpl;
import com.energyict.mdc.device.data.impl.NumericalRegisterImpl;
import com.energyict.obis.ObisCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

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

    @Before
    public void setUp() {
        testable = new MasterEndDeviceControlsServiceCallHandler(endPointConfigurationService, endDeviceEventsServiceProvider,
                serviceCallService, clock, thesaurus, replyTypeFactory);
    }

    @Test
    public void testCreateEndDeviceDetails_EmptyDevice() {
        when(serviceCall.getTargetObject()).thenReturn(Optional.empty());
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForCreditStatus(Optional.empty());
        Assert.assertEquals(false, endDeviceEventDetail.isPresent());
    }

    @Test
    public void testCreateEndDeviceDetails_WithEmptyRegisters() {
        when(((Optional<Device>) serviceCall.getTargetObject())).thenReturn(Optional.of(device));
        ArrayList<Register> registers = new ArrayList<>();
        when(device.getRegisters()).thenReturn(registers);
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForContactorStatus(Optional.of(device));
        Assert.assertEquals(false, endDeviceEventDetail.isPresent());
    }

    @Test
    public void testCreateEndDeviceDetails_Success() {
        when(register.getLastReading()).thenReturn(Optional.of(numericalReading));
        when(numericalReading.getValue()).thenReturn(BigDecimal.valueOf(0));
        when(register.getRegisterTypeObisCode()).thenReturn(ObisCode.fromString("0.0.96.3.10.255"));
        when(((Optional<Device>) serviceCall.getTargetObject())).thenReturn(Optional.of(device));
        ArrayList<Register> registers = new ArrayList<>();
        registers.add(register);
        when(device.getRegisters()).thenReturn(registers);
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForContactorStatus(Optional.of(device));
        Assert.assertEquals("Opened", endDeviceEventDetail.get().getValue());
    }

    @Test
    public void testCreateEndDeviceDetails_WrongObisCode() {
        when(register.getRegisterTypeObisCode()).thenReturn(ObisCode.fromString("0.0.96.3.10.300"));
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForContactorStatus(Optional.of(device));
        Assert.assertEquals(false, endDeviceEventDetail.isPresent());
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
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForContactorStatus(Optional.of(device));
        Assert.assertEquals(false, endDeviceEventDetail.isPresent());
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
        Optional<EndDeviceEventDetail> endDeviceEventDetail = testable.createEndDeviceDetailsForCreditStatus(Optional.of(device));
        Assert.assertEquals(false, endDeviceEventDetail.isPresent());
    }


}
