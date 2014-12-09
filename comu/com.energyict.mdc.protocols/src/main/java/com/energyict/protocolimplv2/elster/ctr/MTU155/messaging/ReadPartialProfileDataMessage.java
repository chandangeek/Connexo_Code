package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

/**
* Copyrights EnergyICT
* Date: 23/02/11
* Time: 9:33
*/
public class ReadPartialProfileDataMessage extends AbstractMTU155Message {


    public ReadPartialProfileDataMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().equals(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST.getPrimaryKey().getValue());
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String loadProfileXML= getDeviceMessageAttribute(message, DeviceMessageConstants.loadProfileAttributeName).getDeviceMessageAttributeValue();
        Date fromDate = new Date(Long.parseLong(getDeviceMessageAttribute(message, DeviceMessageConstants.fromDateAttributeName).getDeviceMessageAttributeValue()));
        Date toDate = new Date(Long.parseLong(getDeviceMessageAttribute(message, DeviceMessageConstants.toDateAttributeName).getDeviceMessageAttributeValue()));

        return readPartialProfileData(message, loadProfileXML, fromDate, toDate);
    }

    private CollectedMessage readPartialProfileData(OfflineDeviceMessage message, String loadProfileXML, Date fromDate, Date toDate) throws CTRException {
        try {
            LegacyPartialLoadProfileMessageBuilder builder = new LegacyPartialLoadProfileMessageBuilder();
            builder = (LegacyPartialLoadProfileMessageBuilder) builder.fromXml(loadProfileXML);
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
