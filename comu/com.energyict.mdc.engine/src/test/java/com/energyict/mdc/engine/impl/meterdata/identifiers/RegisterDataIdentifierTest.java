/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegisterDataIdentifierTest {

    private static ObisCode registerObisCode = ObisCode.fromString("1.0.1.8.0.255");
    private static ObisCode registerDeviceObisCode = ObisCode.fromString("1.0.1.9.0.255");

    @Mock
    private Device device;

    private DeviceIdentifier<Device> getMockedDeviceIdentifier() {
        DeviceIdentifier<Device> deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(this.device);
        return deviceIdentifier;
    }

    @Test
    public void testGetObisCode () {
        DeviceIdentifier<Device> deviceIdentifier = mock(DeviceIdentifier.class);
        RegisterDataIdentifier identifier = new RegisterDataIdentifier(registerObisCode, registerObisCode, deviceIdentifier);

        // Business method
        ObisCode obisCode = identifier.getObisCode();

        // Asserts
        assertThat(registerObisCode).isEqualTo(obisCode);
    }

    @Test
    public void testGetDeviceRegisterObisCode () {
        DeviceIdentifier<Device> deviceIdentifier = mock(DeviceIdentifier.class);
        RegisterDataIdentifier identifier = new RegisterDataIdentifier(registerObisCode, registerDeviceObisCode, deviceIdentifier);

        // Business method
        ObisCode obisCode = identifier.getDeviceRegisterObisCode();

        // Asserts
        assertThat(registerDeviceObisCode).isEqualTo(obisCode);
    }

    @Test
    public void registerDoesNotExistTest() {
        when(this.device.getRegisters()).thenReturn(new ArrayList<Register>(0));

        // business method
        BaseRegister register = new RegisterDataIdentifier(registerObisCode, registerObisCode, getMockedDeviceIdentifier()).findRegister();

        // Asserts
        assertThat(register).isNull();
    }


    @Test
    public void deviceDoesNotExist() {
        final DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(null);

        // Business method
        BaseRegister register = new RegisterDataIdentifier(registerObisCode, registerObisCode, getMockedDeviceIdentifier()).findRegister();
        // should not fail or get NULLPOINTEREXCEPTIONS

        // Asserts
        assertThat(register).isNull();
    }

    @Test
    public void singleRegisterBasedOnRegisterTypeTest() {
        MeasurementType mapping = mock(MeasurementType.class);
        when(mapping.getObisCode()).thenReturn(registerObisCode);
        RegisterSpec spec = mock(RegisterSpec.class);
        Register register = mock(Register.class);
        when(register.getRegisterTypeObisCode()).thenReturn(registerObisCode);
        when(register.getRegisterSpec()).thenReturn(spec);
        when(device.getRegisters()).thenReturn(Arrays.asList(register));

        // Business method
        BaseRegister foundRegister = new RegisterDataIdentifier(registerObisCode, registerObisCode, getMockedDeviceIdentifier()).findRegister();

        // asserts
        assertThat(foundRegister).isNotNull();
        assertThat(foundRegister).isEqualTo(register);
    }

    @Test
    public void singleRegisterBasedOnOverruledObisCodeTest() {
        final ObisCode overruledObisCode = ObisCode.fromString("1.0.2.8.0.255");
        MeasurementType mapping = mock(MeasurementType.class);
        when(mapping.getObisCode()).thenReturn(registerObisCode);
        RegisterSpec spec = mock(RegisterSpec.class);
        when(spec.getDeviceObisCode()).thenReturn(overruledObisCode);
        Register register = mock(Register.class);
        when(register.getRegisterTypeObisCode()).thenReturn(registerObisCode);
        when(register.getDeviceObisCode()).thenReturn(overruledObisCode);
        when(register.getRegisterSpec()).thenReturn(spec);
        when(device.getRegisters()).thenReturn(Arrays.asList(register));

        // Business method
        Register foundRegister = new RegisterDataIdentifier(registerObisCode, overruledObisCode, getMockedDeviceIdentifier()).findRegister();

        // asserts
        assertThat(foundRegister).isNotNull();
        assertThat(foundRegister).isEqualTo(register);
    }

    @Test
    public void multipleRegistersTest() {
        final ObisCode overruledObisCode = ObisCode.fromString("1.0.2.8.0.255");
        MeasurementType mapping = mock(MeasurementType.class);
        when(mapping.getObisCode()).thenReturn(registerObisCode);
        RegisterSpec spec = mock(RegisterSpec.class);
        when(spec.getDeviceObisCode()).thenReturn(overruledObisCode);
        Register register = mock(Register.class);
        when(register.getDeviceObisCode()).thenReturn(registerObisCode);
        when(register.getRegisterSpec()).thenReturn(spec);

        MeasurementType mapping2 = mock(MeasurementType.class);
        when(mapping2.getObisCode()).thenReturn(registerObisCode);
        RegisterSpec spec2 = mock(RegisterSpec.class);
        Register register2 = mock(Register.class);
        when(register2.getDeviceObisCode()).thenReturn(registerObisCode);
        when(register2.getRegisterSpec()).thenReturn(spec2);

        when(device.getRegisters()).thenReturn(Arrays.asList(register, register2));

        // Business method
        Register foundRegister = new RegisterDataIdentifier(overruledObisCode, registerObisCode, getMockedDeviceIdentifier()).findRegister();

        // asserts
        assertThat(foundRegister).isNotNull();
        assertThat(foundRegister).isEqualTo(register);
    }

}