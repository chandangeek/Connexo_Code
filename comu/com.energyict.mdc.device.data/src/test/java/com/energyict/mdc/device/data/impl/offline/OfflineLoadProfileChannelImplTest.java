package com.energyict.mdc.device.data.impl.offline;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.impl.offline.OfflineLoadProfileChannelImpl;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.device.config.ChannelSpec;

import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.mdc.device.data.impl.offline.OfflineLoadProfileChannelImpl}
 *
 * @author gna
 * @since 30/05/12 - 14:31
 */
public class OfflineLoadProfileChannelImplTest {

    private static final Unit CHANNEL_UNIT = Unit.get(BaseUnit.WATTHOUR, 3);
    private static final int RTU_ID = 5431;
    private static final int LOAD_PROFILE_ID = 1246;
    private static final boolean STORE_DATA = true;
    private static final String MASTER_SERIAL_NUMBER = "Master_SerialNumber";

    @Test
    public void goOfflineTest() {
        BaseDevice rtu = mock(BaseDevice.class);
        when(rtu.getSerialNumber()).thenReturn(MASTER_SERIAL_NUMBER);
        EndDeviceChannel channel = mock(EndDeviceChannel.class);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getDeviceObisCode()).thenReturn(OfflineRtuRegisterImplTest.RTU_REGISTER_MAPPING_OBISCODE);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channel.getUnit()).thenReturn(CHANNEL_UNIT);
        when(channel.getDeviceId()).thenReturn(RTU_ID);
        when(channel.getLoadProfileId()).thenReturn(LOAD_PROFILE_ID);
        when(channel.isStoreData()).thenReturn(STORE_DATA);
        when(channel.getDevice()).thenReturn(rtu);

        OfflineLoadProfileChannelImpl offlineLoadProfileChannel = new OfflineLoadProfileChannelImpl(channel);

        // Asserts
        assertNotNull(offlineLoadProfileChannel);
        Assert.assertEquals("Expected the correct ObisCode", OfflineRtuRegisterImplTest.RTU_REGISTER_MAPPING_OBISCODE, offlineLoadProfileChannel.getObisCode());
        assertEquals("Expected the correct Unit", CHANNEL_UNIT, offlineLoadProfileChannel.getUnit());
        assertEquals("Expected the correct RTU_ID", RTU_ID, offlineLoadProfileChannel.getRtuId());
        assertEquals("Expected the correct LOAD_PROFILE_ID", LOAD_PROFILE_ID, offlineLoadProfileChannel.getLoadProfileId());
        assertEquals("Expected the correct MASTER_SERIAL_NUMBER", MASTER_SERIAL_NUMBER, offlineLoadProfileChannel.getMasterSerialNumber());
        assertTrue("Expected to store the meterData", offlineLoadProfileChannel.isStoreData());
    }

}
