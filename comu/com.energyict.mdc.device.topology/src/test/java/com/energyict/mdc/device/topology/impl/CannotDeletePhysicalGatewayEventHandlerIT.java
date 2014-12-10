package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.topology.StillGatewayException;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import org.junit.*;
import org.junit.rules.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link CannotDeletePhysicalGatewayEventHandler} compnent.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:43)
 */
public class CannotDeletePhysicalGatewayEventHandlerIT extends PersistenceIntegrationTest {

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Test(expected = StillGatewayException.class)
    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY)
    public void cannotDeleteBecauseStillUsedAsPhysicalGatewayTest() {
        Device physicalMaster = this.createSimpleDeviceWithName("PhysicalMaster");
        Device device1 = getDeviceService().newDevice(deviceConfiguration, "Origin1", MRID);
        device1.save();
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);

        // Business method
        physicalMaster.delete();

        // Asserts: see expected exception and constraint violation rule
    }

    @Test
    @Transactional
    public void deletePhysicalMasterAfterDeletingSlaveTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device device = getDeviceService().newDevice(deviceConfiguration, "Origin", MRID);
        device.save();
        this.getTopologyService().setPhysicalGateway(device, physicalMaster);

        Device reloadedSlave = getReloadedDevice(device);
        reloadedSlave.delete();

        Device reloadedMaster = getReloadedDevice(physicalMaster);
        long masterId = reloadedMaster.getId();
        reloadedMaster.delete();

        assertThat(getDeviceService().findDeviceById(masterId)).isNull();
    }

    private ServerDeviceService getDeviceService() {
        return inMemoryPersistence.getDeviceService();
    }

    private TopologyService getTopologyService() {
        return inMemoryPersistence.getTopologyService();
    }

}