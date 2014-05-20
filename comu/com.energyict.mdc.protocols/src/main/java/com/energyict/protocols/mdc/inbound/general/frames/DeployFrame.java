package com.energyict.protocols.mdc.inbound.general.frames;

import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.SerialNumberPlaceHolder;
import com.energyict.protocols.mdc.services.impl.Bus;

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
        CollectedTopology deviceTopology = this.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierBySerialNumber("CORRECT_MY_SERIALNUMBER"));
        String meterType = getInboundParameters().getMeterType();
        //TODO use info for topology

        deviceTopology.setFailureInformation(
                ResultType.InCompatible,
                Bus.getIssueService().newIssueCollector()
                    .addWarning(deviceTopology, "protocol.deploynotsupported", getInboundParameters().getSerialNumber()));
        getCollectedDatas().add(deviceTopology);
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

}
