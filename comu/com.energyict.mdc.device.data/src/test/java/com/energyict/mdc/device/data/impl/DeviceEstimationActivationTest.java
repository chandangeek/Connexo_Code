package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceEstimationActivationTest extends PersistenceIntegrationTest {
    
    @Test
    @Transactional
    public void testToggleActivateEstimation() {
        Device device = createSimpleDeviceWithName("device", "device");
        DeviceEstimation deviceEstimation = device.forEstimation();
        assertThat(deviceEstimation.isEstimationActive()).isFalse();
        deviceEstimation.activateEstimation();
        assertThat(deviceEstimation.isEstimationActive()).isTrue();
        
        device = inMemoryPersistence.getDeviceService().findByUniqueMrid("device").get();
        deviceEstimation = device.forEstimation();
        assertThat(deviceEstimation.isEstimationActive()).isTrue();
        deviceEstimation.deactivateEstimation();

        device = inMemoryPersistence.getDeviceService().findByUniqueMrid("device").get();
        assertThat(device.forEstimation().isEstimationActive()).isFalse();
    }
    
    @Test
    @Transactional
    public void testSyncRuleSetActivationsIfDeviceConfigChanged() {
        EstimationRuleSet rs1 = createEstimationRuleSet("RS1");
        EstimationRuleSet rs2 = createEstimationRuleSet("RS2");
        EstimationRuleSet rs3 = createEstimationRuleSet("RS3");
        
        deviceConfiguration.addEstimationRuleSet(rs1);
        deviceConfiguration.addEstimationRuleSet(rs2);
        deviceConfiguration.addEstimationRuleSet(rs3);
        deviceConfiguration.save();
        
        Device device = createSimpleDeviceWithName("device", "device");
        assertThat(device.getVersion()).isEqualTo(2l);
        device.forEstimation().deactivateEstimationRuleSet(rs1);
        device.forEstimation().deactivateEstimationRuleSet(rs2);
        
        
        List<DeviceEstimationRuleSetActivation> ruleSetActivations = device.forEstimation().getEstimationRuleSetActivations();
        assertThat(device.getVersion()).isEqualTo(4l);
        assertThat(ruleSetActivations.stream().map(rs -> rs.getEstimationRuleSet()).collect(Collectors.toList())).containsExactly(rs1, rs2, rs3);
        assertThat(ruleSetActivations.stream().map(rs -> rs.isActive()).collect(Collectors.toList())).containsExactly(false, false, true);

        deviceConfiguration.removeEstimationRuleSet(rs1);
        deviceConfiguration.save();
        
        ruleSetActivations = inMemoryPersistence.getDeviceService().findByUniqueMrid("device").get()
                    .forEstimation().getEstimationRuleSetActivations();
        assertThat(device.getVersion()).isEqualTo(4l);
        assertThat(ruleSetActivations.stream().map(rs -> rs.getEstimationRuleSet()).collect(Collectors.toList())).containsExactly(rs2, rs3);
        assertThat(ruleSetActivations.stream().map(rs -> rs.isActive()).collect(Collectors.toList())).containsExactly(false, true);
    }
    
    @Test
    @Transactional
    public void testToggleEstimationRuleSet() {
        EstimationRuleSet rs1 = createEstimationRuleSet("RS1");
        EstimationRuleSet rs2 = createEstimationRuleSet("RS2");
        EstimationRuleSet rs3 = createEstimationRuleSet("RS3");
        
        deviceConfiguration.addEstimationRuleSet(rs1);
        deviceConfiguration.addEstimationRuleSet(rs2);
        deviceConfiguration.addEstimationRuleSet(rs3);
        deviceConfiguration.save();
        
        List<DeviceEstimationRuleSetActivation> ruleSetActivations = null;
        Device device = createSimpleDeviceWithName("device", "device");
        ruleSetActivations = device.forEstimation().getEstimationRuleSetActivations();

        assertThat(device.getVersion()).isEqualTo(2l);
        assertThat(device.forEstimation().isEstimationActive()).isFalse();
        assertThat(ruleSetActivations.stream().map(rs -> rs.getEstimationRuleSet()).collect(Collectors.toList())).containsExactly(rs1, rs2, rs3);
        assertThat(ruleSetActivations.stream().map(rs -> rs.isActive()).collect(Collectors.toList())).containsExactly(true, true, true);
        
        device.forEstimation().deactivateEstimationRuleSet(rs2);
        device = inMemoryPersistence.getDeviceService().findByUniqueMrid("device").get();
        ruleSetActivations = device.forEstimation().getEstimationRuleSetActivations();

        assertThat(device.getVersion()).isEqualTo(3l);
        assertThat(device.forEstimation().isEstimationActive()).isFalse();
        assertThat(ruleSetActivations.stream().map(rs -> rs.getEstimationRuleSet()).collect(Collectors.toList())).containsExactly(rs1, rs2, rs3);
        assertThat(ruleSetActivations.stream().map(rs -> rs.isActive()).collect(Collectors.toList())).containsExactly(true, false, true);
        
        device.forEstimation().activateEstimationRuleSet(rs2);
        device.forEstimation().deactivateEstimationRuleSet(rs3);
        device.forEstimation().activateEstimation();
        device = inMemoryPersistence.getDeviceService().findByUniqueMrid("device").get();
        ruleSetActivations = device.forEstimation().getEstimationRuleSetActivations();

        assertThat(device.getVersion()).isEqualTo(6l);
        assertThat(device.forEstimation().isEstimationActive()).isTrue();
        assertThat(ruleSetActivations.stream().map(rs -> rs.getEstimationRuleSet()).collect(Collectors.toList())).containsExactly(rs1, rs2, rs3);
        assertThat(ruleSetActivations.stream().map(rs -> rs.isActive()).collect(Collectors.toList())).containsExactly(true, true, false);
    }
    
    @Test
    @Transactional
    public void testEstimationRuleSetResolver() {
        DeviceConfigurationEstimationRuleSetResolver resolver = new DeviceConfigurationEstimationRuleSetResolver();
        resolver.setDeviceConfigurationService(inMemoryPersistence.getDeviceConfigurationService());
        resolver.setDeviceService(inMemoryPersistence.getDeviceService());
        
        EstimationRuleSet rs1 = createEstimationRuleSet("RS1");
        EstimationRuleSet rs2 = createEstimationRuleSet("RS2");
        deviceConfiguration.addEstimationRuleSet(rs1);
        deviceConfiguration.addEstimationRuleSet(rs2);
        deviceConfiguration.save();
        
        Device device = createSimpleDeviceWithName("device", "device");
        device.forEstimation().activateEstimation();
        device.forEstimation().activateEstimationRuleSet(rs1);
        device.forEstimation().deactivateEstimationRuleSet(rs2);
        
        Meter meter = inMemoryPersistence.getMeteringService().findMeter("device").get();
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        
        List<EstimationRuleSet> resolvedRuleSets = resolver.resolve(meterActivation);
        
        assertThat(resolvedRuleSets).hasSize(1);
        assertThat(resolvedRuleSets).containsExactly(rs1);
        
        device.forEstimation().deactivateEstimation();
        resolvedRuleSets = resolver.resolve(meterActivation);
        
        assertThat(resolvedRuleSets).hasSize(0);
    }
    
    private Device createSimpleDeviceWithName(String name, String mRID){
        return inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, mRID, Instant.now());
    }
    
    private EstimationRuleSet createEstimationRuleSet(String name) {
        EstimationRuleSet estimationRuleSet = inMemoryPersistence.getEstimationService().createEstimationRuleSet(name, QualityCodeSystem.MDC);
        estimationRuleSet.save();
        return estimationRuleSet;
    }
}
