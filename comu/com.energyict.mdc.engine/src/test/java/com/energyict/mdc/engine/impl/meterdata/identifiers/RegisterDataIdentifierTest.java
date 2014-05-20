package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RegisterDataIdentifier} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 16/10/12
 * Time: 15:06
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterDataIdentifierTest {

    private static ObisCode registerObisCode = ObisCode.fromString("1.0.1.8.0.255");
    private static ObisCode registerDeviceObisCode = ObisCode.fromString("1.0.1.9.0.255");

    @Mock
    private BaseDevice device;

    private DeviceIdentifier getMockedDeviceIdentifier() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(this.device);
        return deviceIdentifier;
    }

    @Test
    public void testGetObisCode () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        RegisterDataIdentifier identifier = new RegisterDataIdentifier(registerObisCode, registerObisCode, deviceIdentifier);

        // Business method
        ObisCode obisCode = identifier.getObisCode();

        // Asserts
        assertThat(registerObisCode).isEqualTo(obisCode);
    }

    @Test
    public void testGetDeviceRegisterObisCode () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        RegisterDataIdentifier identifier = new RegisterDataIdentifier(registerObisCode, registerObisCode, deviceIdentifier);

        // Business method
        ObisCode obisCode = identifier.getDeviceRegisterObisCode();

        // Asserts
        assertThat(registerDeviceObisCode).isEqualTo(obisCode);
    }

    @Test
    public void testToStringPrintsDeviceIdentifier() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        ObisCode deviceRegisterObisCode = mock(ObisCode.class);
        RegisterDataIdentifier identifier = new RegisterDataIdentifier(registerObisCode, deviceRegisterObisCode, deviceIdentifier);

        // Business method
        identifier.toString();

        // Asserts
        verify(deviceRegisterObisCode).toString();
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
    public void singleRegisterBasedOnRegisterMappingTest() {
        RegisterMapping mapping = mock(RegisterMapping.class);
        when(mapping.getObisCode()).thenReturn(registerObisCode);
        RegisterSpec spec = mock(RegisterSpec.class);
        Register register = mock(Register.class);
        when(register.getRegisterMappingObisCode()).thenReturn(registerObisCode);
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
        RegisterMapping mapping = mock(RegisterMapping.class);
        when(mapping.getObisCode()).thenReturn(registerObisCode);
        RegisterSpec spec = mock(RegisterSpec.class);
        when(spec.getDeviceObisCode()).thenReturn(overruledObisCode);
        Register register = mock(Register.class);
        when(register.getRegisterMappingObisCode()).thenReturn(registerObisCode);
        when(register.getDeviceObisCode()).thenReturn(overruledObisCode);
        when(register.getRegisterSpec()).thenReturn(spec);
        when(device.getRegisters()).thenReturn(Arrays.asList(register));

        // Business method
        BaseRegister foundRegister = new RegisterDataIdentifier(overruledObisCode, registerObisCode, getMockedDeviceIdentifier()).findRegister();

        // asserts
        assertThat(foundRegister).isNotNull();
        assertThat(foundRegister).isEqualTo(register);
    }

    @Test
    public void multipleRegistersTest() {
        final ObisCode overruledObisCode = ObisCode.fromString("1.0.2.8.0.255");
        RegisterMapping mapping = mock(RegisterMapping.class);
        when(mapping.getObisCode()).thenReturn(registerObisCode);
        RegisterSpec spec = mock(RegisterSpec.class);
        when(spec.getDeviceObisCode()).thenReturn(overruledObisCode);
        Register register = mock(Register.class);
        when(register.getRegisterMappingObisCode()).thenReturn(registerObisCode);
        when(register.getRegisterSpec()).thenReturn(spec);

        RegisterMapping mapping2 = mock(RegisterMapping.class);
        when(mapping2.getObisCode()).thenReturn(registerObisCode);
        RegisterSpec spec2 = mock(RegisterSpec.class);
        Register register2 = mock(Register.class);
        when(register2.getRegisterMappingObisCode()).thenReturn(registerObisCode);
        when(register2.getRegisterSpec()).thenReturn(spec2);

        when(device.getRegisters()).thenReturn(Arrays.asList(register, register2));

        // Business method
        BaseRegister foundRegister = new RegisterDataIdentifier(overruledObisCode, registerObisCode, getMockedDeviceIdentifier()).findRegister();

        // asserts
        assertThat(foundRegister).isNotNull();
        assertThat(foundRegister).isEqualTo(register);
    }

}