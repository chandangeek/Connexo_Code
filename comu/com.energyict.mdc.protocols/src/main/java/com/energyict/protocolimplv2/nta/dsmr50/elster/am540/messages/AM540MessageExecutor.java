package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.DSMR50Properties;

import java.io.IOException;
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

    private static final ObisCode PLC_G3_TIMEOUT_OBISCODE = ObisCode.fromString("0.0.94.33.10.255");
    public static final ObisCode RELAY_CONTROL_DEFAULT_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");

    private AbstractMessageExecutor dsmr50MessageExecutor;
    private AbstractMessageExecutor mbusMessageExecutor;

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
                    if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_TMR_TTL)) {
                        setTMRTTL(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES)) {
                        setMaxFrameRetries(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL)) {
                        setNeighbourTableEntryTTL(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE)) {
                        setHighPriorityWindowSize(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT)) {
                        setCSMAFairnessLimit(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH)) {
                        setBeaconRandomizationWindowLength(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_A)) {
                        setMacA(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_K)) {
                        setMacK(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS)) {
                        setMinimumCWAttempts(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_BE)) {
                        setMaxBe(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF)) {
                        setMaxCSMABackOff(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_BE)) {
                        setMinBe(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME)) {
                        setMaxNumberOfHops(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME)) {
                        setWeakLQIValue(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_SECURITY_LEVEL)) {
                        setSecurityLevelpendingMessage(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION)) {
                        setRoutingConfiguration(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_BROAD_CAST_LOG_TABLE_ENTRY_TTL)) {
                        setBroadCastLogTableEntryTTL(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME)) {
                        setMaxJoinWaitTime(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME)) {
                        setPathDiscoveryTime(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_METRIC_TYPE)) {
                        setMetricType(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_COORD_SHORT_ADDRESS)) {
                        setCoordShortAddress(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_DISABLE_DEFAULT_ROUTING)) {
                        setDisableDefaultRouting(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_DEVICE_TYPE)) {
                        setDeviceType(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_WRITE_PLC_G3_TIMEOUT)) {
                        writePlcG3Timeout(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_CLOSE_RELAY)) {
                        closeRelay(pendingMessage);
                    } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_OPEN_RELAY)) {
                        openRelay(pendingMessage);
                    } else {
                        collectedMessage = null;
                        dsmr40Messages.add(pendingMessage); // These messages are not specific for AM540, but can be executed by the super (= Dsmr 4.0) messageExecutor
                    }
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                        collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    }   //Else: throw communication exception
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
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
        super.executePendingMessages(dsmr40Messages).getCollectedMessages()
                .stream()
                .forEach(result::addCollectedMessages);

