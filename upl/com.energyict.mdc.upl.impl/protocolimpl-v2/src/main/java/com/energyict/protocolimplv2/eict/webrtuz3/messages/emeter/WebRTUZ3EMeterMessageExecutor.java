package com.energyict.protocolimplv2.eict.webrtuz3.messages.emeter;

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
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/04/2015 - 15:26
 */
public class WebRTUZ3EMeterMessageExecutor extends AbstractMessageExecutor {

    public static final ObisCode DISCONNECTOR_OBIS = ObisCode.fromString("0.x.96.3.10.255");
    public static final ObisCode DISCONNECTOR_SCRIPT_TABLE_OBIS = ObisCode.fromString("0.x.10.0.106.255");
    public static final ObisCode DISCONNECTOR_CTR_SCHEDULE_OBIS = ObisCode.fromString("0.x.15.0.1.255");

    public WebRTUZ3EMeterMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
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
        } else {   //Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private void changeConnectControlMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int mode = Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS, pendingMessage));
        connectorMode.writeControlMode(new TypeEnum(mode));
    }

    private void contactorCloseWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, contactorActivationDateAttributeName).getValue();

        Array executionTimeArray = convertEpochToDateTimeArray(epoch);
        SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(DISCONNECTOR_CTR_SCHEDULE_OBIS, pendingMessage));

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(getCorrectedObisCode(DISCONNECTOR_SCRIPT_TABLE_OBIS, pendingMessage).getLN()));
        scriptStruct.addDataType(new Unsigned16(2));     // method '2' is the 'remote_connect' method

        sasConnect.writeExecutedScript(scriptStruct);
        sasConnect.writeExecutionTime(executionTimeArray);
    }

    private void contactorOpenWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, contactorActivationDateAttributeName).getValue();

        Array executionTimeArray = convertEpochToDateTimeArray(epoch);
        SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(DISCONNECTOR_CTR_SCHEDULE_OBIS, pendingMessage));

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(getCorrectedObisCode(DISCONNECTOR_SCRIPT_TABLE_OBIS, pendingMessage).getLN()));
        scriptStruct.addDataType(new Unsigned16(1));    // method '1' is the 'remote_disconnect' method

        sasConnect.writeExecutedScript(scriptStruct);
        sasConnect.writeExecutionTime(executionTimeArray);
    }

    private void contactorClose(OfflineDeviceMessage pendingMessage) throws IOException {
        Disconnector connector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS, pendingMessage));
        connector.remoteReconnect();
    }

    private void contactorOpen(OfflineDeviceMessage pendingMessage) throws IOException {
        Disconnector connector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS, pendingMessage));
        connector.remoteDisconnect();
    }

    private ObisCode getCorrectedObisCode(ObisCode obisCode, OfflineDeviceMessage pendingMessage) {
        return getProtocol().getPhysicalAddressCorrectedObisCode(obisCode, pendingMessage.getDeviceSerialNumber());
    }
}