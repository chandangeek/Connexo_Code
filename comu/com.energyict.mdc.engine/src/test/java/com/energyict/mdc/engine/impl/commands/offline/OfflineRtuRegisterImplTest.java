package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link OfflineRegisterImpl} component
 *
 * @author gna
 * @since 14/06/12 - 10:33
 */
public class OfflineRtuRegisterImplTest {

    public static final ObisCode RTU_REGISTER_MAPPING_OBISCODE = ObisCode.fromString("1.0.1.8.0.255");

    private static final Unit REGISTER_UNIT = Unit.get(BaseUnit.WATTHOUR, 3);
    private static final long RTU_REGISTER_GROUP_ID = 3145;
    private static final int DEFAULT_RTU_REGISTER_GROUP_ID = 0;
    private static final String METER_SERIAL_NUMBER = "MeterSerialNumber";
    private static final long REGISTER_SPEC_ID = 48654;

    public static RegisterSpec getMockedRtuRegisterSpec(RegisterGroup registerGroup) {
        RegisterMapping mockedRegisterMapping = getMockedRegisterMapping(registerGroup);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        when(registerSpec.getDeviceObisCode()).thenReturn(RTU_REGISTER_MAPPING_OBISCODE);
        when(registerSpec.getId()).thenReturn(REGISTER_SPEC_ID);
        when(registerSpec.getRegisterMapping()).thenReturn(mockedRegisterMapping);
        when(registerSpec.getUnit()).thenReturn(REGISTER_UNIT);
        return registerSpec;
    }

    public static RegisterMapping getMockedRegisterMapping(RegisterGroup registerGroup){
        RegisterMapping registerMapping = mock(RegisterMapping.class);
        when(registerMapping.getRegisterGroup()).thenReturn(registerGroup);
        return registerMapping;
    }

    public static RegisterGroup getMockedRtuRegisterGroup() {
        RegisterGroup rtuRegisterGroup = mock(RegisterGroup.class);
        when(rtuRegisterGroup.getId()).thenReturn(RTU_REGISTER_GROUP_ID);
        return rtuRegisterGroup;
    }

    @Test
    public void goOfflineTest() {
        Device rtu = mock(Device.class);
        when(rtu.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        Register rtuRegister = mock(Register.class);
        RegisterGroup mockedRegisterGroup = getMockedRtuRegisterGroup();
        RegisterSpec mockedRegisterSpec = getMockedRtuRegisterSpec(mockedRegisterGroup);
        when(rtuRegister.getRegisterSpec()).thenReturn(mockedRegisterSpec);
        when(rtuRegister.getDevice()).thenReturn(rtu);

        //Business Methods
        OfflineRegister offlineRegister = new OfflineRegisterImpl(rtuRegister);

        // asserts
        assertThat(offlineRegister).isNotNull();
        assertEquals(REGISTER_SPEC_ID, offlineRegister.getRegisterId());
        assertEquals(RTU_REGISTER_MAPPING_OBISCODE, offlineRegister.getObisCode());
        assertEquals(REGISTER_UNIT, offlineRegister.getUnit());
        assertEquals(RTU_REGISTER_GROUP_ID, offlineRegister.getRegisterGroupId());
        assertEquals(METER_SERIAL_NUMBER, offlineRegister.getSerialNumber());
    }

    @Test
    public void goOfflineHavingNoDeviceRegisterGroupSetTest() {
        Device device = mock(Device.class);
        when(device.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        Register rtuRegister = mock(Register.class);
        RegisterSpec mockedRtuRegisterSpec = getMockedRtuRegisterSpec(null);
        when(rtuRegister.getRegisterSpec()).thenReturn(mockedRtuRegisterSpec);
        when(rtuRegister.getDevice()).thenReturn(device);

        //Business Methods
        OfflineRegister offlineRegister = new OfflineRegisterImpl(rtuRegister);

        // asserts
        assertThat(offlineRegister).isNotNull();
        assertEquals(REGISTER_SPEC_ID, offlineRegister.getRegisterId());
        assertEquals(RTU_REGISTER_MAPPING_OBISCODE, offlineRegister.getObisCode());
        assertEquals(REGISTER_UNIT, offlineRegister.getUnit());
        assertEquals(DEFAULT_RTU_REGISTER_GROUP_ID, offlineRegister.getRegisterGroupId());
        assertEquals(METER_SERIAL_NUMBER, offlineRegister.getSerialNumber());
    }
}
