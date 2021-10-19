package com.energyict.protocolimplv2.dlms.idis.AS3000G.messages;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540MessageExecutor;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.util.Date;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.adHocEndOfBillingActivationDatedAttributeName;


public class AS3000GMessageExecutor extends AM540MessageExecutor {

    private static final ObisCode BILLING_DATE_CONFIGURATION = ObisCode.fromString("0.0.15.0.0.255");

    public AS3000GMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        CollectedMessage plcMessageResult = getPLCConfigurationDeviceMessageExecutor().executePendingMessage(pendingMessage, collectedMessage);
        if (plcMessageResult != null) {
            collectedMessage = plcMessageResult;
        } else { // if it was not a PLC message
            if (pendingMessage.getSpecification().equals(DeviceActionMessage.BillingDateConfiguration)) {
                collectedMessage = billingDateConfiguration(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER)) {
                executeImageTransferActions(pendingMessage);
            } else {
                collectedMessage = super.executeMessage(pendingMessage, collectedMessage);
            }
        }

        return collectedMessage;
    }

    private CollectedMessage billingDateConfiguration(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        try {
            String activationEpochString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, adHocEndOfBillingActivationDatedAttributeName).getValue();
            SingleActionSchedule adHocEndOfBilling = getCosemObjectFactory().getSingleActionSchedule(BILLING_DATE_CONFIGURATION);

            Array executionTime = convertEpochToDateTimeArray(activationEpochString);

            adHocEndOfBilling.writeExecutionTime(executionTime);

            String protocolInfo = activationEpochString;
            try {
                Date activationDate = new Date(Long.parseLong(activationEpochString));
                protocolInfo = activationDate.toString();
            } catch (Exception ex) {
                // swallow
            }

            collectedMessage.setDeviceProtocolInformation("Added a billing date configuration was set to " + protocolInfo);
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String errorMsg = "Failed to add a billing date configuration: " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, errorMsg));
        }
        return collectedMessage;
    }

}
