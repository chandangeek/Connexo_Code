package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.cpo.Environment;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.messaging.PartialLoadProfileMessageBuilder;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        String loadProfileXML= message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        Date fromDate = new Date(Long.parseLong(message.getDeviceMessageAttributes().get(1).getDeviceMessageAttributeValue()));
        Date toDate = new Date(Long.parseLong(message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue()));

        try {
            collectedMessage = readPartialProfileData(message, collectedMessage, loadProfileXML, fromDate, toDate);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private CollectedMessage readPartialProfileData(OfflineDeviceMessage message, CollectedMessage collectedMessage, String loadProfileXML, Date fromDate, Date toDate) throws CTRException {
        try {
            PartialLoadProfileMessageBuilder builder = PartialLoadProfileMessageBuilder.getInstance();
            builder = (PartialLoadProfileMessageBuilder) builder.fromXml(loadProfileXML);
            builder.setStartReadingTime(fromDate);
            builder.setEndReadingTime(toDate);

            LoadProfileReader lpr = builder.getLoadProfileReader();
            getProtocol().fetchLoadProfileConfiguration(Arrays.asList(lpr));
            List<CollectedLoadProfile> collectedLoadProfiles = getProtocol().getLoadProfileData(Arrays.asList(lpr));

            if (collectedLoadProfiles.size() == 0) {
                throw new CTRException("LoadProfile returned no data.");
            } else {
                for (CollectedLoadProfile collectedLoadProfile : collectedLoadProfiles) {
                    if (collectedLoadProfile.getIssues().get(0).isProblem() || collectedLoadProfile.getCollectedIntervalData().size() == 0) {
                        throw new CTRException("LoadProfile returned no interval data.");
                    }
                }
            }

            return createCollectedMessageWithCollectedLoadProfileData(message, collectedLoadProfiles.get(0));
        } catch (SAXException e) {
            String msg = "Failed to parse the content of the loadProfile XML.";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        } catch (IOException e) {
            String msg = "Failed while fetching the LoadProfile data.";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
    }
}
