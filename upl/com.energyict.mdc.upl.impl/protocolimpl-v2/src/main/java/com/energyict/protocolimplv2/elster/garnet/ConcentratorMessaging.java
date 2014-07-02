package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.exception.NotExecutedException;
import com.energyict.protocolimplv2.elster.garnet.structure.ContactorResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.field.NotExecutedError;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;

import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 27/06/2014 - 13:26
 */
public class ConcentratorMessaging implements DeviceMessageSupport {

    private final GarnetConcentrator deviceProtocol;

    public ConcentratorMessaging(GarnetConcentrator deviceProtocol) {
        this.deviceProtocol = deviceProtocol;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = null;

            if (isMessageForEMeterSlave(pendingMessage)) {
                if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN)) {
                    collectedMessage = executeContactorOperation(pendingMessage, false);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE)) {
                    collectedMessage = executeContactorOperation(pendingMessage, true);
                }
            }

            if (collectedMessage == null) { //This is the case when the message is not executed
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    private boolean isMessageForEMeterSlave(OfflineDeviceMessage pendingMessage) {
        return getDeviceProtocol().getOfflineDevice().getId() != pendingMessage.getDeviceId();
    }

    private CollectedMessage executeContactorOperation(OfflineDeviceMessage pendingMessage, boolean isReconnect)  {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            ContactorResponseStructure contactorResponseStructure = getDeviceProtocol().getRequestFactory().executeContactorOperation(pendingMessage.getDeviceSerialNumber(), isReconnect);
            int feedbackCode = contactorResponseStructure.getContactorFeedback().getFeedbackCode();

            String message;
            ErrorCode errorCode = ErrorCode.fromCode(feedbackCode >> 6 & 0xFF);
            switch (errorCode) {
                case SUCCESS:
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
                    break;
                case METER_NOT_FOUND:
                case METER_REPLACED:
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(errorCode.getDescription());
                    message = "Failed to operate the contactor of slave " + pendingMessage.getDeviceSerialNumber()+ " - Probably the meter does not exists or has been replaced.";
                    collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, message));
                    break;
                case METER_IS_NOT_THE_MAIN:
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(errorCode.getDescription());
                    message = "Failed to operate the contactor of slave " + pendingMessage.getDeviceSerialNumber()+ " - Contactor operations are only allowed on the main meter of a poly-phase configuration.";
                    collectedMessage.setFailureInformation(ResultType.DataIncomplete, createMessageFailedIssue(pendingMessage, message));
                    break;
                default:
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(errorCode.getDescription());
                    message = "Failed to operate the contactor of slave " + pendingMessage.getDeviceSerialNumber();
                    collectedMessage.setFailureInformation(ResultType.DataIncomplete, createMessageFailedIssue(pendingMessage, message));
                    break;
            }
        } catch (NotExecutedException e) {
            if (e.getErrorStructure().getNotExecutedError().getErrorCode().equals(NotExecutedError.ErrorCode.COMMAND_NOT_IMPLEMENTED)) {
                collectedMessage.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addProblem(pendingMessage, "operationNotSupported"));
            } else if (e.getErrorStructure().getNotExecutedError().getErrorCode().equals(NotExecutedError.ErrorCode.SLAVE_DOES_NOT_EXIST)) {
                collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, MdcManager.getIssueCollector().addProblem(pendingMessage, "topologyMismatch", deviceProtocol.getSerialNumber()));
            } else {
                collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(pendingMessage, "CouldNotParseMessageData"));
            }
        } catch (GarnetException e) {
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(pendingMessage, "CouldNotParseMessageData"));
        }
        return collectedMessage;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    protected Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) {
        return MdcManager.getIssueCollector().addWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return MdcManager.getIssueCollector().addWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    public GarnetConcentrator getDeviceProtocol() {
        return deviceProtocol;
    }

    private enum ErrorCode {
        SUCCESS(0, "Command executed with success"),
        METER_NOT_FOUND(1, "Meter not found"),
        METER_REPLACED(2, "Meter replaced"),
        METER_IS_NOT_THE_MAIN(3, "Meter is not the main"),
        UNKNOWN(0xFF, "Unknown execution code");

        private final int code;
        private final String description;

        ErrorCode(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static ErrorCode fromCode(int code) {
            for (ErrorCode errorCode : ErrorCode.values()) {
                if (errorCode.getCode() == code) {
                    return errorCode;
                }
            }
            return ErrorCode.UNKNOWN;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}