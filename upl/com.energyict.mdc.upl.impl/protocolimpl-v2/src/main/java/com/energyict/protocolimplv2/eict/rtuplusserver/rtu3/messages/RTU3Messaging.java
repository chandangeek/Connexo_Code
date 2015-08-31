package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.RTU3;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.firmwareobjects.BroadcastUpgrade;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.firmwareobjects.DeviceInfoSerializer;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects.MasterDataSerializer;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects.MasterDataSync;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 22/06/2015 - 9:53
 */
public class RTU3Messaging extends AbstractMessageExecutor implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;
    private static final String TEMP_DIR = "java.io.tmpdir";

    static {
        supportedMessages = new ArrayList<>();
        supportedMessages.add(DeviceActionMessage.SyncMasterdataForDC);
        supportedMessages.add(DeviceActionMessage.SyncDeviceDataForDC);
        supportedMessages.add(DeviceActionMessage.PauseDCScheduler);
        supportedMessages.add(DeviceActionMessage.ResumeDCScheduler);
        supportedMessages.add(FirmwareDeviceMessage.BroadcastFirmwareUpgrade);
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER);
    }

    private MasterDataSync masterDataSync;

    public RTU3Messaging(RTU3 protocol) {
        super(protocol);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.dcDeviceIDAttributeName)) {
            return MasterDataSerializer.serializeMasterData(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.dcDeviceID2AttributeName)) {
            return MasterDataSerializer.serializeMeterDetails(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastEncryptionKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.broadcastAuthenticationKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastDevicesGroupAttributeName)) {
            return DeviceInfoSerializer.serializeDeviceInfo(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastFirmwareUpdateImageIdentifierAttributeName)) {
            return new Base64EncoderDecoder().encode(((UserFile) messageAttribute).loadFileInByteArray());  //Base64 string representing the byte array
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastInitialTimeBetweenBlocksAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getMilliSeconds()); //Return value in ms
        } else if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateUserFileAttributeName)) {

            //TODO stream the blobcontent to a file instead of fully loading it in memory (see userfile impl)

            final UserFile userFile = (UserFile) messageAttribute;
            final byte[] bytes = userFile.loadFileInByteArray();
            final String fileName = System.getProperty(TEMP_DIR) + userFile.getFileName() + "_" + userFile.getId();

            final File testFile = new File(fileName);
            if (testFile.exists()) {
                if (testFile.length() != bytes.length) {
                    if (testFile.delete()) {
                        ProtocolTools.writeBytesToFile(fileName, bytes, false);
                    } else {
                        throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(new IOException("Could not write file '" + userFile.getFileName() + "' to temp directory, it already exists but is still in use"));
                    }
                }   //Else: file exists and has the correct content. Ok, continue to execute message.
            } else {
                ProtocolTools.writeBytesToFile(fileName, bytes, false);
            }
            System.gc();    //Remove the in memory byte array
            return fileName;
        } else {
            return messageAttribute.toString();
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncMasterdataForDC)) {
                    collectedMessage = getMasterDataSync().syncMasterData(pendingMessage, collectedMessage);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncDeviceDataForDC)) {
                    collectedMessage = getMasterDataSync().syncDeviceData(pendingMessage, collectedMessage);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.PauseDCScheduler)) {
                    setSchedulerState(SchedulerState.PAUSED);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ResumeDCScheduler)) {
                    setSchedulerState(SchedulerState.RUNNING);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.BroadcastFirmwareUpgrade)) {
                    collectedMessage = new BroadcastUpgrade(this).broadcastFirmware(pendingMessage, collectedMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER)) {
                    upgradeFirmware(pendingMessage);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException | NullPointerException | ApplicationException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.toString());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            } finally {
                result.addCollectedMessage(collectedMessage);
            }
        }

        return result;
    }

    private MasterDataSync getMasterDataSync() {
        if (masterDataSync == null) {
            masterDataSync = new MasterDataSync(this);
        }
        return masterDataSync;
    }

    private void setSchedulerState(SchedulerState state) throws IOException {
        getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager().writeSchedulerState(state.toDLMSEnum());
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {

        //TODO no, not userfile in memory!!
        String userFile = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getDeviceMessageAttributeValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue(); // Will return empty string if the MessageAttribute could not be found
        byte[] image = ProtocolTools.getBytesFromHexString(userFile, "");

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();

        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(30);
        it.setDelayBeforeSendingBlocks(5000);
        it.upgrade(image, false, imageIdentifier, true);

        try {
            it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
            it.imageActivation();
        } catch (DataAccessResultException e) {
            if (isTemporaryFailure(e)) {
                getProtocol().getLogger().log(Level.INFO, "Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
            } else {
                throw e;
            }
        }
    }

    private boolean isTemporaryFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
    }
}