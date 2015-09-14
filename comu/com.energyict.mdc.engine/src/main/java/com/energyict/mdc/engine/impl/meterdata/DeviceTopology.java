package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedDeviceTopologyDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.protocol.api.device.data.TopologyNeighbour;
import com.energyict.mdc.protocol.api.device.data.TopologyPathSegment;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
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
     * The unique identifier of the Device.
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
     * A list containing additional info that is collected for (some of) the devices.
     */
    private List<CollectedDeviceInfo> additionalCollectedDeviceInfo;

    private List<TopologyPathSegment> topologyPathSegments;
    private List<TopologyNeighbour> topologyNeighbours;
    private G3TopologyDeviceAddressInformation g3IDeviceAddressInformation;

    /**
     * Default constructor.
     *
     * @param deviceIdentifier unique identification of the device which need s to update his cache
     */
    public DeviceTopology(DeviceIdentifier deviceIdentifier) {
        this(deviceIdentifier, new ArrayList<>());
    }

    public DeviceTopology(DeviceIdentifier deviceIdentifier, List<DeviceIdentifier> slaveDeviceIdentifiers) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.slaveDeviceIdentifiers = slaveDeviceIdentifiers;
        this.additionalCollectedDeviceInfo = new ArrayList<>();
        this.topologyPathSegments = new ArrayList<>();
        this.topologyNeighbours = new ArrayList<>();
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToUpdateTopology();
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedDeviceTopologyDeviceCommand(this, comTaskExecution, meterDataStoreCommand, serviceProvider);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.deviceIdentifier;
    }

    @Override
    public List<DeviceIdentifier> getSlaveDeviceIdentifiers() {
        return Collections.unmodifiableList(this.slaveDeviceIdentifiers);
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
    public List<CollectedDeviceInfo> getAdditionalCollectedDeviceInfo() {
        return additionalCollectedDeviceInfo;
    }

    @Override
    public void addAdditionalCollectedDeviceInfo(CollectedDeviceInfo additionalDeviceInfo) {
        additionalCollectedDeviceInfo.add(additionalDeviceInfo);
    }

    @Override
    public void injectComTaskExecution(ComTaskExecution comTaskExecution) {
        super.injectComTaskExecution(comTaskExecution);
        this.getAdditionalCollectedDeviceInfo()
                .stream()
                .filter(collectedDeviceInfo -> collectedDeviceInfo instanceof ServerCollectedData)
                .map(ServerCollectedData.class::cast)
                .forEach(collectedDeviceInfo -> collectedDeviceInfo.injectComTaskExecution(comTaskExecution));
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

    @Override
    public void addPathSegmentFor(DeviceIdentifier source, DeviceIdentifier target, DeviceIdentifier intermediateHop, Duration timeToLive, int cost) {
        topologyPathSegments.add(new TopologyPathSegment(source, target, intermediateHop, timeToLive, cost));
    }

    @Override
    public void addTopologyNeighbour(DeviceIdentifier neighbour, int modulationSchema, long toneMap, int modulation, int txGain, int txRes, int txCoeff, int lqi, int phaseDifferential, int tmrValidTime, int neighbourValidTime) {
        topologyNeighbours.add(new TopologyNeighbour(neighbour, modulationSchema, toneMap, modulation, txGain, txRes, txCoeff, lqi, phaseDifferential, tmrValidTime, neighbourValidTime));
    }

    @Override
    public List<TopologyPathSegment> getTopologyPathSegments() {
        return this.topologyPathSegments;
    }

    @Override
    public List<TopologyNeighbour> getTopologyNeighbours() {
        return this.topologyNeighbours;
    }

    @Override
    public void addG3IdentificationInformation(String formattedIPv6Address, int ipv6ShortAddress, int logicalDeviceId) {
        this.g3IDeviceAddressInformation = new G3TopologyDeviceAddressInformation(this.deviceIdentifier, formattedIPv6Address, ipv6ShortAddress, logicalDeviceId);
    }

    @Override
    public G3TopologyDeviceAddressInformation getG3TopologyDeviceAddressInformation() {
        return g3IDeviceAddressInformation;
    }
}