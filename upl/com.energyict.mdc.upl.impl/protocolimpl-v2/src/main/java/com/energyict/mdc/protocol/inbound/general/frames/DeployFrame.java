package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.CallHomeIdPlaceHolder;

/**
 * Copyrights EnergyICT
 * Date: 26/06/12
 * Time: 9:52
 * Author: khe
 */
public class DeployFrame extends AbstractInboundFrame {

    @Override
    protected FrameType getType() {
        return FrameType.DEPLOY;
    }

    public DeployFrame(String frame, CallHomeIdPlaceHolder callHomeIdPlaceHolder) {
        super(frame, callHomeIdPlaceHolder);
    }

    @Override
    public void doParse() {
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(getDeviceIdentifierByDialHomeIdPlaceHolder());
        String meterType = getInboundParameters().getMeterType();
        //TODO use info for topology

        deviceTopology.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(deviceTopology, "protocol.deploynotsupported", getInboundParameters().getSerialNumber()));
        getCollectedDatas().add(deviceTopology);
    }
}
