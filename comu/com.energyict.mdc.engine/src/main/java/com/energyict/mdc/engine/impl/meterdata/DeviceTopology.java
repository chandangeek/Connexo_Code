package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.CollectedDeviceTopologyDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a DeviceTopology, collected from a Device. If no data could be collected or the feature is not supported,
 * the a proper {@link com.energyict.mdc.issues.Issue} and {@link com.energyict.mdc.protocol.api.device.data.ResultType} should be returned.
 *
 * @author gna
 * @since 5/04/12 - 11:57
 */
public class DeviceTopology extends CollectedDeviceData implements CollectedTopology {

    /**
     * The unique identifier of the Device
     */
    private final DeviceIdentifier deviceIdentifier;

    /**
     * The {@link TopologyAction action} that should be executed.
     * By default, topology action UPDATE will be used.
     */
    private TopologyAction topologyAction = TopologyAction.UPDATE;

    /**
     * A list containing the unique device identifiers of all attached slave devices.
     * If this device has no attached slaves, the list is empty.
     */
    private List<DeviceIdentifier> slaveDeviceIdentifiers;
    private ComTaskExecution comTaskExecution;

    /**
     * Default constructor
     *
     * @param deviceIdentifier unique identification of the device which need s to update his cache
     */
    public DeviceTopology(DeviceIdentifier deviceIdentifier) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.slaveDeviceIdentifiers = new ArrayList<>();
    }

    public DeviceTopology(DeviceIdentifier deviceIdentifier, List<DeviceIdentifier> slaveDeviceIdentifiers) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.slaveDeviceIdentifiers = slaveDeviceIdentifiers;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToUpdateTopology();
    }

    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        return new CollectedDeviceTopologyDeviceCommand(this, comTaskExecution, issueService);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.deviceIdentifier;
    }

    @Override
    public List<DeviceIdentifier> getSlaveDeviceIdentifiers() {
        List<DeviceIdentifier> slaveDeviceIdentifiers = new ArrayList<>(this.slaveDeviceIdentifiers.size());
        for (DeviceIdentifier identifier : this.slaveDeviceIdentifiers) {
            slaveDeviceIdentifiers.add(identifier);
        }
        return slaveDeviceIdentifiers;
    }

    @Override
    public void addSlaveDevice(DeviceIdentifier slaveIdentifier) {
        slaveDeviceIdentifiers.add(slaveIdentifier);
    }

    @Override
    public void removeSlaveDevice(DeviceIdentifier slaveIdentifier) {
        slaveDeviceIdentifiers.remove(slaveIdentifier);
    }

    @Override
    public TopologyAction getTopologyAction() {
        return topologyAction;
    }

    @Override
    public void setTopologyAction(TopologyAction topologyAction) {
        this.topologyAction = topologyAction;
    }

    @Override
    public void setDataCollectionConfiguration (DataCollectionConfiguration configuration) {
        this.comTaskExecution = (ComTaskExecution) configuration;
    }

}