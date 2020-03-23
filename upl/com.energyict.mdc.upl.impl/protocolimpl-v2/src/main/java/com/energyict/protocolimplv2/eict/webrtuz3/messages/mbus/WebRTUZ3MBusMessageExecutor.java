package com.energyict.protocolimplv2.eict.webrtuz3.messages.mbus;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.openKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.transferKeyAttributeName;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/04/2015 - 15:26
 */
public class WebRTUZ3MBusMessageExecutor extends AbstractMessageExecutor {

    public static final ObisCode MBUS_CLIENT_OBIS = ObisCode.fromString("0.x.24.1.0.255");
    public static final ObisCode MBUS_DISCONNECT_CONTROL_OBIS = ObisCode.fromString("0.x.24.4.0.255");
    public static final ObisCode MBUS_DISCONNECT_CONTROL_SCHEDULE_OBIS = ObisCode.fromString("0.x.24.6.0.255");
    public static final ObisCode MBUS_DISCONNECT_SCRIPT_TABLE_OBIS = ObisCode.fromString("0.x.24.7.0.255");

    public WebRTUZ3MBusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                collectedMessage = executeMessage(pendingMessage, collectedMessage);
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession().getProperties().getRetries() + 1)) {
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

    private CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT)) {
            contactorClose(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT)) {
            contactorOpen(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
            contactorCloseWithActivationDate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
            contactorOpenWithActivationDate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE)) {
            changeConnectControlMode(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.Decommission)) {
            decommission(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.SetEncryptionKeys)) {
            setEncryptionKeys(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.UseCorrectedValues)) {
            useCorrectedValues(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.UseUncorrectedValues)) {
            useUncorrectedValues(pendingMessage);
        } else {   //Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private void useUncorrectedValues(OfflineDeviceMessage pendingMessage) throws IOException {
        MBusClient mc = getCosemObjectFactory().getMbusClient(getCorrectedObisCode(MBUS_CLIENT_OBIS, pendingMessage));
        Array capDef = new Array();
        Structure struct = new Structure();
        OctetString dib = OctetString.fromByteArray(new byte[]{(byte) 0x0C});
        struct.addDataType(dib);
        OctetString vib = OctetString.fromByteArray(new byte[]{(byte) 0x93, (byte) 0x3A});
        struct.addDataType(vib);
        capDef.addDataType(struct);
        mc.writeCaptureDefinition(capDef);
    }

    private void useCorrectedValues(OfflineDeviceMessage pendingMessage) throws IOException {
        MBusClient mc = getCosemObjectFactory().getMbusClient(getCorrectedObisCode(MBUS_CLIENT_OBIS, pendingMessage));
        Array capDef = new Array();
        Structure struct = new Structure();
        OctetString dib = OctetString.fromByteArray(new byte[]{0x0C});
        struct.addDataType(dib);
        OctetString vib = OctetString.fromByteArray(new byte[]{0x13});
        struct.addDataType(vib);
        capDef.addDataType(struct);
        mc.writeCaptureDefinition(capDef);
    }

    private void setEncryptionKeys(OfflineDeviceMessage pendingMessage) throws IOException {
        String openKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, openKeyAttributeName).getValue();
        String transferKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, transferKeyAttributeName).getValue();

        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getCorrectedObisCode(MBUS_CLIENT_OBIS, pendingMessage));
        mbusClient.setEncryptionKey(ProtocolTools.getBytesFromHexString(openKey, ""));
        mbusClient.setTransportKey(ProtocolTools.getBytesFromHexString(transferKey, ""));
    }

    private void decommission(OfflineDeviceMessage pendingMessage) throws IOException {
        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getCorrectedObisCode(MBUS_CLIENT_OBIS, pendingMessage));
        mbusClient.deinstallSlave();
    }

    private void changeConnectControlMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int mode = Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_OBIS, pendingMessage));
        connectorMode.writeControlMode(new TypeEnum(mode));
    }

    private void contactorCloseWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, contactorActivationDateAttributeName).getValue();

        Array executionTimeArray = convertEpochToDateTimeArray(epoch);
        SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_SCHEDULE_OBIS, pendingMessage));

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(getCorrectedObisCode(MBUS_DISCONNECT_SCRIPT_TABLE_OBIS, pendingMessage).getLN()));
        scriptStruct.addDataType(new Unsigned16(2));     // method '2' is the 'remote_connect' method

        sasConnect.writeExecutedScript(scriptStruct);
        sasConnect.writeExecutionTime(executionTimeArray);
    }

    private void contactorOpenWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, contactorActivationDateAttributeName).getValue();

        Array executionTimeArray = convertEpochToDateTimeArray(epoch);
        SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_SCHEDULE_OBIS, pendingMessage));

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(getCorrectedObisCode(MBUS_DISCONNECT_SCRIPT_TABLE_OBIS, pendingMessage).getLN()));
        scriptStruct.addDataType(new Unsigned16(1));    // method '1' is the 'remote_disconnect' method

        sasConnect.writeExecutedScript(scriptStruct);
        sasConnect.writeExecutionTime(executionTimeArray);
    }

    private void contactorClose(OfflineDeviceMessage pendingMessage) throws IOException {
        Disconnector connector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_OBIS, pendingMessage));
        connector.remoteReconnect();
    }

    private void contactorOpen(OfflineDeviceMessage pendingMessage) throws IOException {
        Disconnector connector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_OBIS, pendingMessage));
        connector.remoteDisconnect();
    }

    private ObisCode getCorrectedObisCode(ObisCode obisCode, OfflineDeviceMessage pendingMessage) {
        return getProtocol().getPhysicalAddressCorrectedObisCode(obisCode, pendingMessage.getDeviceSerialNumber());
    }
}