package com.energyict.protocols.mdc.inbound.general.frames;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.services.IdentificationService;

/**
 * Copyrights EnergyICT
 * Date: 26/06/12
 * Time: 9:52
 * Author: khe
 */
public class DeployFrame extends AbstractInboundFrame {

    private final CollectedDataFactory collectedDataFactory;

    @Override
    protected FrameType getType() {
        return FrameType.DEPLOY;
    }

    public DeployFrame(String frame, IssueService issueService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory) {
        super(frame, issueService, identificationService);
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public void doParse() {
        CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(getDeviceIdentifier());
        String meterType = getInboundParameters().getMeterType();
        //TODO use info for topology

        deviceTopology.setFailureInformation(
                ResultType.InCompatible,
                this.getIssueService().newIssueCollector()
                    .addWarning(deviceTopology, "protocol.deploynotsupported", getInboundParameters().getSerialNumber()));
        getCollectedDatas().add(deviceTopology);
    }

}