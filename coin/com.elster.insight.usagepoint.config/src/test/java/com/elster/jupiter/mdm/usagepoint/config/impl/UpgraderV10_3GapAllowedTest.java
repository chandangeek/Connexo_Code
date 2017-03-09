/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationUpdater;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.users.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dantonov on 09.03.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class UpgraderV10_3GapAllowedTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private UserService userService;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private MetrologyConfigurationsInstaller metrologyConfigurationsInstaller;
    @Mock
    private DataModelUpgrader dataModelUpgrader;
    @InjectMocks
    private UpgraderV10_3 upgrader;

    @Before
    public void before() {
        // mock all calls to findMetrologyConfiguration method
        when(metrologyConfigurationService.findMetrologyConfiguration(any(String.class))).thenReturn(Optional.of(mock
                (MetrologyConfiguration.class)));
        // mock two calls to findMetrologyConfiguration for one configuration
        when(metrologyConfigurationService.findMetrologyConfiguration(MetrologyConfigurationsInstaller
                .OOTBMetrologyConfiguration.RESIDENTIAL_PROSUMER_WITH_1_METER.getName()))
                .thenReturn(Optional.of(mock
                        (MetrologyConfiguration.class)), Optional.empty());
    }

    /**
     * Test {@link UpgraderV10_3#upgradeGapAllowedFlagForMetrologyConfigurations()} method
     */
    @Test
    public void upgradeGapAllowedFlagForMetrologyConfigurationsTest() {

        List<UsagePointMetrologyConfiguration> correctOotbMetrologyConfigurations = new ArrayList<>();

        correctOotbMetrologyConfigurations.addAll(Arrays.stream(MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration
                .values()).map
                (ootbMetrologyConfiguration -> {
                    return mockMetrologyConfiguration(ootbMetrologyConfiguration.getName(),
                            ootbMetrologyConfiguration.isGapAllowed());
                }).collect(Collectors.toList()));

        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn
                (new ArrayList<>(correctOotbMetrologyConfigurations));

        upgrader.migrate(dataModelUpgrader);

        correctOotbMetrologyConfigurations.forEach((metrologyConfiguration -> {
            verify(metrologyConfiguration, times(0)).startUpdate();
        }));
    }

    /**
     * Test {@link UpgraderV10_3#upgradeGapAllowedFlagForMetrologyConfigurations()} method when all metrology
     * configuration has wrong gapAllowed flag
     */
    @Test
    public void upgradeGapAllowedFlagForIncorrectMetrologyConfigurationsTest() {

        List<UsagePointMetrologyConfiguration> incorrectOotbMetrologyConfigurations = new ArrayList<>();

        incorrectOotbMetrologyConfigurations.addAll(Arrays.stream(MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration
                .values()).map(ootbMetrologyConfiguration -> {
                    return mockMetrologyConfiguration(ootbMetrologyConfiguration.getName(),
                            !ootbMetrologyConfiguration.isGapAllowed());
                }).collect(Collectors.toList()));

        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn
                (new ArrayList<>(incorrectOotbMetrologyConfigurations));

            upgrader.migrate(dataModelUpgrader);

        incorrectOotbMetrologyConfigurations.forEach((metrologyConfiguration -> {
            verify(metrologyConfiguration, times(1)).startUpdate();
        }));
    }

    private UsagePointMetrologyConfiguration mockMetrologyConfiguration(String name, boolean isGapAllowed) {
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);

        when(metrologyConfiguration.getName()).thenReturn(name);
        when(metrologyConfiguration.isGapAllowed()).thenReturn(isGapAllowed);

        MetrologyConfigurationUpdater updater = mock(MetrologyConfigurationUpdater.class);
        when(updater.setGapAllowed(anyBoolean())).thenReturn(updater);
        when(metrologyConfiguration.startUpdate()).thenReturn(updater);

        return metrologyConfiguration;
    }
}
