package com.energyict.protocolimplv2.dlms.a1860.messages;

import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a1860.A1860;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.convertor.AbstractMessageConverter;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;

public class A1860MessageExecutor extends AbstractMessageExecutor {

    public A1860MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                collectedMessage = executeMessage(pendingMessage, collectedMessage);
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            } catch (Exception e) {
                //in case we get an exception and we did not managed to put the collected message to failed, we will do it here
                if (!collectedMessage.getNewDeviceMessageStatus().equals(DeviceMessageStatus.FAILED)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }
                throw e;
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST)) {
            collectedMessage = partialLoadProfileRequest(pendingMessage);    //This message returns a result
        } else {    // Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private CollectedMessage partialLoadProfileRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        try {
            final String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getValue();
            final String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getValue();
            final String toDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, toDateAttributeName).getValue();
            final Date fromDate = new Date(Long.parseLong(fromDateEpoch));
            final Date toDate = new Date(Long.parseLong(toDateEpoch));
            final SimpleDateFormat formatter = AbstractMessageConverter.dateTimeFormatWithTimeZone;
            final A1860 protocol = (A1860) getProtocol();

            final String fullLoadProfileContent = LoadProfileMessageUtils.createPartialLoadProfileMessage(
                    "PartialLoadProfile", formatter.format(fromDate), formatter.format(toDate), loadProfileContent
            );

            final LegacyPartialLoadProfileMessageBuilder builder = LegacyPartialLoadProfileMessageBuilder.fromXml(fullLoadProfileContent);

            final LoadProfileReader lpr = builder.getLoadProfileReader();  // Does not contain the correct from & to date yet, they were stored in separate attributes
            final LoadProfileReader fullLpr = new LoadProfileReader(lpr.getProfileObisCode(), fromDate, toDate, lpr.getLoadProfileId(), lpr.getMeterSerialNumber(), lpr.getChannelInfos());

            List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = protocol.fetchLoadProfileConfiguration(Collections.singletonList(fullLpr));
            for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
                if (!config.isSupportedByMeter()) {   //LP not supported
                    CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    String errorMessage = "Load profile with obiscode " + config.getObisCode() + " is not supported by the device";
                    collectedMessage.setDeviceProtocolInformation(errorMessage);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, errorMessage));
                    getProtocol().journal(errorMessage);
                    return collectedMessage;
                }
            }

            List<CollectedLoadProfile> loadProfileData = protocol.getLoadProfileDataReader()
                    .getLoadProfileData(Collections.singletonList(fullLpr), true);
            CollectedMessage collectedMessage = createCollectedMessageWithLoadProfileData(pendingMessage, loadProfileData.get(0), fullLpr);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (SAXException e) {  // Failed to parse XML data
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(e.getLocalizedMessage());
            getProtocol().journal(e.getLocalizedMessage());
            return collectedMessage;
        }
    }

}
