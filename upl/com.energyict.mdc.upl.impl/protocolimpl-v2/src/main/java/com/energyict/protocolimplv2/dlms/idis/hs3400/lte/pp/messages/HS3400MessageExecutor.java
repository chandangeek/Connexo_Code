package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.messages;

import com.energyict.dlms.cosem.GPRSModemSetup;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.hs3300.messages.HS3300MessageExecutor;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

import java.io.IOException;

public class HS3400MessageExecutor extends HS3300MessageExecutor {

    public HS3400MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, keyAccessorTypeExtractor, issueFactory);
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(DeviceActionMessage.ReadDLMSAttribute)) {
            collectedMessage = this.readDlmsAttribute(collectedMessage, pendingMessage);
        } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER)) {
            upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_LTE_APN_NAME)) {
            changeAPNName(pendingMessage);
        } else {    // Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    protected CollectedMessage changeAPNName(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        String lteAPN = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.apnAttributeName);

        if (lteAPN != null) {
            try {
                getCosemObjectFactory().getGPRSModemSetup(GPRSModemSetup.getDefaultObisCode()).writeAPN(lteAPN);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            } catch (IOException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, "Unable to execute the message to write " + GPRSModemSetup.getDefaultObisCode()
                        + "!"));
            }
        }
        return collectedMessage;
    }
}
