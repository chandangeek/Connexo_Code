package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.loadprofile;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.AnyObisMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.GenericLoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.data.SelectiveBufferReader;

import java.io.IOException;
import java.util.List;

public class ActarisSl7000LoadProfileReader extends GenericLoadProfileReader<ActarisSl7000> {

    /**
     * Load profiling parameters objects, holding time parameters for the load profiling management
     */
    private static final ObisCode LOADPROFILING_PARAMETERS_OBIS = ObisCode.fromString("0.0.136.0.1.255");


    public ActarisSl7000LoadProfileReader(IssueFactory issueFactory, CollectedDataFactory collectedDataFactory, int limitMaxNrOfDays, AbstractDlmsProtocol dlmsProtocol) {
        super(new AnyObisMatcher(), issueFactory, collectedDataFactory, new ActarisSl7000ChannelReader()
                , new SelectiveBufferReader(limitMaxNrOfDays), new ActarisSl7000BufferParser(dlmsProtocol));
    }

    @Override
    public CollectedLoadProfileConfiguration readConfiguration(ActarisSl7000 protocol, com.energyict.protocol.LoadProfileReader lpr) {
        try {
            CollectedLoadProfileConfiguration lpc = collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), protocol.getOfflineDevice().getDeviceIdentifier(), lpr.getMeterSerialNumber());
            ProfileGeneric pg = new ProfileGeneric(protocol.getDlmsSession(), new ObjectReference(new UniversalObject(lpr.getProfileObisCode(), DLMSClassId.PROFILE_GENERIC).getLNArray(), DLMSClassId.PROFILE_GENERIC.getClassId()));
            List<ChannelInfo> channelInfos = this.channelInfoReader.getChannelInfo(protocol, lpr, pg);
            lpc.setSupportedByMeter(true);
            lpc.setChannelInfos(channelInfos);
            Data loadProfilingParameters = protocol.getDlmsSession().getCosemObjectFactory().getData(LOADPROFILING_PARAMETERS_OBIS);
            DataContainer dataContainer = loadProfilingParameters.getDataContainer();
            lpc.setProfileInterval(dataContainer.getRoot().getInteger(0) * 60);
            return lpc;
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
        }
    }

}
