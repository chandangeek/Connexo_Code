/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;

import java.util.Optional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link OfflineLoadProfileChannelImpl}
 *
 * @author gna
 * @since 30/05/12 - 14:31
 */
public class OfflineLoadProfileChannelImplTest {

    private static final Unit CHANNEL_UNIT = Unit.get(BaseUnit.WATTHOUR, 3);
    private static final long DEVICE_ID = 5431;
    private static final long LOAD_PROFILE_ID = 1246;
    private static final boolean STORE_DATA = true;
    private static final String MASTER_SERIAL_NUMBER = "Master_SerialNumber";

    @Test
    public void goOfflineTest() {
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getId()).thenReturn(LOAD_PROFILE_ID);
        Device device = mock(Device.class);
        when(device.getSerialNumber()).thenReturn(MASTER_SERIAL_NUMBER);
        when(device.getId()).thenReturn(DEVICE_ID);
        Channel channel = mock(Channel.class);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channel.getObisCode()).thenReturn(OfflineRegisterImplTest.REGISTER_MAPPING_OBISCODE);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channel.getUnit()).thenReturn(CHANNEL_UNIT);
        when(channel.getLoadProfile()).thenReturn(loadProfile);
        when(channel.getDevice()).thenReturn(device);
        when(channel.getOverflow()).thenReturn(Optional.empty());

        OfflineLoadProfileChannelImpl offlineLoadProfileChannel = new OfflineLoadProfileChannelImpl(channel);

        // Asserts
        assertNotNull(offlineLoadProfileChannel);
        assertEquals("Expected the correct ObisCode", OfflineRegisterImplTest.REGISTER_MAPPING_OBISCODE, offlineLoadProfileChannel.getObisCode());
        assertEquals("Expected the correct Unit", CHANNEL_UNIT, offlineLoadProfileChannel.getUnit());
        assertEquals("Expected the correct DEVICE_ID", DEVICE_ID, offlineLoadProfileChannel.getRtuId());
        assertEquals("Expected the correct LOAD_PROFILE_ID", LOAD_PROFILE_ID, offlineLoadProfileChannel.getLoadProfileId());
        assertEquals("Expected the correct MASTER_SERIAL_NUMBER", MASTER_SERIAL_NUMBER, offlineLoadProfileChannel.getMasterSerialNumber());
        assertTrue("Expected to store the meterData", offlineLoadProfileChannel.isStoreData());
    }

}
