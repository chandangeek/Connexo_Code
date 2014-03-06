package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ChannelSpecLinkType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import java.util.List;

/**
 * Extends {@link DeviceConfigurationService} with behavior that
 * is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (16:36)
 */
public interface ServerDeviceConfigurationService extends DeviceConfigurationService {

    public List<DeviceConfiguration> findDeviceConfigurationsByDeviceType(DeviceType deviceType);

    public ChannelSpec findChannelSpecByDeviceConfigurationAndName(DeviceConfiguration deviceConfig, String name);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are linked to the given {@link ChannelSpec} and
     * has the given {@link ChannelSpecLinkType}
     *
     * @param channelSpec the {@link com.energyict.mdc.device.config.ChannelSpec}
     * @param linkType    the {@link com.energyict.mdc.device.config.ChannelSpecLinkType}
     * @return the list of RegisterSpecs
     */
    public List<RegisterSpec> findRegisterSpecsByChannelSpecAndLinkType(ChannelSpec channelSpec, ChannelSpecLinkType linkType);

}