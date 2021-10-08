package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile;

import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.AbstractObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration.ChannelInfoReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.data.BufferParser;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.data.BufferReader;

import java.io.IOException;
import java.util.List;

public class GenericLoadProfileReader<T extends AbstractDlmsProtocol> extends AbstractObisReader<CollectedLoadProfile, com.energyict.protocol.LoadProfileReader, ObisCode, T> implements LoadProfileReader<T> {

    protected final ChannelInfoReader channelInfoReader;
    protected final BufferReader bufferReader;
    protected final BufferParser bufferParser;
    protected final IssueFactory issueFactory;
    protected final CollectedDataFactory collectedDataFactory;

    public GenericLoadProfileReader(Matcher<ObisCode> matcher, IssueFactory issueFactory, CollectedDataFactory collectedDataFactory, ChannelInfoReader channelInfoReader, BufferReader bufferReader, BufferParser bufferParser) {
        super(matcher);
        this.bufferParser = bufferParser;
        this.bufferReader = bufferReader;
        this.channelInfoReader = channelInfoReader;
        this.issueFactory = issueFactory;
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public CollectedLoadProfile read(T protocol, com.energyict.protocol.LoadProfileReader loadProfileReader) {
        CollectedLoadProfile collectedLoadProfile = collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode(), protocol
                .getOfflineDevice()
                .getDeviceIdentifier()));
        ObisCode lpObisCode = loadProfileReader.getProfileObisCode();
        try {
            CollectedLoadProfileConfiguration collectedLoadProfileConfiguration = this.readConfiguration(protocol, loadProfileReader);
            DataContainer dataContainer = bufferReader.read(protocol, super.map(loadProfileReader.getProfileObisCode()), loadProfileReader);
            collectedLoadProfile.setCollectedIntervalData(bufferParser.parse(dataContainer, collectedLoadProfileConfiguration, loadProfileReader), collectedLoadProfileConfiguration.getChannelInfos());
            collectedLoadProfile.setDoStoreOlderValues(true);
        } catch (DataAccessResultException e) {
            // this can happen when the load profile is read twice in the same time window (day for daily lp), than the data block is not accessible. It could also happen when the load profile is not configured properly.
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                String message = String.join(" ", "Load profile was probably already read today, try modifying the 'last reading' date in the load profile properties.", e.getMessage());
                Issue problem = issueFactory.createWarning(loadProfileReader, "loadProfileXBlockingIssue", lpObisCode, message);
                collectedLoadProfile.setFailureInformation(ResultType.DataIncomplete, problem);
            }
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                Issue problem = issueFactory.createProblem(loadProfileReader, "loadProfileXBlockingIssue", lpObisCode, e.getMessage());
                collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
            }
        }
        return collectedLoadProfile;
    }

    @Override
    public CollectedLoadProfileConfiguration readConfiguration(T protocol, com.energyict.protocol.LoadProfileReader lpr) {
        try {
            CollectedLoadProfileConfiguration lpc = collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), protocol.getOfflineDevice()
                    .getDeviceIdentifier(), lpr.getMeterSerialNumber());
            ProfileGeneric pg = new ProfileGeneric(protocol.getDlmsSession(), new ObjectReference(DLMSUtils.findCosemObjectInObjectList(protocol.getDlmsSession()
                    .getMeterConfig()
                    .getInstantiatedObjectList(), lpr.getProfileObisCode()).getLNArray(), DLMSClassId.PROFILE_GENERIC.getClassId()));
            List<ChannelInfo> channelInfos = this.channelInfoReader.getChannelInfo(protocol, lpr, pg);
            lpc.setSupportedByMeter(true);
            lpc.setChannelInfos(channelInfos);
            lpc.setProfileInterval(pg.getCapturePeriod());
            return lpc;
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
        }
    }

}
