package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.topology.DeviceMapping;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.ibm.KaifaRegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.phaseAttributeName;

public class KaifaDsmr40MessageExecutor extends Dsmr40MessageExecutor{
    public KaifaDsmr40MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor);
    }

    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);
        List<OfflineDeviceMessage> mbusMessages = getMbusMessages(pendingMessages);
        if (!mbusMessages.isEmpty()) {
            // Execute messages for MBus devices
            result.addCollectedMessages(getMbusMessageExecutor().executePendingMessages(mbusMessages));
        }

        List<OfflineDeviceMessage> notExecutedDeviceMessages = new ArrayList<>();
        for (OfflineDeviceMessage pendingMessage : masterMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                    if (pendingMessage.getSpecification()
                            .equals(PowerConfigurationDeviceMessage.SetVoltageSagThreshold)) {
                        writeVoltageSagThresholdValue(pendingMessage);
                    } else if (pendingMessage.getSpecification()
                            .equals(PowerConfigurationDeviceMessage.SetVoltageSagTimeThreshold)) {
                        writeVoltageSagTimeThresholdValue(pendingMessage);
                    } else if (pendingMessage.getSpecification()
                            .equals(PowerConfigurationDeviceMessage.SetVoltageSwellThreshold)) {
                        writeVoltageSwellThresholdValue(pendingMessage);
                    } else if (pendingMessage.getSpecification()
                            .equals(PowerConfigurationDeviceMessage.SetVoltageSwellTimeThreshold)) {
                        writeVoltageSwellTimeThresholdValue(pendingMessage);
                    } else {
                        collectedMessage = null;
                        notExecutedDeviceMessages.add(pendingMessage);
                    }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            }
            if (collectedMessage != null) {
                result.addCollectedMessage(collectedMessage);
            }
        }

        // Then delegate all other messages to the Dsmr 4.0 message executor
        result.addCollectedMessages(super.executePendingMessages(notExecutedDeviceMessages));
        return result;
    }

    protected void mbusReset(OfflineDeviceMessage pendingMessage) throws IOException {
        //Find the MBus channel based on the given MBus serial number
        String mbusSerialNumber = pendingMessage.getDeviceSerialNumber();
        int channel = 0;
        for (DeviceMapping deviceMapping : ((AbstractSmartNtaProtocol)getProtocol()).getMeterTopology().getMbusMeterMap()) {
            if (deviceMapping.getSerialNumber().equals(mbusSerialNumber)) {
                channel = deviceMapping.getPhysicalAddress();
                break;
            }
        }
        if (channel == 0) {
            throw new IOException("No MBus slave meter with serial number '" + mbusSerialNumber + "' is installed on this e-meter");
        }

        ObisCode mbusClientObisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) channel);
        MBusClient mbusClient = getProtocol().getDlmsSession().getCosemObjectFactory().getMbusClient(mbusClientObisCode, MbusClientAttributes.VERSION10);
        try{
            mbusClient.setIdentificationNumber(new Unsigned32(0));
            mbusClient.setManufacturerID(new Unsigned16(0));
            mbusClient.setVersion(0);
            mbusClient.setDeviceType(0);
        }catch(ProtocolException e){
        }
    }

    private void writeVoltageSagThresholdValue(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int threshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, phaseAttributeName).getValue()).intValue();
        getCosemObjectFactory().writeObject(KaifaRegisterFactory.PV_VOLTAGE_SAG,  DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(threshold).getBEREncodedByteArray());
    }
    private void writeVoltageSagTimeThresholdValue(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int threshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, phaseAttributeName).getValue()).intValue();
        getCosemObjectFactory().writeObject(KaifaRegisterFactory.PV_TIME_SAG,  DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(threshold).getBEREncodedByteArray());
    }

    private void writeVoltageSwellThresholdValue(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int threshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, phaseAttributeName).getValue()).intValue();
        getCosemObjectFactory().writeObject(KaifaRegisterFactory.PV_THRESHOLD_VOLTAGE_SWELL,  DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(threshold).getBEREncodedByteArray());
    }
    private void writeVoltageSwellTimeThresholdValue(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int threshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, phaseAttributeName).getValue()).intValue();
        getCosemObjectFactory().writeObject(KaifaRegisterFactory.PV_TIME_THRESHOLD_SWELL,  DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(threshold).getBEREncodedByteArray());
    }
}
