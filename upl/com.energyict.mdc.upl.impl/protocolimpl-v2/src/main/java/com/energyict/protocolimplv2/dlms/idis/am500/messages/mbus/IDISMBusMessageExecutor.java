package com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.AM500;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
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

    private static final ObisCode TIMED_CONNECTOR_ACTION_MBUS_OBISCODE = ObisCode.fromString("0.1.15.0.1.255");
    protected static final ObisCode DISCONNECTOR_SCRIPT_MBUS_OBISCODE = ObisCode.fromString("0.1.10.0.106.255");
    protected static final ObisCode DISCONNECTOR_CONTROL_MBUS_OBISCODE = ObisCode.fromString("0.0.24.4.0.255");
    protected static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");

    public IDISMBusMessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    public AM500 getProtocol() {
        return (AM500) super.getProtocol();
    }

    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        OfflineDeviceMessage pendingMessage = pendingMessages.get(0);
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
            }
        } catch (IOException e) {
            if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            }   //Else: throw communication exception
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
        }

        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        result.addCollectedMessage(collectedMessage);
        return result;
    }

    private void writeMBusCapturePeriod(OfflineDeviceMessage pendingMessage) throws IOException {
        int capturePeriodInSeconds = new BigDecimal(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue()).intValue();
        getMBusClient(pendingMessage).setCapturePeriod(capturePeriodInSeconds);
    }

    protected void writeCaptureDefinition(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] dibInstance1 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance1).getDeviceMessageAttributeValue());
        byte[] dibInstance2 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance2).getDeviceMessageAttributeValue());
        byte[] dibInstance3 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance3).getDeviceMessageAttributeValue());
        byte[] dibInstance4 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance4).getDeviceMessageAttributeValue());

        byte[] vibInstance1 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance1).getDeviceMessageAttributeValue());
        byte[] vibInstance2 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance2).getDeviceMessageAttributeValue());
        byte[] vibInstance3 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance3).getDeviceMessageAttributeValue());
        byte[] vibInstance4 = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dibInstance4).getDeviceMessageAttributeValue());

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
        byte[] openKey = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.openKeyAttributeName).getDeviceMessageAttributeValue());
        byte[] transferKey = ProtocolTools.getBytesFromHexString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.transferKeyAttributeName).getDeviceMessageAttributeValue());

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
        Long epoch = Long.valueOf(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
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