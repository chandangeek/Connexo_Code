package com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:31
 */
public class IDISMBusMessageExecutor extends AbstractMessageExecutor {

    protected static final ObisCode DISCONNECTOR_SCRIPT_MBUS_OBISCODE = ObisCode.fromString("0.1.10.0.106.255");
    protected static final ObisCode DISCONNECTOR_CONTROL_MBUS_OBISCODE = ObisCode.fromString("0.0.24.4.0.255");
    protected static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");
    private static final ObisCode TIMED_CONNECTOR_ACTION_MBUS_OBISCODE = ObisCode.fromString("0.1.15.0.1.255");

    public IDISMBusMessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN)) {
                    remoteDisconnect(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE)) {
                    remoteConnect(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
                    timedAction(pendingMessage, 1);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
                    timedAction(pendingMessage, 2);
                } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.Decommission)) {
                    decomission(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.SetEncryptionKeys)) {
                    setEncryptionKeys(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.WriteCaptureDefinitionForAllInstances)) {
                    writeCaptureDefinition(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.WriteMBusCapturePeriod)) {
                    writeMBusCapturePeriod(pendingMessage);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                    collectedMessage.setDeviceProtocolInformation("Message is currently not supported by the protocol");
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession().getProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
            }

            result.addCollectedMessage(collectedMessage);
        }

        return result;
    }

    private void writeMBusCapturePeriod(OfflineDeviceMessage pendingMessage) throws IOException {
        int capturePeriodInSeconds = new BigDecimal(pendingMessage.getDeviceMessageAttributes().get(0).getValue()).intValue();
        getMBusClient(pendingMessage).setCapturePeriod(capturePeriodInSeconds);
    }

    protected void writeCaptureDefinition(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] dibInstance1 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance1).getValue(), "");
        byte[] dibInstance2 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance2).getValue(), "");
        byte[] dibInstance3 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance3).getValue(), "");
        byte[] dibInstance4 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance4).getValue(), "");

        byte[] vibInstance1 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.vibInstance1).getValue(), "");
        byte[] vibInstance2 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.vibInstance2).getValue(), "");
        byte[] vibInstance3 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.vibInstance3).getValue(), "");
        byte[] vibInstance4 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.vibInstance4).getValue(), "");

        Structure element1 = new Structure();
        element1.addDataType(OctetString.fromByteArray(dibInstance1));
        element1.addDataType(OctetString.fromByteArray(vibInstance1));
        Structure element2 = new Structure();
        element2.addDataType(OctetString.fromByteArray(dibInstance2));
        element2.addDataType(OctetString.fromByteArray(vibInstance2));
        Structure element3 = new Structure();
        element3.addDataType(OctetString.fromByteArray(dibInstance3));
        element3.addDataType(OctetString.fromByteArray(vibInstance3));
        Structure element4 = new Structure();
        element4.addDataType(OctetString.fromByteArray(dibInstance4));
        element4.addDataType(OctetString.fromByteArray(vibInstance4));

        Array capture_definition = new Array();
        capture_definition.addDataType(element1);
        capture_definition.addDataType(element2);
        capture_definition.addDataType(element3);
        capture_definition.addDataType(element4);

        getMBusClient(pendingMessage).writeCaptureDefinition(capture_definition);
    }

    private void setEncryptionKeys(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] openKey = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.openKeyAttributeName).getValue(), "");
        byte[] transferKey = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.transferKeyAttributeName).getValue(), "");

        MBusClient mBusClient = getMBusClient(pendingMessage);
        mBusClient.setTransportKey(transferKey);
        mBusClient.setEncryptionKey(openKey);
    }

    private void decomission(OfflineDeviceMessage pendingMessage) throws IOException {
        getMBusClient(pendingMessage).invoke(2, new Unsigned8(0).getBEREncodedByteArray());
    }

    private MBusClient getMBusClient(OfflineDeviceMessage offlineDeviceMessage) throws ProtocolException {
        return getCosemObjectFactory().getMbusClient(getMBusClientObisCode(offlineDeviceMessage), MbusClientAttributes.VERSION10);
    }

    /**
     * Returns the obiscode of the MBus-client object for a specific MBus meter.
     */
    private ObisCode getMBusClientObisCode(OfflineDeviceMessage offlineDeviceMessage) {
        return ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, getMBusChannelId(offlineDeviceMessage));
    }

    protected void timedAction(OfflineDeviceMessage offlineDeviceMessage, int action) throws IOException {
        Long epoch = Long.valueOf(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getValue());
        Date actionTime = new Date(epoch);  //EIServer system timezone

        SingleActionSchedule singleActionSchedule = getCosemObjectFactory().getSingleActionSchedule(TIMED_CONNECTOR_ACTION_MBUS_OBISCODE);

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(DISCONNECTOR_SCRIPT_MBUS_OBISCODE.getLN()));
        scriptStruct.addDataType(new Unsigned16(action + (2 * (getMBusChannelId(offlineDeviceMessage) - 1))));     // 1 = disconnect MBus 1, 2 = reconnect MBus 1, 3 = disconnect MBus 2,...

        singleActionSchedule.writeExecutedScript(scriptStruct);
        Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
        cal.setTime(actionTime);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(cal));
    }

    protected void remoteDisconnect(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        ObisCode obisCode = ProtocolTools.setObisCodeField(DISCONNECTOR_CONTROL_MBUS_OBISCODE, 1, getMBusChannelId(offlineDeviceMessage));
        getCosemObjectFactory().getDisconnector(obisCode).remoteDisconnect();
    }

    protected void remoteConnect(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        ObisCode obisCode = ProtocolTools.setObisCodeField(DISCONNECTOR_CONTROL_MBUS_OBISCODE, 1, getMBusChannelId(offlineDeviceMessage));
        getCosemObjectFactory().getDisconnector(obisCode).remoteReconnect();
    }

    private byte getMBusChannelId(OfflineDeviceMessage offlineDeviceMessage) {
        return (byte) getProtocol().getPhysicalAddressFromSerialNumber(offlineDeviceMessage.getDeviceSerialNumber());
    }
}