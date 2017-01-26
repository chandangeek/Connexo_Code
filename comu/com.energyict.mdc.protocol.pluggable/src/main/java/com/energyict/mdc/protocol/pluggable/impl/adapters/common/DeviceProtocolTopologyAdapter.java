package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.support.DeviceTopologySupport;

/**
 * Adapter between a {@link MeterProtocol MeterProtocol} or
 * {@link SmartMeterProtocol} and the {@link DeviceTopologySupport}
 * that do not by default support topology updates.
 *
 * @author gna
 * @since 5/04/12 - 11:23
 */
public class DeviceProtocolTopologyAdapter implements DeviceTopologySupport {

    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private DeviceIdentifier deviceIdentifier;

    public DeviceProtocolTopologyAdapter(IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super();
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
    }

    /**
     * Collect the actual Topology from a Device. If for some reason the Topology could not be fetched,
     * a proper {@link ResultType} <b>and</b> {@link com.energyict.mdc.upl.issue.Issue}
     * should be set so proper logging of this action can be performed.
     *
     * @return the current Topology
     */
    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(deviceIdentifier);
        deviceTopology.setFailureInformation(ResultType.NotSupported, getIssue(deviceIdentifier, MessageSeeds.DEVICE_TOPOLOGY_NOT_SUPPORTED_BY_ADAPTER));
        return deviceTopology;
    }

    private Issue getIssue(Object source, MessageSeed description, Object... arguments) {
        return this.issueService.newWarning(source, description, arguments);
    }

    /**
     * The used DeviceIdentifier.
     *
     * @param deviceIdentifier the used DeviceIdentifier
     */
    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

}