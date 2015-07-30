package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ChannelSpecLinkType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.nls.Thesaurus;

import java.util.List;

/**
 * Extends {@link DeviceConfigurationService} with behavior that
 * is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (16:36)
 */
public interface ServerDeviceConfigurationService extends DeviceConfigurationService {

    public Thesaurus getThesaurus();

    public List<DeviceConfiguration> findDeviceConfigurationsByDeviceType(DeviceType deviceType);

    public ChannelSpec findChannelSpecByDeviceConfigurationAndName(DeviceConfiguration deviceConfig, String name);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are linked to the given {@link ChannelSpec} and
     * has the given {@link ChannelSpecLinkType}.
     *
     * @param channelSpec the {@link com.energyict.mdc.device.config.ChannelSpec}
     * @param linkType    the {@link com.energyict.mdc.device.config.ChannelSpecLinkType}
     * @return the list of RegisterSpecs
     */
    public List<RegisterSpec> findRegisterSpecsByChannelSpecAndLinkType(ChannelSpec channelSpec, ChannelSpecLinkType linkType);

    /**
     * Finds a list of {@link ChannelSpec ChannelSpecs} which are linked to the given {@link LoadProfileSpec}.
     *
     * @param loadProfileSpec the LoadProfileSpec
     * @return the list of ChannelSpecs
     */
    public List<ChannelSpec> findChannelSpecsForLoadProfileSpec(LoadProfileSpec loadProfileSpec);

    /**
     * Tests if the specified {@link ComTask} is used by at least one {@link DeviceConfiguration}.
     *
     * @param comTask The ComTask
     * @return A flag that indicates if the ComTask is used or not
     */
    public boolean usedByDeviceConfigurations(ComTask comTask);

}