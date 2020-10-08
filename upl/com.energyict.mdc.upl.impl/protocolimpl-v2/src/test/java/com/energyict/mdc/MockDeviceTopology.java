package com.energyict.mdc;

import com.energyict.cbo.ObservationDateProperty;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.TopologyNeighbour;
import com.energyict.mdc.upl.meterdata.TopologyPathSegment;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.mdc.upl.tasks.TopologyAction;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/04/2017 - 11:05
 */
public class MockDeviceTopology implements CollectedTopology {

    /**
     * The unique identifier of the Device
     */
    private final DeviceIdentifier deviceIdentifier;
    /**
     * A list containing the unique device identifiers of all attached slave devices.
     * If this device has no attached slaves, the list is empty.
     */
    private final Map<DeviceIdentifier, ObservationDateProperty> slaveDeviceIdentifiers;
    /**
     * A list containing additional info that is collected for (some of) the devices
     */
    private final List<CollectedDeviceInfo> additionalCollectedDeviceInfo;
    /**
     * A list containing only the nodes which joined the network
     */
    private Map<DeviceIdentifier, ObservationDateProperty> joinedSlaveDeviceIdentifiers;

    /**
     * A list containing only the nodes which were lost from the network
     */
    private List<DeviceIdentifier> lostSlaveDeviceIdentifiers;
    /**
     * The {@link TopologyAction action} that should be executed.
     * By default, topology action UPDATE will be used.
     */
    private TopologyAction topologyAction = TopologyAction.UPDATE;

    private List<TopologyPathSegment> topologyPathSegments;
    private List<TopologyNeighbour> topologyNeighbours;
    private G3TopologyDeviceAddressInformation g3IDeviceAddressInformation;
    private List<Issue> issues;

    /**
     * Default constructor
     *
     * @param deviceIdentifier unique identification of the device which need s to update his cache
     */
    public MockDeviceTopology(DeviceIdentifier deviceIdentifier) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.slaveDeviceIdentifiers = new HashMap<>();
        this.joinedSlaveDeviceIdentifiers = null;
        this.additionalCollectedDeviceInfo = new ArrayList<>();
        lostSlaveDeviceIdentifiers = null;
    }

    public MockDeviceTopology(DeviceIdentifier deviceIdentifier, Map<DeviceIdentifier, ObservationDateProperty> slaveDeviceIdentifiers) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.slaveDeviceIdentifiers = slaveDeviceIdentifiers;
        this.additionalCollectedDeviceInfo = new ArrayList<>();
        this.joinedSlaveDeviceIdentifiers = null;
        lostSlaveDeviceIdentifiers = null;
    }

    @Override
    public ResultType getResultType() {
        return ResultType.Supported;
    }

    @Override
    public List<Issue> getIssues() {
        if (issues == null) {
            issues = new ArrayList<>();
        }
        return issues;
    }

    @Override
    public void setFailureInformation(ResultType resultType, Issue issue) {
        getIssues().add(issue);
    }

    @Override
    public void setFailureInformation(ResultType resultType, List<Issue> issues) {
        getIssues().addAll(issues);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
        return comTask.isConfiguredToUpdateTopology();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.deviceIdentifier;
    }

    @Override
    public Map<DeviceIdentifier, ObservationDateProperty> getSlaveDeviceIdentifiers() {
        return slaveDeviceIdentifiers;
    }

    @Override
    public Map<DeviceIdentifier, ObservationDateProperty> getJoinedSlaveDeviceIdentifiers() {
        return this.joinedSlaveDeviceIdentifiers;
    }

    @Override
    public List<DeviceIdentifier> getLostSlaveDeviceIdentifiers() {
        return this.lostSlaveDeviceIdentifiers;
    }

    @Override
    public void addSlaveDevice(DeviceIdentifier slaveIdentifier) {
        slaveDeviceIdentifiers.put(slaveIdentifier, null);
    }

    @Override
    public void addSlaveDevice(DeviceIdentifier slaveIdentifier, ObservationDateProperty observationTimestampProperty) {
        slaveDeviceIdentifiers.put(slaveIdentifier, observationTimestampProperty);
    }

    @Override
    public void addJoinedSlaveDevice(DeviceIdentifier slaveIdentifier, ObservationDateProperty lastSeenDateInfo) {
        if (joinedSlaveDeviceIdentifiers == null) {
            joinedSlaveDeviceIdentifiers = new HashMap<>();
        }

        joinedSlaveDeviceIdentifiers.put(slaveIdentifier, lastSeenDateInfo);
    }

    @Override
    public void addLostSlaveDevice(DeviceIdentifier slaveIdentifier) {
        if (lostSlaveDeviceIdentifiers == null) {
            lostSlaveDeviceIdentifiers = new ArrayList<>();
        }

        lostSlaveDeviceIdentifiers.add(slaveIdentifier);
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
    public TopologyAction getTopologyAction() {
        return topologyAction;
    }

    @Override
    public void setTopologyAction(TopologyAction topologyAction) {
        this.topologyAction = topologyAction;
    }

    @Override
    public void addPathSegmentFor(DeviceIdentifier source, DeviceIdentifier target, DeviceIdentifier intermediateHop, Duration timeToLive, int cost) {
        topologyPathSegments.add(new TopologyPathSegment(source, target, intermediateHop, timeToLive, cost));
    }

    @Override
    public void addTopologyNeighbour(DeviceIdentifier neighbour, int modulationSchema, long toneMap, int modulation,
                                     int txGain, int txRes, int txCoeff, int lqi, int phaseDifferential, int tmrValidTime,
                                     int neighbourValidTime, long macPANId, String nodeAddress, int shortAddress, Date lastUpdate,
                                     Date lastPathRequest, int state, long roundTrip, int linkCost) {
        topologyNeighbours.add(
                new TopologyNeighbour(neighbour, modulationSchema, toneMap, modulation, txGain, txRes, txCoeff, lqi,
                        phaseDifferential, tmrValidTime, neighbourValidTime, macPANId, nodeAddress, shortAddress,
                        lastUpdate, lastPathRequest, state, roundTrip, linkCost)
        );
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