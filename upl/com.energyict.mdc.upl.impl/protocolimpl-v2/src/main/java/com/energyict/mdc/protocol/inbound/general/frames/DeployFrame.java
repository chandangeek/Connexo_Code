package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.identifiers.CallHomeIdPlaceHolder;

/**
 * Copyrights EnergyICT
 * Date: 26/06/12
 * Time: 9:52
 * Author: khe
 */
public class DeployFrame extends AbstractInboundFrame {

    public DeployFrame(String frame, CallHomeIdPlaceHolder callHomeIdPlaceHolder, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(frame, callHomeIdPlaceHolder, collectedDataFactory, issueFactory);
    }

    @Override
    protected FrameType getType() {
        return FrameType.DEPLOY;
    }

    @Override
    public void doParse() {
        CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(getDeviceIdentifierByDialHomeIdPlaceHolder());
        String meterType = getInboundParameters().getMeterType();
        //TODO use info for topology

        deviceTopology.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(deviceTopology, "protocol.deploynotsupported", getInboundParameters().getSerialNumber()));
        getCollectedDatas().add(deviceTopology);
    }
}
