package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.messages;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.GPRSModemSetup;
import com.energyict.dlms.cosem.PPPSetup;
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
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_PPP_AUTHENTICATION_PAP)) {
            changePPPAuthenticationPAP(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_PPP_AUTHENTICATION_PAP_TO_NULL)) {
            changePPPAuthenticationToNull(pendingMessage);
        } else {    // Unsupported message
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
            }
            return collectedMessage;
    }

    private CollectedMessage changePPPAuthenticationToNull(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        PPPSetup pppSetup = getCosemObjectFactory().getPPPSetup();

        Structure papAuth = new Structure();
        papAuth.addDataType( OctetString.fromString("") );
        papAuth.addDataType( OctetString.fromString("") );
        pppSetup.writePPPAuthenticationType(papAuth);

        Array lcpOptions = new Array();
        Structure authProtocol = new Structure();
        authProtocol.addDataType( new Unsigned8(3) ); // 3 = Authentication-Protocol
        authProtocol.addDataType( new Unsigned8(4) ); // length of all 3 fields
        authProtocol.addDataType( new Unsigned16(49187) ); // 0xc023 = PAP protocol
        lcpOptions.addDataType(authProtocol);

        try {
            pppSetup.writePPPAuthenticationType(papAuth);
            pppSetup.writeLCPOptions(lcpOptions);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
            collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, "Unable to execute the message to write " + GPRSModemSetup.getDefaultObisCode() + "!"));
        }

        return collectedMessage;
    }

    private CollectedMessage changePPPAuthenticationPAP(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        String userName = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.usernamePPPAuth);
        String password = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.passwordPPPAuth);
        PPPSetup pppSetup = getCosemObjectFactory().getPPPSetup();

        Structure papAuth = new Structure();
        papAuth.addDataType( OctetString.fromString(userName) );
        papAuth.addDataType( OctetString.fromString(password) );
        pppSetup.writePPPAuthenticationType(papAuth);

        Array lcpOptions = new Array();
        Structure authProtocol = new Structure();
        authProtocol.addDataType( new Unsigned8(3) ); // 3 = Authentication-Protocol
        authProtocol.addDataType( new Unsigned8(4) ); // length of all 3 fields
        authProtocol.addDataType( new Unsigned16(49187) ); // 0xc023 = PAP protocol
        lcpOptions.addDataType(authProtocol);

        try {
            pppSetup.writePPPAuthenticationType(papAuth);
            pppSetup.writeLCPOptions(lcpOptions);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
            collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, "Unable to execute the message to write " + GPRSModemSetup.getDefaultObisCode() + "!"));
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
