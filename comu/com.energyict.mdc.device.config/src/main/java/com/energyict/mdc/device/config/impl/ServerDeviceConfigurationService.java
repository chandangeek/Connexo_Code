/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.QueryStream;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ChannelSpecLinkType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LockService;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.tasks.ComTask;

import java.util.List;
import java.util.Optional;

/**
 * Extends {@link DeviceConfigurationService} with behavior that
 * is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (16:36)
 */
public interface ServerDeviceConfigurationService extends DeviceConfigurationService, LockService {

    QueryStream<AllowedCalendar> getAllowedCalendarsQuery();

    Thesaurus getThesaurus();

    List<DeviceConfiguration> findDeviceConfigurationsByDeviceType(DeviceType deviceType);

    ChannelSpec findChannelSpecByDeviceConfigurationAndName(DeviceConfiguration deviceConfig, String name);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are linked to the given {@link ChannelSpec} and
     * has the given {@link ChannelSpecLinkType}.
     *
     * @param channelSpec the {@link com.energyict.mdc.device.config.ChannelSpec}
     * @param linkType    the {@link com.energyict.mdc.device.config.ChannelSpecLinkType}
     * @return the list of RegisterSpecs
     */
    List<RegisterSpec> findRegisterSpecsByChannelSpecAndLinkType(ChannelSpec channelSpec, ChannelSpecLinkType linkType);

    /**
     * Finds a list of {@link ChannelSpec ChannelSpecs} which are linked to the given {@link LoadProfileSpec}.
     *
     * @param loadProfileSpec the LoadProfileSpec
     * @return the list of ChannelSpecs
     */
    List<ChannelSpec> findChannelSpecsForLoadProfileSpec(LoadProfileSpec loadProfileSpec);

    /**
     * Tests if the specified {@link ComTask} is used by at least one {@link DeviceConfiguration}.
     *
     * @param comTask The ComTask
     * @return A flag that indicates if the ComTask is used or not
     */
    boolean usedByDeviceConfigurations(ComTask comTask);

    Optional<DeviceMessageFile> findDeviceMessageFile(long id);

}