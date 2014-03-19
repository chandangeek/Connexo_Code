package com.energyict.mdc.device.data.impl.offline;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.common.ObisCode;
import org.junit.*;

import static org.junit.Assert.*;
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
    private static final int RTU_REGISTER_GROUP_ID = 3145;
    private static final int DEFAULT_RTU_REGISTER_GROUP_ID = 0;
    private static final String METER_SERIAL_NUMBER = "MeterSerialNumber";
    private static final int RTU_REGISTER_ID = 48654;

    public static RegisterSpec getMockedRtuRegisterSpec() {
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        when(registerSpec.getDeviceObisCode()).thenReturn(RTU_REGISTER_MAPPING_OBISCODE);
        return registerSpec;
    }

    public static RegisterGroup getMockedRtuRegisterGroup() {
        RegisterGroup rtuRegisterGroup = mock(RegisterGroup.class);
        when(rtuRegisterGroup.getId()).thenReturn(RTU_REGISTER_GROUP_ID);
        return rtuRegisterGroup;
    }

    @Test
    public void goOfflineTest() {
        BaseDevice rtu = mock(BaseDevice.class);
        when(rtu.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        BaseRegister rtuRegister = mock(BaseRegister.class);
        when(rtuRegister.getId()).thenReturn(RTU_REGISTER_ID);
        when(rtuRegister.getUnit()).thenReturn(REGISTER_UNIT);
        RegisterGroup mockedRtuRegisterGroup = getMockedRtuRegisterGroup();
        when(rtuRegister.getRegisterGroup()).thenReturn(mockedRtuRegisterGroup);
        RegisterSpec mockedRegisterSpec = getMockedRtuRegisterSpec();
        when(rtuRegister.getRegisterSpec()).thenReturn(mockedRegisterSpec);
        when(rtuRegister.getDevice()).thenReturn(rtu);

        //Business Methods
        OfflineRegister offlineRegister = new OfflineRegisterImpl(rtuRegister);

        // asserts
        assertNotNull(offlineRegister);
        assertEquals("Expected the correct RtuRegisterID", RTU_REGISTER_ID, offlineRegister.getRegisterId());
        assertEquals("Expected the correct obiscode", RTU_REGISTER_MAPPING_OBISCODE, offlineRegister.getObisCode());
        assertEquals("Expected the correct unit", REGISTER_UNIT, offlineRegister.getUnit());
        assertEquals("Expected the correct registerGroupId", RTU_REGISTER_GROUP_ID, offlineRegister.getRegisterGroupId());
        assertEquals("Expected the correct SerialNumber", METER_SERIAL_NUMBER, offlineRegister.getSerialNumber());
    }

    @Test
    public void goOfflineHavingNoDeviceRegisterGroupSetTest() {
        BaseDevice rtu = mock(BaseDevice.class);
        when(rtu.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        BaseRegister rtuRegister = mock(BaseRegister.class);
        when(rtuRegister.getId()).thenReturn(RTU_REGISTER_ID);
        when(rtuRegister.getUnit()).thenReturn(REGISTER_UNIT);
        when(rtuRegister.getRegisterGroup()).thenReturn(null);  // The rtuRegister does not belong to any group
        RegisterSpec mockedRtuRegisterSpec = getMockedRtuRegisterSpec();
        when(rtuRegister.getRegisterSpec()).thenReturn(mockedRtuRegisterSpec);
        when(rtuRegister.getDevice()).thenReturn(rtu);

        //Business Methods
        OfflineRegister offlineRegister = new OfflineRegisterImpl(rtuRegister);

        // asserts
        assertNotNull(offlineRegister);
        assertEquals("Expected the correct RtuRegisterID", RTU_REGISTER_ID, offlineRegister.getRegisterId());
        assertEquals("Expected the correct obiscode", RTU_REGISTER_MAPPING_OBISCODE, offlineRegister.getObisCode());
        assertEquals("Expected the correct unit", REGISTER_UNIT, offlineRegister.getUnit());
        assertEquals("Expected the correct registerGroupId", DEFAULT_RTU_REGISTER_GROUP_ID, offlineRegister.getRegisterGroupId());
        assertEquals("Expected the correct SerialNumber", METER_SERIAL_NUMBER, offlineRegister.getSerialNumber());
    }
}
