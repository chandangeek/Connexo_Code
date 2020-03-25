package com.energyict.protocolimplv2.dlms.acud.messages;

import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ChargeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareImageType;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;

public class AcudMessageExecutor extends AbstractMessageExecutor {

    public AcudMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            try {
                collectedMessage = executeMessage(pendingMessage, collectedMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
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
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_KDL_AND_HASH_AND_ACTIVATION)) {
            upgradeFirmware(pendingMessage);
            // clock
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.ACTIVATE_PASSIVE_UNIT_CHARGE)) {
            activatePassiveUnitCharge(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION)) {
            changePassiveUnitChargeWithActivation(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION_DATE)) {
            changePassiveUnitChargeWithActivationDate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.UPDATE_UNIT_CHARGE)) {
            updateUnitCharge(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_CHARGE_PERIOD)) {
            changeChargePeriod(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_CHARGE_PROPORTION)) {
            changeChargePropertion(pendingMessage);
        } else {   //Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private void activatePassiveUnitCharge(OfflineDeviceMessage pendingMessage){
    }

    private void changePassiveUnitChargeWithActivation(OfflineDeviceMessage pendingMessage){
    }

    private void changePassiveUnitChargeWithActivationDate(OfflineDeviceMessage pendingMessage){
    }

    private void updateUnitCharge(OfflineDeviceMessage pendingMessage){
    }

    private void changeChargePeriod(OfflineDeviceMessage pendingMessage){
    }

    private void changeChargePropertion(OfflineDeviceMessage pendingMessage){
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String hexUserFileContent = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateFileAttributeName);
        String activationEpochString = getDeviceMessageAttributeValue(pendingMessage, firmwareUpdateActivationDateAttributeName);
        String hash = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateHashAttributeName);
        String kdl = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateKDLAttributeName);
        String type = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateImageTypeAttributeName);
        byte[] imageData = ProtocolTools.getBytesFromHexString(hexUserFileContent, "");
        byte[] typeBytes = FirmwareImageType.typeForDescription(type).getByteArray();
        byte[] hashBytes = ProtocolTools.getBytesFromHexString(hash, 2);
        byte[] kdlBytes = ProtocolTools.getBytesFromHexString(kdl, 2);
        byte[] dateBytes = new AXDRDateTime(Long.parseLong(activationEpochString), getProtocol().getTimeZone()).getCosemDate().toBytes();
        byte[] initializationBytes = ProtocolTools.concatByteArrays(kdlBytes, hashBytes, dateBytes, typeBytes);
        String imageIdentifier = new String(initializationBytes, StandardCharsets.ISO_8859_1);

        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
        imageTransfer.setCharSet(StandardCharsets.ISO_8859_1);
        imageTransfer.setCheckNumberOfBlocksInPreviousSession(false);
        imageTransfer.setTransferBlocks(true);
        List<ImageTransfer.ImageToActivateInfo> imageToActivateInfos = null;
        String imageIdentifierInDevice = "";
        try {
            imageToActivateInfos = imageTransfer.readImageToActivateInfo();
            imageIdentifierInDevice = imageToActivateInfos.get(0).getImageIdentifier();
        } catch (DataAccessResultException e) {
            // swallow, this happens when a device has never been upgraded
        }
        int lastTransferredBlockNumber = imageTransfer.readFirstNotTransferedBlockNumber().intValue();
        if (lastTransferredBlockNumber > 0 && imageIdentifier.equalsIgnoreCase(imageIdentifierInDevice)) {
            imageTransfer.setStartIndex(lastTransferredBlockNumber - 1);
        }
        ImageTransfer.ImageBlockSupplier dataSupplier = new ImageTransfer.ByteArrayImageBlockSupplier(imageData);
        imageTransfer.enableImageTransfer(dataSupplier, imageIdentifier);
        imageTransfer.initializeAndTransferBlocks(dataSupplier, false, imageIdentifier);
        if (imageTransfer.getImageTransferStatus().getValue() == 1) {
            imageTransfer.checkAndSendMissingBlocks();
        }
//        The device will start verification and activation wil be done on the date that is specified in the imageIdentifier.
    }
}
