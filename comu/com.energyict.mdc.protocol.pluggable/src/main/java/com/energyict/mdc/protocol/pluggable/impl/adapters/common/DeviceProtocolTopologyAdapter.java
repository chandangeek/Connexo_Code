package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceTopologySupport;

import java.util.List;

/**
 * Adapter between a {@link MeterProtocol MeterProtocol} or
 * {@link SmartMeterProtocol} and the {@link DeviceTopologySupport}.
 * A <code>MeterProtocol</code> by default does not support topology updates ...
 *
 * @author gna
 * @since 5/04/12 - 11:23
 */
public class DeviceProtocolTopologyAdapter implements DeviceTopologySupport {

    private final IssueService issueService;
    private DeviceIdentifier deviceIdentifier;

    public DeviceProtocolTopologyAdapter(IssueService issueService) {
        super();
        this.issueService = issueService;
    }

    /**
     * Collect the actual Topology from a Device. If for some reason the Topology could not be fetched,
     * a proper {@link ResultType} <b>and</b> {@link com.energyict.mdc.issues.Issue}
     * should be set so proper logging of this action can be performed.
     *
     * @return the current Topology
     */
    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = this.getCollectedDataFactory().createCollectedTopology(deviceIdentifier);
        deviceTopology.setFailureInformation(ResultType.NotSupported, getIssue(deviceIdentifier.findDevice(), "devicetopologynotsupported"));
        return deviceTopology;
    }

    private Issue getIssue(Object source, String description, Object... arguments){
        return this.issueService.newProblem(
                source,
                Environment.DEFAULT.get().getTranslation(description).replaceAll("'", "''"),
                arguments);
    }

    /**
     * The used DeviceIdentifier.
     *
     * @param deviceIdentifier the used DeviceIdentifier
     */
    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

}