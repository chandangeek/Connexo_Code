/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.util.collections.KPermutation;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeviceConfigurationEstimationRuleSetUsageTest extends PersistenceTest {

    DeviceType deviceType;

    DeviceConfiguration deviceConfiguration;
    
    DeviceConfigurationService deviceConfigurationService;

    @Mock
    DeviceProtocol deviceProtocol;
    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    @Before
    public void setUp() {
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        deviceConfigurationService = PersistenceTest.inMemoryPersistence.getDeviceConfigurationService();
        deviceType = createDeviceType("Device type");
        deviceConfiguration = createDeviceConfiguration("Device configuration", deviceType);
    }

    @Test
    @Transactional
    public void testAddEstimationRuleSets() {
        deviceConfiguration.addEstimationRuleSet(createEstimationRuleSet("RuleSet1"));
        deviceConfiguration.addEstimationRuleSet(createEstimationRuleSet("RuleSet2"));
        deviceConfiguration.addEstimationRuleSet(createEstimationRuleSet("RuleSet3"));
        deviceConfiguration.save();

        List<EstimationRuleSet> estimationRuleSets = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get()
                .getEstimationRuleSets();

        assertThat(estimationRuleSets).hasSize(3);
        assertThat(estimationRuleSets.stream().map(EstimationRuleSet::getName).collect(Collectors.toList())).containsExactly("RuleSet1", "RuleSet2", "RuleSet3");
    }

    @Test
    @Transactional
    public void testRemoveEstimationRuleSets() {
        EstimationRuleSet estimationRuleSet = createEstimationRuleSet("RuleSet3");
        deviceConfiguration.addEstimationRuleSet(estimationRuleSet);
        deviceConfiguration.save();

        List<EstimationRuleSet> estimationRuleSets;
        estimationRuleSets = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get().getEstimationRuleSets();
        assertThat(estimationRuleSets).hasSize(1);

        deviceConfiguration.removeEstimationRuleSet(estimationRuleSet);

        estimationRuleSets = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get().getEstimationRuleSets();
        assertThat(estimationRuleSets).isEmpty();
    }

    @Test
    @Transactional
    public void testReorderEstimationRuleSets() {
        EstimationRuleSet ruleSet1 = createEstimationRuleSet("RuleSet1");
        EstimationRuleSet ruleSet2 = createEstimationRuleSet("RuleSet2");
        EstimationRuleSet ruleSet3 = createEstimationRuleSet("RuleSet3");
        deviceConfiguration.addEstimationRuleSet(ruleSet1);
        deviceConfiguration.addEstimationRuleSet(ruleSet2);
        deviceConfiguration.addEstimationRuleSet(ruleSet3);
        deviceConfiguration.save();

        List<EstimationRuleSet> estimationRuleSets = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get()
                .getEstimationRuleSets();

        assertThat(estimationRuleSets).hasSize(3);
        assertThat(estimationRuleSets.stream().map(EstimationRuleSet::getName).collect(Collectors.toList())).containsExactly("RuleSet1", "RuleSet2", "RuleSet3");

        KPermutation kPermutation = KPermutation.of(new long[] { 1, 2, 3 }, new long[] { 3, 2, 1 });
        deviceConfiguration.reorderEstimationRuleSets(kPermutation);

        estimationRuleSets = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get().getEstimationRuleSets();

        assertThat(estimationRuleSets).hasSize(3);
        assertThat(estimationRuleSets.stream().map(EstimationRuleSet::getName).collect(Collectors.toList())).containsExactly("RuleSet3", "RuleSet2", "RuleSet1");
    }
    
    @Test
    @Transactional
    public void testFindAllLinkedDeviceConfigurationForEstiamationRuleSet() {
        EstimationRuleSet estimationRuleSet = createEstimationRuleSet("RuleSet");
        
        DeviceConfiguration dcA = createDeviceConfiguration("dcA", deviceType);
        dcA.addEstimationRuleSet(estimationRuleSet);
        dcA.save();
        DeviceConfiguration dcB = createDeviceConfiguration("dcB", deviceType);
        dcB.addEstimationRuleSet(estimationRuleSet);
        dcB.save();
        DeviceConfiguration dcC = createDeviceConfiguration("dcC", deviceType);
        dcC.addEstimationRuleSet(estimationRuleSet);
        dcC.save();
        DeviceConfiguration notLinked = createDeviceConfiguration("dc1", deviceType);
        notLinked.save();
        
        List<DeviceConfiguration> result = deviceConfigurationService.findDeviceConfigurationsForEstimationRuleSet(estimationRuleSet).find();
        
        assertThat(result).hasSize(3);
        assertThat(result.stream().map(DeviceConfiguration::getName).toArray()).containsExactly("dcA", "dcB", "dcC");
    }

    private EstimationRuleSet createEstimationRuleSet(String name) {
        EstimationRuleSet estimationRuleSet = PersistenceTest.inMemoryPersistence.getEstimationService().createEstimationRuleSet(name, QualityCodeSystem.MDC);
        estimationRuleSet.save();
        return estimationRuleSet;
    }

    private DeviceType createDeviceType(String name) {
        return deviceConfigurationService.newDeviceType(name, deviceProtocolPluggableClass);
    }

    private DeviceConfiguration createDeviceConfiguration(String name, DeviceType deviceType) {
        DeviceConfiguration configuration = deviceType.newConfiguration(name).add();
        configuration.save();
        return configuration;
    }
}
