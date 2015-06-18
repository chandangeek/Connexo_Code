package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.common.Quantity;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;


import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * @author sva
 * @since 29/11/13 - 15:18
 */
public class Dsmr23MbusMessageExecutor extends AbstractMessageExecutor {

    public static final OctetString MBUS_CLIENT_VALUE_INFORMATION_BLOCK_USE_CORRECTED = OctetString.fromByteArray(new byte[]{0x13});
    public static final OctetString MBUS_CLIENT_VALUE_INFORMATION_BLOCK_USE_UNCORRECTED = OctetString.fromByteArray(new byte[]{(byte) 0x93, (byte) 0x3A});

    private final Clock clock;
    private final TopologyService topologyService;
    private final LoadProfileFactory loadProfileFactory;

    public Dsmr23MbusMessageExecutor(AbstractDlmsProtocol protocol, Clock clock, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, TopologyService topologyService, CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory) {
        super(protocol, issueService, readingTypeUtilService, collectedDataFactory);
        this.clock = clock;
        this.topologyService = topologyService;
        this.loadProfileFactory = loadProfileFactory;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_OPEN)) {
                    doDisconnect(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
                    doTimedControlAction(pendingMessage, ContactorAction.REMOTE_DISCONNECT);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_CLOSE)) {
                    doConnect(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
                    doTimedControlAction(pendingMessage, ContactorAction.REMOTE_CONENCT);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE)) {
                    changeControlMode(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.MBUS_SETUP_DECOMMISSION)) {
                    doDecommission(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS)) {
                    setMbusEncryptionKeys(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.MBUS_SETUP_USE_CORRECTED_VALUES)) {
                   setMbusCorrectedMode(pendingMessage, MbusCorrectedMode.USE_CORRECTED_MODE);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.MBUS_SETUP_USE_UNCORRECTED_VALUES)) {
                    setMbusCorrectedMode(pendingMessage, MbusCorrectedMode.USE_UNCORRECTED_MODE);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST)) {
                    collectedMessage = partialLoadProfileRequest(pendingMessage);    //This message returns a result
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST)) {
                    collectedMessage = loadProfileRegisterRequest(pendingMessage);    //This message returns a result
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }
            }
            result.addCollectedMessages(collectedMessage);
        }
        return result;
    }

    private void doDisconnect(OfflineDeviceMessage pendingMessage) throws IOException {
        Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(pendingMessage.getDeviceSerialNumber())).getObisCode());
        connector.remoteReconnect();
    }

    private void doConnect(OfflineDeviceMessage pendingMessage) throws IOException {
        Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(pendingMessage.getDeviceSerialNumber())).getObisCode());
        connector.remoteDisconnect();
    }

    private void doTimedControlAction(OfflineDeviceMessage pendingMessage, ContactorAction action) throws IOException {
        Array executionTimeArray = convertEpochToDateTimeArray(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.contactorActivationDateAttributeName));
        SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getMbusDisconnectControlSchedule(getMbusAddress(pendingMessage.getDeviceSerialNumber())).getObisCode());

        ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getMbusDisconnectorScriptTable(getMbusAddress(pendingMessage.getDeviceSerialNumber())).getObisCode());
        byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
        scriptStruct.addDataType(new Unsigned16(action.getAction()));

        sasDisconnect.writeExecutedScript(scriptStruct);
        sasDisconnect.writeExecutionTime(executionTimeArray);
    }

    private void changeControlMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int controlMode = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.contactorModeAttributeName));
        Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(pendingMessage.getDeviceSerialNumber())).getObisCode());
        connectorMode.writeControlMode(new TypeEnum(controlMode));
    }

    private void doDecommission(OfflineDeviceMessage pendingMessage) throws IOException {
        getMBusClient(pendingMessage.getDeviceSerialNumber()).deinstallSlave();
    }

    protected void setMbusEncryptionKeys(OfflineDeviceMessage pendingMessage) throws IOException {
        String openKey = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.openKeyAttributeName);
        String transferKey = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.transferKeyAttributeName);
        MBusClient mbusClient = getMBusClient(pendingMessage.getDeviceSerialNumber());

        if (openKey == null) {
            mbusClient.setEncryptionKey("");
        } else if (transferKey != null) {
            mbusClient.setTransportKey(convertStringToByte(transferKey));
            mbusClient.setEncryptionKey(convertStringToByte(openKey));
        } else {
            throw new IOException("Transfer key may not be empty when setting the encryption keys.");
        }
    }

    private byte[] convertStringToByte(String string) throws IOException {
        try {
            byte[] b = new byte[string.length() / 2];
            int offset = 0;
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) Integer.parseInt(string.substring(offset, offset += 2), 16);
            }
            return b;
        } catch (NumberFormatException e) {
            throw new IOException("String " + string + " can not be formatted to byteArray");
        }
    }

    private void setMbusCorrectedMode(OfflineDeviceMessage pendingMessage, MbusCorrectedMode mode) throws IOException {
        Array capDef = new Array();
        Structure struct = new Structure();
        OctetString dib = OctetString.fromByteArray(new byte[]{0x0C});
        struct.addDataType(dib);
        OctetString vib;
        if (mode.equals(MbusCorrectedMode.USE_CORRECTED_MODE)) {
            vib = MBUS_CLIENT_VALUE_INFORMATION_BLOCK_USE_CORRECTED;
        } else {
            vib = MBUS_CLIENT_VALUE_INFORMATION_BLOCK_USE_UNCORRECTED;
        }
        struct.addDataType(vib);
        capDef.addDataType(struct);
        getMBusClient(pendingMessage.getDeviceSerialNumber()).writeCaptureDefinition(capDef);
    }

    private CollectedMessage partialLoadProfileRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        try {
            String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getDeviceMessageAttributeValue();
            String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getDeviceMessageAttributeValue();
            String toDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, toDateAttributeName).getDeviceMessageAttributeValue();
            String fullLoadProfileContent = LoadProfileMessageUtils.createPartialLoadProfileMessage("PartialLoadProfile", "fromDate", "toDate", loadProfileContent);
            Instant fromDate = Instant.ofEpochMilli(Long.valueOf(fromDateEpoch));
            Instant toDate = Instant.ofEpochMilli(Long.valueOf(toDateEpoch));

            LegacyLoadProfileRegisterMessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder(clock, this.topologyService, loadProfileFactory);
            builder.fromXml(fullLoadProfileContent);

            LoadProfileReader lpr = builder.getLoadProfileReader();  //Does not contain the correct from & to date yet, they were stored in separate attributes
            LoadProfileReader fullLpr =
                    new LoadProfileReader(
                            this.clock,
                            lpr.getProfileObisCode(),
                            fromDate,
                            toDate,
                            lpr.getLoadProfileId(),
                            lpr.getDeviceIdentifier(),
                            lpr.getChannelInfos(),
                            lpr.getMeterSerialNumber(),
                            lpr.getLoadProfileIdentifier());

            fullLpr = checkLoadProfileReader(fullLpr, builder.getMeterSerialNumber());
            List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = getProtocol().fetchLoadProfileConfiguration(Arrays.asList(fullLpr));
            for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
                if (!config.isSupportedByMeter()) {   //LP not supported
                    CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(
                            ResultType.NotSupported,
                            createMessageFailedIssue(pendingMessage, "Load profile with obiscode " + config.getObisCode() + " is not supported by the device"));
                    return collectedMessage;
                }
            }

            List<CollectedLoadProfile> loadProfileData = getProtocol().getLoadProfileData(Arrays.asList(fullLpr));
            CollectedMessage collectedMessage = createCollectedMessageWithLoadProfileData(pendingMessage, loadProfileData.get(0));
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (SAXException e) {              //Failed to parse XML data
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }
    }

    /**
     * The Mbus Hourly gasProfile needs to change the B-field in the ObisCode to readout the correct profile. To do se, we use the serialNumber of the Message.
     *
     * @param lpr the reader to change
     * @return the adapted LoadProfileReader
     */
    private LoadProfileReader checkLoadProfileReader(final LoadProfileReader lpr, String serialNumber) {
        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString("0.x.24.3.0.255"))) {
            return new LoadProfileReader(
                    this.clock,
                    lpr.getProfileObisCode(),
                    lpr.getStartReadingTime(),
                    lpr.getEndReadingTime(),
                    lpr.getLoadProfileId(),
                    lpr.getDeviceIdentifier(),
                    lpr.getChannelInfos(),
                    serialNumber,
                    lpr.getLoadProfileIdentifier());
        } else {
            return lpr;
        }
    }

    protected CollectedMessage loadProfileRegisterRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getDeviceMessageAttributeValue();
        String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getDeviceMessageAttributeValue();
        String fullLoadProfileContent = LoadProfileMessageUtils.createLoadProfileRegisterMessage("LoadProfileRegister", "fromDate", loadProfileContent);
        Instant fromDate = Instant.ofEpochMilli(Long.valueOf(fromDateEpoch));
        try {
            LegacyLoadProfileRegisterMessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder(clock, this.topologyService, loadProfileFactory);
            builder.fromXml(fullLoadProfileContent);
            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
                CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(
                        ResultType.ConfigurationMisMatch,
                        createMessageFailedIssue(pendingMessage, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode()));
            }

            LoadProfileReader lpr = checkLoadProfileReader(constructDateTimeCorrectdLoadProfileReader(builder.getLoadProfileReader()), builder.getMeterSerialNumber());
            LoadProfileReader fullLpr =
                    new LoadProfileReader(
                            this.clock,
                            lpr.getProfileObisCode(),
                            fromDate,
                            this.clock.instant(),
                            lpr.getLoadProfileId(),
                            lpr.getDeviceIdentifier(),
                            lpr.getChannelInfos(),
                            lpr.getMeterSerialNumber(),
                            lpr.getLoadProfileIdentifier());

            List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = getProtocol().fetchLoadProfileConfiguration(Arrays.asList(fullLpr));
            for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
                if (!config.isSupportedByMeter()) {   //LP not supported
                    CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(
                            ResultType.NotSupported,
                            createMessageFailedIssue(pendingMessage, "Load profile with obiscode " + config.getObisCode() + " is not supported by the device"));
                    return collectedMessage;
                }
            }

            List<CollectedLoadProfile> loadProfileData = getProtocol().getLoadProfileData(Arrays.asList(fullLpr));

            CollectedLoadProfile collectedLoadProfile = loadProfileData.get(0);
            IntervalData intervalDatas = null;
            for (IntervalData intervalData : collectedLoadProfile.getCollectedIntervalData()) {
                if (intervalData.getEndTime().equals(builder.getStartReadingTime())) {
                    intervalDatas = intervalData;
                }
            }

            if (intervalDatas == null) {
                CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(
                        ResultType.DataIncomplete,
                        createMessageFailedIssue(pendingMessage, "Didn't receive data for requested interval (" + builder.getStartReadingTime() + ")"));
                return collectedMessage;
            }

            Register previousRegister = null;
            List<CollectedRegister> collectedRegisters = new ArrayList<>();
            for (Register register : builder.getRegisters()) {
                if (register.equals(previousRegister)) {
                    continue;    //Don't add the same intervals twice if there's 2 channels with the same obiscode
                }
                for (int i = 0; i < collectedLoadProfile.getChannelInfo().size(); i++) {
                    final ChannelInfo channel = collectedLoadProfile.getChannelInfo().get(i);
                    if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
                        final RegisterValue registerValue = new RegisterValue(register, new Quantity(intervalDatas.get(i), channel.getUnit()), intervalDatas.getEndTime(), null, intervalDatas.getEndTime(), new Date(), register.getRegisterSpecId());
                        collectedRegisters.add(createCollectedRegister(registerValue, pendingMessage));
                    }
                }
                previousRegister = register;
            }
            CollectedMessage collectedMessage = createCollectedMessageWithRegisterData(pendingMessage, collectedRegisters);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (SAXException e) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }
    }

    /**
     * Substracts 5 seconds from the startReadingTime and adds 5 seconds to the endReadingTime
     *
     * @param loadProfileReader the reader
     * @return the reader with the adjested times
     */
    protected LoadProfileReader constructDateTimeCorrectdLoadProfileReader(LoadProfileReader loadProfileReader) {
        Instant from = loadProfileReader.getStartReadingTime().minus(Duration.ofSeconds(5));
        Instant to = loadProfileReader.getEndReadingTime().plus(Duration.ofSeconds(5));
        return new LoadProfileReader(
                this.clock,
                loadProfileReader.getProfileObisCode(),
                from,
                to,
                loadProfileReader.getLoadProfileId(),
                loadProfileReader.getDeviceIdentifier(),
                loadProfileReader.getChannelInfos(),
                loadProfileReader.getMeterSerialNumber(),
                loadProfileReader.getLoadProfileIdentifier());
    }

    private enum ContactorAction {
        REMOTE_DISCONNECT(1),
        REMOTE_CONENCT(2);

        private final int action;

        private ContactorAction(int action) {
            this.action = action;
        }

        private int getAction() {
            return action;
        }
    }

    private enum MbusCorrectedMode {
        USE_CORRECTED_MODE,
        USE_UNCORRECTED_MODE
    }
}
