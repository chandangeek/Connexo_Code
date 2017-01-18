package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessageExecutor;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.DSMR50Properties;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;

/**
 * @author sva
 * @since 6/01/2015 - 9:58
 */
public class AM540MessageExecutor extends Dsmr50MessageExecutor {

    public static final ObisCode RELAY_CONTROL_DEFAULT_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode PLC_G3_TIMEOUT_OBISCODE = ObisCode.fromString("0.0.94.33.10.255");
    private AbstractMessageExecutor dsmr50MessageExecutor;
    private AbstractMessageExecutor mbusMessageExecutor;
    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor;

    public AM540MessageExecutor(AbstractDlmsProtocol protocol, Clock clock, TopologyService topologyService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory) {
        super(protocol, clock, topologyService, issueService, readingTypeUtilService, collectedDataFactory, loadProfileFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> dsmr40Messages = new ArrayList<>();
        List<OfflineDeviceMessage> mbusMessages = new ArrayList<>();

        // First try to execute all G3 PLC related messages
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = null;
            int mBusChannelId = getProtocol().getPhysicalAddressFromSerialNumber(pendingMessage.getDeviceSerialNumber());
            if (mBusChannelId > 0) {
                mbusMessages.add(pendingMessage);
            } else {
                collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
                try {
                    boolean messageExecuted = getPLCConfigurationDeviceMessageExecutor().executePendingMessage(pendingMessage, collectedMessage);
                    if (!messageExecuted) { // if it was not a PLC message
                        if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.CONTACTOR_CLOSE_RELAY)) {
                            closeRelay(pendingMessage);
                        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.CONTACTOR_OPEN_RELAY)) {
                            openRelay(pendingMessage);
                        } else {
                            collectedMessage = null;
                            dsmr40Messages.add(pendingMessage); // These messages are not specific for AM540, but can be executed by the super (= Dsmr 4.0) messageExecutor
                        }
                    }
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                        collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    }   //Else: throw communication exception
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    if (collectedMessage == null) {
                        collectedMessage = getCollectedDataFactory().createCollectedMessage(pendingMessage.getIdentifier());
                    }
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            }
            if (collectedMessage != null) {
                result.addCollectedMessages(collectedMessage);
            }
        }

        // Then delegate all other messages to the Dsmr 5.0 message executor
        getDsmr50MessageExecutor().executePendingMessages(dsmr40Messages).getCollectedMessages()
                .stream().forEach(result::addCollectedMessages);

        // Then delegate all MBus related messages to the Mbus message executor
        getMbusMessageExecutor().executePendingMessages(mbusMessages).getCollectedMessages()
                .stream().forEach(result::addCollectedMessages);
        return result;
    }

    private void openRelay(OfflineDeviceMessage pendingMessage) throws IOException {
        int relayNumber = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode obisCode = ProtocolTools.setObisCodeField(RELAY_CONTROL_DEFAULT_OBISCODE, 1, (byte) relayNumber);
        getCosemObjectFactory().getDisconnector(obisCode).remoteDisconnect();
    }

    private void closeRelay(OfflineDeviceMessage pendingMessage) throws IOException {
        int relayNumber = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode obisCode = ProtocolTools.setObisCodeField(RELAY_CONTROL_DEFAULT_OBISCODE, 1, (byte) relayNumber);
        getCosemObjectFactory().getDisconnector(obisCode).remoteReconnect();
    }

    @Override
    protected void upgradeFirmwareWithActivationDateAndImageIdentifier(OfflineDeviceMessage pendingMessage) throws IOException {
        String path = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getDeviceMessageAttributeValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getDeviceMessageAttributeValue();   // Will return empty string if the MessageAttribute could not be found
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue(); // Will return empty string if the MessageAttribute could not be found

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();

        try (RandomAccessFile file = new RandomAccessFile(new File(path), "r")) {

            if (imageIdentifier.isEmpty()) {
                imageIdentifier = getImageIdentifierFromFile(file);
            }

            if (isResume(pendingMessage)) {
                int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
                if (lastTransferredBlockNumber > 0) {
                    it.setStartIndex(lastTransferredBlockNumber - 1);
                }
            }

            it.setBooleanValue(getBooleanValue());
            it.setUsePollingVerifyAndActivate(true);    //Poll verification
            it.setPollingDelay(10000);
            it.setPollingRetries(30);
            it.setDelayBeforeSendingBlocks(5000);
            it.setCheckNumberOfBlocksInPreviousSession(((DSMR50Properties) getProtocol().getDlmsSession().getProperties()).getCheckNumberOfBlocksDuringFirmwareResume());
            if (imageIdentifier.isEmpty()) {
                it.upgrade(new ImageTransfer.RandomAccessFileImageBlockSupplier(file), false, ImageTransfer.DEFAULT_IMAGE_NAME, false);
            } else {
                it.upgrade(new ImageTransfer.RandomAccessFileImageBlockSupplier(file), false, imageIdentifier, false);
            }
        }

        if (activationDate.isEmpty()) {
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
        } else {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            Array dateArray = convertActivationDateEpochToDateTimeArray(activationDate);
            sas.writeExecutionTime(dateArray);
        }
    }

    private String getImageIdentifierFromFile(RandomAccessFile file) throws IOException {
        file.seek(0);
        byte[] fileStart = new byte[1024];
        file.readFully(fileStart);
        file.seek(0);

        int first = -1;
        int last = -1;
        for (int i = 0; i < fileStart.length; i++)
            if (first == -1) {
                if (isAsciiPrintable((char) (fileStart[i] & 0xFF))) {
                    first = i;
                }
            } else {
                if (!isAsciiPrintable((char) (fileStart[i] & 0xFF))) {
                    last = i;
                    i = fileStart.length;
                }
            }
        if (first == -1) {
            return "";
        } else if (last == -1) {
            last = fileStart.length;
        }
        return new String(fileStart, first, last - first);
    }

    /**
     * <p>Checks whether the character is ASCII 7 bit printable.</p>
     * <p>
     * <pre>
     *   CharUtils.isAsciiPrintable('a')  = true
     *   CharUtils.isAsciiPrintable('A')  = true
     *   CharUtils.isAsciiPrintable('3')  = true
     *   CharUtils.isAsciiPrintable('-')  = true
     *   CharUtils.isAsciiPrintable('\n') = false
     *   CharUtils.isAsciiPrintable('&copy;') = false
     * </pre>
     *
     * @param ch the character to check
     * @return true if between 32 and 126 inclusive
     */
    private boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    private PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new PLCConfigurationDeviceMessageExecutor(getProtocol().getDlmsSession(), getProtocol().getOfflineDevice(), getCollectedDataFactory(), getClock(), getIssueService());
        }
        return plcConfigurationDeviceMessageExecutor;
    }

    public AbstractMessageExecutor getDsmr50MessageExecutor() {
        if (dsmr50MessageExecutor == null) {
            dsmr50MessageExecutor = new Dsmr50MessageExecutor(getProtocol(), getClock(), getTopologyService(), getIssueService(), getReadingTypeUtilService(), getCollectedDataFactory(), getLoadProfileFactory());
        }
        return dsmr50MessageExecutor;
    }

    public AbstractMessageExecutor getMbusMessageExecutor() {
        if (mbusMessageExecutor == null) {
            mbusMessageExecutor = new IDISMBusMessageExecutor(getProtocol(), getIssueService(), getReadingTypeUtilService(), getCollectedDataFactory());
        }
        return mbusMessageExecutor;
    }
}