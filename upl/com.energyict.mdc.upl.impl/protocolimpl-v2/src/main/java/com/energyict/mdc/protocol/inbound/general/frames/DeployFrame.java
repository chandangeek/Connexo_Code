package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.SerialNumberPlaceHolder;

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

    public DeployFrame(String frame, SerialNumberPlaceHolder serialNumberPlaceHolder) {
        super(frame, serialNumberPlaceHolder);
    }

    @Override
    public void doParse() {
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierBySerialNumber("CORRECT_MY_SERIALNUMBER"));
        String meterType = getInboundParameters().getMeterType();
        //TODO use info for topology

        deviceTopology.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(deviceTopology, "protocol.deploynotsupported", getInboundParameters().getSerialNumber()));
        getCollectedDatas().add(deviceTopology);
    }
}
