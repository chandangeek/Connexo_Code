package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.elster.garnet.common.InstallationConfig;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.exception.NotExecutedException;
import com.energyict.protocolimplv2.elster.garnet.exception.UnableToExecuteException;
import com.energyict.protocolimplv2.elster.garnet.structure.ContactorResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.DiscoverMetersResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterSerialNumber;
import com.energyict.protocolimplv2.elster.garnet.structure.field.NotExecutedError;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.ContactorStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterInstallationStatusBitMaskField;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 27/06/2014 - 13:26
 */
public class ConcentratorMessaging implements DeviceMessageSupport {

    private final GarnetConcentrator deviceProtocol;
    private ErrorCode errorCode;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public ConcentratorMessaging(GarnetConcentrator deviceProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.deviceProtocol = deviceProtocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.collectedDataFactory.createCollectedMessageList(pendingMessages);
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

    private CollectedMessage executeContactorOperation(OfflineDeviceMessage pendingMessage, boolean isReconnect) {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            ContactorResponseStructure contactorResponseStructure = getDeviceProtocol().getRequestFactory().executeContactorOperation(pendingMessage.getDeviceSerialNumber(), isReconnect);
            int feedbackCode = contactorResponseStructure.getContactorFeedback().getFeedbackCode();

            this.errorCode = ErrorCode.fromCode(feedbackCode >> 6 & 0xFF);
            String feedbackMessage = checkFeedbackCode(pendingMessage, isReconnect, feedbackCode);

            String message;
            switch (errorCode) {
                case SUCCESS:
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
                    collectedMessage.setDeviceProtocolInformation(feedbackMessage);
                    break;
                case METER_NOT_FOUND:
                case METER_REPLACED:
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(errorCode.getDescription());
                    message = "Failed to operate the contactor of slave " + pendingMessage.getDeviceSerialNumber() + " - Probably the meter does not exists or has been replaced.";
                    collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, message));
                    break;
                case METER_IS_NOT_THE_MAIN:
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(errorCode.getDescription());
                    message = "Failed to operate the contactor of slave " + pendingMessage.getDeviceSerialNumber() + " - Contactor operations are only allowed on the main meter of a poly-phase configuration.";
                    collectedMessage.setFailureInformation(ResultType.DataIncomplete, createMessageFailedIssue(pendingMessage, message));
                    break;
                case NON_TECHNICAL_LOSS:
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(errorCode.getDescription() + " - " + feedbackMessage);
                    message = "Failed to operate the contactor of slave " + pendingMessage.getDeviceSerialNumber() + " - " + feedbackMessage;
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
                collectedMessage.setFailureInformation(ResultType.NotSupported, this.issueFactory.createProblem(pendingMessage, "operationNotSupported"));
            } else if (e.getErrorStructure().getNotExecutedError().getErrorCode().equals(NotExecutedError.ErrorCode.SLAVE_DOES_NOT_EXIST)) {
                collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, this.issueFactory.createProblem(pendingMessage, "topologyMismatch", deviceProtocol.getSerialNumber()));
            } else {
                collectedMessage.setFailureInformation(ResultType.InCompatible, this.issueFactory.createProblem(pendingMessage, "CouldNotParseMessageData", e.getMessage()));
            }
        } catch (GarnetException e) {
            collectedMessage.setFailureInformation(ResultType.InCompatible, this.issueFactory.createProblem(pendingMessage, "CouldNotParseMessageData", e.getMessage()));
        }
        return collectedMessage;
    }

    private String checkFeedbackCode(OfflineDeviceMessage pendingMessage, boolean isReconnect, int feedbackCode) throws GarnetException {
        int nrOfPhases = getInstallationConfigForMeter(pendingMessage.getDeviceSerialNumber());
        StringBuilder builder = new StringBuilder();

        ContactorStatus.ContactorState statusPhase1 = isReconnect ? ContactorStatus.ReconnectStatus.fromContactorCode(feedbackCode & 0x03) : ContactorStatus.DisconnectStatus.fromContactorCode(feedbackCode & 0x03);
        ContactorStatus.ContactorState statusPhase2 = isReconnect ? ContactorStatus.ReconnectStatus.fromContactorCode(feedbackCode >> 2 & 0x03) : ContactorStatus.DisconnectStatus.fromContactorCode(feedbackCode >> 2 & 0x03);
        ContactorStatus.ContactorState statusPhase3 = isReconnect ? ContactorStatus.ReconnectStatus.fromContactorCode(feedbackCode >> 4 & 0x03) : ContactorStatus.DisconnectStatus.fromContactorCode(feedbackCode >> 2 & 0x03);

        builder.append("Phase 1: ").append(statusPhase1.getContactorInfo());
        builder.append(nrOfPhases > 1 ? ", Phase 2: " + statusPhase2.getContactorInfo() : "");
        builder.append(nrOfPhases > 2 ? ", Phase 3: " + statusPhase3.getContactorInfo() : "");

        if (nrOfPhases == 1 && statusPhase1.getContactorCode() != 1 ||
                nrOfPhases == 2 && (statusPhase1.getContactorCode() != 1 || statusPhase2.getContactorCode() != 1) ||
                nrOfPhases == 3 && (statusPhase1.getContactorCode() != 1 || statusPhase2.getContactorCode() != 1 || statusPhase3.getContactorCode() != 1)) {
            // Adapt the error code to reflect non technical loss - this error code will be used in the state machine that is executed after this method
            setErrorCodeToNonTechnicalLoss();
        }
        return builder.toString();
    }

    private void setErrorCodeToNonTechnicalLoss() {
        this.errorCode = ErrorCode.NON_TECHNICAL_LOSS;
    }

    private int getInstallationConfigForMeter(String serialNumber) throws GarnetException {
        DiscoverMetersResponseStructure meterStatuses = getDeviceProtocol().getRequestFactory().discoverMeters();
        int meterIndex = getMeterIndex(meterStatuses, serialNumber);
        List<MeterInstallationStatusBitMaskField> installationStatuses = meterStatuses.getMeterInstallationStatusCollection().getAllBitMasks();
        InstallationConfig installationConfig = new InstallationConfig(installationStatuses);
        return installationConfig.getConfigForMeter(meterIndex).size();
    }

    private int getMeterIndex(DiscoverMetersResponseStructure meterStatuses, String slaveSerialNumber) throws GarnetException {
        int meterIndex = 0;
        for (MeterSerialNumber serialNumber : meterStatuses.getMeterSerialNumberCollection()) {
            if (serialNumber.getSerialNumber().equals(slaveSerialNumber)) {
                return meterIndex;
            }
            meterIndex++;
        }
        throw new UnableToExecuteException("Slave " + slaveSerialNumber + " not found on the concentrator. The EIMaster topology is probably wrong.");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    protected Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) {
        return this.issueFactory.createWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueFactory.createWarning(pendingMessage, "DeviceMessage.failed",
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
        NON_TECHNICAL_LOSS(4, "Command executed with success, but one or more relays did not switch correctly"),
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