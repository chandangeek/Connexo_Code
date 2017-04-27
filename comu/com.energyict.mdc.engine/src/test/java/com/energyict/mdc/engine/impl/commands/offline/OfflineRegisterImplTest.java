/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDeviceBySerialNumber;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for the {@link OfflineRegisterImpl} component
 *
 * @author gna
 * @since 14/06/12 - 10:33
 */
@RunWith(MockitoJUnitRunner.class)
public class OfflineRegisterImplTest {

    public static final ObisCode REGISTER_MAPPING_OBISCODE = ObisCode.fromString("1.0.1.8.0.255");

    private static final Unit REGISTER_UNIT = Unit.get(BaseUnit.WATTHOUR, 3);
    private static final long REGISTER_GROUP_ID = 3145;
    private static final int DEFAULT_RTU_REGISTER_GROUP_ID = 0;
    private static final String METER_SERIAL_NUMBER = "MeterSerialNumber";
    private static final long REGISTER_SPEC_ID = 48654;

    @Mock
    private IdentificationService identificationService;

    public static RegisterSpec getMockedRegisterSpec(RegisterGroup registerGroup) {
        RegisterType mockedMeasurementType = getMockedRegisterType(registerGroup);
        RegisterSpec registerSpec = mock(RegisterSpec.class, withSettings().extraInterfaces(NumericalRegisterSpec.class));
        when(registerSpec.getDeviceObisCode()).thenReturn(REGISTER_MAPPING_OBISCODE);
        when(registerSpec.getId()).thenReturn(REGISTER_SPEC_ID);
        when(registerSpec.getRegisterType()).thenReturn(mockedMeasurementType);
        when(((NumericalRegisterSpec) registerSpec).getOverflowValue()).thenReturn(Optional.empty());
        when(mockedMeasurementType.getUnit()).thenReturn(REGISTER_UNIT);
        return registerSpec;
    }

    public static RegisterType getMockedRegisterType(RegisterGroup registerGroup) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(OfflineRegisterImplTest.class.getSimpleName());
        RegisterType registerType = mock(RegisterType.class);
        if (registerGroup == null) {
            when(registerType.getRegisterGroups()).thenReturn(Collections.<RegisterGroup>emptyList());
        } else {
            when(registerType.getRegisterGroups()).thenReturn(Arrays.asList(registerGroup));
        }
        when(registerType.getReadingType()).thenReturn(readingType);
        return registerType;
    }

    public static RegisterGroup getMockedRtuRegisterGroup() {
        RegisterGroup rtuRegisterGroup = mock(RegisterGroup.class);
        when(rtuRegisterGroup.getId()).thenReturn(REGISTER_GROUP_ID);
        return rtuRegisterGroup;
    }

    @Test
    public void goOfflineTest() {
        Device device = getMockedDevice();
        Register register = mock(Register.class);
        RegisterGroup mockedRegisterGroup = getMockedRtuRegisterGroup();
        RegisterSpec mockedRegisterSpec = getMockedRegisterSpec(mockedRegisterGroup);
        when(register.getRegisterSpec()).thenReturn(mockedRegisterSpec);
        when(register.getRegisterSpecId()).thenReturn(REGISTER_SPEC_ID);
        when(register.getDevice()).thenReturn(device);
        when(register.getDeviceObisCode()).thenReturn(REGISTER_MAPPING_OBISCODE);

        //Business Methods
        OfflineRegister offlineRegister = new OfflineRegisterImpl(register, this.identificationService);

        // asserts
        assertThat(offlineRegister).isNotNull();
        assertEquals(REGISTER_SPEC_ID, offlineRegister.getRegisterId());
        assertEquals(REGISTER_MAPPING_OBISCODE, offlineRegister.getObisCode());
        assertEquals(REGISTER_UNIT, offlineRegister.getUnit());
        assertThat(offlineRegister.inGroup(REGISTER_GROUP_ID)).isTrue();
        assertEquals(METER_SERIAL_NUMBER, offlineRegister.getSerialNumber());
    }

    private Device getMockedDevice() {
        Device device = mock(Device.class);
        when(device.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        return device;
    }

    @Test
    public void goOfflineHavingNoDeviceRegisterGroupSetTest() {
        Device device = getMockedDevice();
        Register register = getMockedRegister(device);
        when(register.getDeviceObisCode()).thenReturn(REGISTER_MAPPING_OBISCODE);
        when(register.getRegisterSpecId()).thenReturn(REGISTER_SPEC_ID);

        //Business Methods
        OfflineRegister offlineRegister = new OfflineRegisterImpl(register, identificationService);

        // asserts
        assertThat(offlineRegister).isNotNull();
        assertEquals(REGISTER_SPEC_ID, offlineRegister.getRegisterId());
        assertEquals(REGISTER_MAPPING_OBISCODE, offlineRegister.getObisCode());
        assertEquals(REGISTER_UNIT, offlineRegister.getUnit());
        assertThat(offlineRegister.inGroup(REGISTER_GROUP_ID)).isFalse();
        assertEquals(METER_SERIAL_NUMBER, offlineRegister.getSerialNumber());
    }

    private Register getMockedRegister(Device device) {
        Register register = mock(Register.class);
        when(register.getDevice()).thenReturn(device);
        RegisterSpec mockedRtuRegisterSpec = getMockedRegisterSpec(null);
        when(register.getRegisterSpec()).thenReturn(mockedRtuRegisterSpec);
        return register;
    }

    @Test
    public void deviceIdentifierForKnownDeviceBySerialNumberShouldBeUsedTest() {
        Device device = getMockedDevice();
        Register register = getMockedRegister(device);

        DeviceIdentifierForAlreadyKnownDeviceBySerialNumber deviceIdentifierForAlreadyKnownDevice = new DeviceIdentifierForAlreadyKnownDeviceBySerialNumber(device);
        when(identificationService.createDeviceIdentifierForAlreadyKnownDevice(any(Device.class))).thenReturn(deviceIdentifierForAlreadyKnownDevice);

        OfflineRegisterImpl offlineRegister = new OfflineRegisterImpl(register, identificationService);

        assertThat(offlineRegister.getDeviceIdentifier().forIntrospection().getTypeName()).isEqualTo("SerialNumber");
    }

}
