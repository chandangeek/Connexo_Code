package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.AbstractObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.buffer.BufferParser;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.buffer.BufferReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.channel.CacheableChannelInfoReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.channel.ChannelInfoReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.channel.GenericChannelInfoReader;

import java.io.IOException;
import java.util.List;

public class GenericLoadProfileReader extends AbstractObisReader<CollectedLoadProfile, com.energyict.protocol.LoadProfileReader, ObisCode> implements LoadProfileReader {

    private final ChannelInfoReader channelInfoReader;
    private final BufferReader bufferReader;
    private final BufferParser bufferParser;
    private final IssueFactory issueFactory;
    private final CollectedDataFactory collectedDataFactory;

    public GenericLoadProfileReader(Matcher<ObisCode> matcher, IssueFactory issueFactory, CollectedDataFactory collectedDataFactory, ChannelInfoReader channelInfoReader, BufferReader bufferReader, BufferParser bufferParser) {
        super(matcher);
        this.channelInfoReader = channelInfoReader;
        this.bufferParser = bufferParser;
        this.bufferReader = bufferReader;
        this.issueFactory = issueFactory;
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public CollectedLoadProfile read(AbstractDlmsProtocol protocol, com.energyict.protocol.LoadProfileReader loadProfileReader) {
        CollectedLoadProfile collectedLoadProfile = collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode(), protocol.getOfflineDevice().getDeviceIdentifier()));
        List<ChannelInfo> channelInfos = channelInfoReader.getChannelInfo(loadProfileReader, protocol).getChannelInfos();
        ObisCode lpObisCode = loadProfileReader.getProfileObisCode();
        try {
            DataContainer dataContainer = bufferReader.read(protocol, super.map(loadProfileReader.getProfileObisCode()), loadProfileReader);
            collectedLoadProfile.setCollectedIntervalData(bufferParser.parse(dataContainer), channelInfos);
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
    public CollectedLoadProfileConfiguration getChannelInfo(com.energyict.protocol.LoadProfileReader lpr, AbstractDlmsProtocol protocol) {
        return channelInfoReader.getChannelInfo(lpr, protocol);
    }

}
