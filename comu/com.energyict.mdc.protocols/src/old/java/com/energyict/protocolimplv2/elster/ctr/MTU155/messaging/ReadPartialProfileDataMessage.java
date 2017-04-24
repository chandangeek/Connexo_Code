/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;

import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ReadPartialProfileDataMessage extends AbstractMTU155Message {

    private final Clock clock;
    private final TopologyService topologyService;
    private final LoadProfileFactory loadProfileFactory;

    public ReadPartialProfileDataMessage(Messaging messaging, Clock clock, IssueService issueService, TopologyService topologyService, CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory) {
        super(messaging, issueService, collectedDataFactory);
        this.clock = clock;
        this.topologyService = topologyService;
        this.loadProfileFactory = loadProfileFactory;
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String loadProfileXML= getDeviceMessageAttribute(message, DeviceMessageConstants.loadProfileAttributeName).getDeviceMessageAttributeValue();
        Instant fromDate = Instant.ofEpochMilli(Long.parseLong(getDeviceMessageAttribute(message, DeviceMessageConstants.fromDateAttributeName).getDeviceMessageAttributeValue()));
        Instant toDate = Instant.ofEpochMilli(Long.parseLong(getDeviceMessageAttribute(message, DeviceMessageConstants.toDateAttributeName).getDeviceMessageAttributeValue()));
        return readPartialProfileData(message, loadProfileXML, fromDate, toDate);
    }

    private CollectedMessage readPartialProfileData(OfflineDeviceMessage message, String loadProfileXML, Instant fromDate, Instant toDate) throws CTRException {
        try {
            LegacyPartialLoadProfileMessageBuilder builder = new LegacyPartialLoadProfileMessageBuilder(this.clock, this.topologyService, loadProfileFactory);
            builder.fromXml(loadProfileXML);
            builder.setStartReadingTime(fromDate);
            builder.setEndReadingTime(toDate);

            LoadProfileReader lpr = builder.getLoadProfileReader();
            getProtocol().fetchLoadProfileConfiguration(Arrays.asList(lpr));
            List<CollectedLoadProfile> collectedLoadProfiles = getProtocol().getLoadProfileData(Arrays.asList(lpr));
            if (!collectedLoadProfiles.isEmpty()) {
                CollectedLoadProfile collectedLoadProfile = collectedLoadProfiles.get(0);
                if (collectedLoadProfile.getResultType().ordinal() != ResultType.Supported.ordinal()) {
                    throw new CTRException(getFailureInformation(collectedLoadProfile));
                }
                return createCollectedMessageWithCollectedLoadProfileData(message, collectedLoadProfile);
            } else {
                throw new CTRException("Failed to fetch the LoadProfile data - Protocol did not return a proper CollectedLoadProfile.");
            }
        } catch (CTRException e) {
            throw e;
        } catch (SAXException | IOException e) {
            String msg = "Failed to parse the content of the loadProfile XML.";
            throw new CTRException(msg);
        }
    }

    private String getFailureInformation(CollectedLoadProfile collectedLoadProfile) {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("Failed to fetch the LoadProfile data");
        if (!collectedLoadProfile.getIssues().isEmpty()) {
            msgBuilder.append(" - ");
            msgBuilder.append("Issues: ");
            Iterator<Issue> iterator = collectedLoadProfile.getIssues().iterator();
            while (iterator.hasNext()) {
                Issue issue = iterator.next();
                msgBuilder.append(issue.getDescription());
                if (iterator.hasNext()) {
                    msgBuilder.append(", ");
                }
            }
        }
        return msgBuilder.toString();
    }
}