//        // Then delegate all MBus related messages to the Mbus message executor
//        getMbusMessageExecutor().executePendingMessages(mbusMessages).getCollectedMessages()
//                .stream()
//                .forEach(result::addCollectedMessages);

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

    private void setTMRTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeTMRTTL(getIntegerAttribute(pendingMessage));
    }

    private void setMaxFrameRetries(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxFrameRetries(getIntegerAttribute(pendingMessage));
    }

    private void setNeighbourTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeNeighbourTableEntryTTL(getIntegerAttribute(pendingMessage));
    }

    private void setHighPriorityWindowSize(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeHighPriorityWindowSize(getIntegerAttribute(pendingMessage));
    }

    private void setCSMAFairnessLimit(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeCSMAFairnessLimit(getIntegerAttribute(pendingMessage));
    }

    private void setBeaconRandomizationWindowLength(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeBeaconRandomizationWindowLength(getIntegerAttribute(pendingMessage));
    }

    private void setMacA(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacA(getIntegerAttribute(pendingMessage));
    }

    private void setMacK(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacK(getIntegerAttribute(pendingMessage));
    }

    private void setMinimumCWAttempts(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinCWAttempts(getIntegerAttribute(pendingMessage));
    }

    private void setMaxBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxBE(getIntegerAttribute(pendingMessage));
    }

    private void setMaxCSMABackOff(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxCSMABackOff(getIntegerAttribute(pendingMessage));
    }

    private void setMinBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinBE(getIntegerAttribute(pendingMessage));
    }

    private void setMaxNumberOfHops(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxHops(value);
    }

    private void setWeakLQIValue(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeWeakLqiValue(value);
    }

    private void setSecurityLevelpendingMessage(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeSecurityLevel(value);
    }

    private void setRoutingConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();

        int adp_net_traversal_time = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_net_traversal_time).getDeviceMessageAttributeValue());
        int adp_routing_table_entry_TTL = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_routing_table_entry_TTL).getDeviceMessageAttributeValue());
        int adp_Kr = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kr).getDeviceMessageAttributeValue());
        int adp_Km = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Km).getDeviceMessageAttributeValue());
        int adp_Kc = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kc).getDeviceMessageAttributeValue());
        int adp_Kq = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kq).getDeviceMessageAttributeValue());
        int adp_Kh = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kh).getDeviceMessageAttributeValue());
        int adp_Krt = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Krt).getDeviceMessageAttributeValue());
        int adp_RREQ_retries = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RREQ_retries).getDeviceMessageAttributeValue());
        int adp_RREQ_RERR_wait = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RREQ_RERR_wait).getDeviceMessageAttributeValue());
        int adp_Blacklist_table_entry_TTL = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Blacklist_table_entry_TTL).getDeviceMessageAttributeValue());
        boolean adp_unicast_RREQ_gen_enable = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_unicast_RREQ_gen_enable).getDeviceMessageAttributeValue());
        boolean adp_RLC_enabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RLC_enabled).getDeviceMessageAttributeValue());
        int adp_add_rev_link_cost = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_add_rev_link_cost).getDeviceMessageAttributeValue());

        cof.getSixLowPanAdaptationLayerSetup().writeRoutingConfiguration(
                adp_net_traversal_time,
                adp_routing_table_entry_TTL,
                adp_Kr,
                adp_Km,
                adp_Kc,
                adp_Kq,
                adp_Kh,
                adp_Krt,
                adp_RREQ_retries,
                adp_RREQ_RERR_wait,
                adp_Blacklist_table_entry_TTL,
                adp_unicast_RREQ_gen_enable,
                adp_RLC_enabled,
                adp_add_rev_link_cost
        );
    }

    private void setBroadCastLogTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeBroadcastLogTableTTL(getIntegerAttribute(pendingMessage));
    }

    private void setMaxJoinWaitTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxJoinWaitTime(getIntegerAttribute(pendingMessage));
    }

    private void setPathDiscoveryTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writePathDiscoveryTime(getIntegerAttribute(pendingMessage));
    }

    private void setMetricType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMetricType(getIntegerAttribute(pendingMessage));
    }

    private void setCoordShortAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeCoordShortAddress(getIntegerAttribute(pendingMessage));
    }

    private void setDisableDefaultRouting(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDisableDefaultRouting(getBooleanAttribute(pendingMessage));
    }

    private void setDeviceType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDeviceType(getIntegerAttribute(pendingMessage));
    }

    private void writePlcG3Timeout(OfflineDeviceMessage pendingMessage) throws IOException {
        int timeoutInMinutes = getIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getData(PLC_G3_TIMEOUT_OBISCODE).setValueAttr(new Unsigned16(timeoutInMinutes));
    }

    @Override
    protected void upgradeFirmwareWithActivationDateAndImageIdentifier(OfflineDeviceMessage pendingMessage) throws IOException {
        String userFile = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getDeviceMessageAttributeValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getDeviceMessageAttributeValue();   // Will return empty string if the MessageAttribute could not be found
        byte[] image = ProtocolTools.getBytesFromHexString(userFile, "");
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue(); // Will return empty string if the MessageAttribute could not be found
        if(imageIdentifier.isEmpty()){
            imageIdentifier = getImageIdentifierFromFile(image);
        }

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
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
            it.upgrade(image, false);
        } else {
            it.upgrade(image, false, imageIdentifier, false);
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

    public static String getImageIdentifierFromFile(byte[] image) {
        int first = -1;
        int last = -1;
        for (int i = 0; i < image.length; i++)
            if (first == -1) {
                if (isAsciiPrintable((char) (image[i] & 0xFF))) {
                    first = i;
                }
            } else {
                if (!isAsciiPrintable((char) (image[i] & 0xFF))) {
                    last = i;
                    i = image.length;
                }
            }
        if (first == -1) {
            return "";
        } else if (last == -1) {
            last = image.length;
        }
        return new String(image, first, last - first);
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
    public static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }
}