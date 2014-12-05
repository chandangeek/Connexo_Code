package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import org.fest.assertions.core.Condition;

import java.util.List;

import org.junit.*;

/**
 * Tests the aspects of the {@link TopologyServiceImpl} component
 * that relate to {@link LoadProfile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (17:23)
 */
public class TopologyServiceLoadProfileImplTest extends PersistenceTestWithMockedDeviceProtocol {

    // Look at LoadProfileImplTest for missing members as the code was already partially moved from there

    @Test
    @Transactional
    public void getAllChannelsTestWithASlaveDeviceTest() {

        final Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", "dwl");
        masterWithLoadProfile.save();
        final Device slave = createSlaveDeviceWithSameLoadProfileType(masterWithLoadProfile);

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(masterWithLoadProfile);

        assertThat(reloadedLoadProfile.getAllChannels()).hasSize(4);
        assertThat(reloadedLoadProfile.getAllChannels()).has(new Condition<List<Channel>>() {
            @Override
            public boolean matches(List<Channel> value) {
                int masterChannels = 0;
                int slaveChannels = 0;
                int obisCode1Match = 0;
                int obisCode2Match = 0;
                for (Channel channel : value) {
                    if (channel.getDevice().getId() == masterWithLoadProfile.getId()) {
                        masterChannels++;
                    }
                    if (channel.getDevice().getId() == slave.getId()) {
                        slaveChannels++;
                    }
                    if (channel.getRegisterTypeObisCode().equals(obisCode1)) {
                        obisCode1Match++;
                    }
                    if (channel.getRegisterTypeObisCode().equals(obisCode2)) {
                        obisCode2Match++;
                    }
                }
                return masterChannels == 2 && slaveChannels == 2 && obisCode1Match == 2 && obisCode2Match == 2;
            }
        });
    }

    @Test
    @Transactional
    public void getAllChannelsTestWithoutSlaveDevices() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID);
        deviceWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);
        assertThat(reloadedLoadProfile.getAllChannels()).hasSize(2);
        assertThat(reloadedLoadProfile.getAllChannels()).has(new Condition<List<Channel>>() {
            @Override
            public boolean matches(List<Channel> value) {
                boolean bothMatch = true;
                for (Channel channel : value) {
                    bothMatch &= (channel.getRegisterTypeObisCode().equals(obisCode1) || channel.getRegisterTypeObisCode().equals(obisCode2));
                }
                return bothMatch;
            }
        });
    }

}