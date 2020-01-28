package com.energyict.protocolimplv2.dlms.ei7.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.DateTimeOctetString;
import com.energyict.dlms.cosem.NbiotPushSetup;
import com.energyict.dlms.cosem.NbiotPushScheduler;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a2.messages.A2MessageExecutor;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.threshold;

public class EI7MessageExecutor extends A2MessageExecutor {

    private static final ObisCode PUSH_SCHEDULER_1 = ObisCode.fromString("0.1.15.0.4.255");
    private static final ObisCode PUSH_SCHEDULER_2 = ObisCode.fromString("0.2.15.0.4.255");
    private static final ObisCode PUSH_SCHEDULER_3 = ObisCode.fromString("0.3.15.0.4.255");
    private static final ObisCode PUSH_SCHEDULER_4 = ObisCode.fromString("0.4.15.0.4.255");

    private static final ObisCode PUSH_SETUP_1 = ObisCode.fromString("0.1.25.9.0.255");
    private static final ObisCode PUSH_SETUP_2 = ObisCode.fromString("0.2.25.9.0.255");
    private static final ObisCode PUSH_SETUP_3 = ObisCode.fromString("0.3.25.9.0.255");
    private static final ObisCode PUSH_SETUP_4 = ObisCode.fromString("0.4.25.9.0.255");

    private static final ObisCode ORPHAN_STATE = ObisCode.fromString("0.0.94.39.10.255");

    public EI7MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        try {
            if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.WRITE_PUSH_SCHEDULER)) {
                writePushScheduler(pendingMessage);
            } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.WRITE_PUSH_SETUP)) {
                writePushSetup(pendingMessage);
            } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.WRITE_ORPHAN_STATE_THRESHOLD)) {
                writeOrphanStateThreshold(pendingMessage);
            } else {
                super.executeMessage(pendingMessage, collectedMessage);
            }
        } catch (ParseException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            }
        return collectedMessage;
    }

    private void writePushScheduler(OfflineDeviceMessage pendingMessage) throws IOException, ParseException {
        ObisCode pushObisCode = null;
        int schedulerNumber = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.schedulerNumber));
        switch(schedulerNumber){
            case 1: pushObisCode = PUSH_SCHEDULER_1; break;
            case 2: pushObisCode = PUSH_SCHEDULER_2; break;
            case 3: pushObisCode = PUSH_SCHEDULER_3; break;
            case 4: pushObisCode = PUSH_SCHEDULER_4; break;
        }
        NbiotPushScheduler nbiotPushScheduler = getCosemObjectFactory().getNbiotPushScheduler(pushObisCode);
        String executionTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.executionTime).getValue();
        Calendar executionTimeCal = Calendar.getInstance(getProtocol().getTimeZone());
        executionTimeCal.setTimeInMillis(Long.valueOf(executionTime));
        nbiotPushScheduler.writeExecutionTime(new Calendar[]{executionTimeCal});
    }
    private void writePushSetup(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode pushObisCode = null;
        int pushNumber = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.pushNumber));
        switch(pushNumber){
            case 1: pushObisCode = PUSH_SETUP_1; break;
            case 2: pushObisCode = PUSH_SETUP_2; break;
            case 3: pushObisCode = PUSH_SETUP_3; break;
            case 4: pushObisCode = PUSH_SETUP_4; break;
        }
        String pushObjectList = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.pushObjectList);
        String transportTypeString = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.transportTypeAttributeName);
        String destinationAddress = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.destinationAddressAttributeName);
        String messageTypeString = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.messageTypeAttributeName);
        String communicationWindow = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.communicationWindow);
        String randomizationStartInterval = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.randomizationStartInterval);
        String numberOfRetries = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.numberOfRetries);
        String repetitionDelay = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.repetitionDelay);

        NbiotPushSetup nbiotPushSetup = getCosemObjectFactory().getNbiotPushSetup(pushObisCode);
        Array objectDefinitionArray = new Array();
        objectDefinitionArray.addDataType(OctetString.fromString(pushObjectList));
        nbiotPushSetup.writePushObjectList(new Array(objectDefinitionArray));
        int transportType = NetworkConnectivityMessage.TransportType.valueOf(transportTypeString).getId();
        int messageType = NetworkConnectivityMessage.MessageType.valueOf(messageTypeString).getId();
        nbiotPushSetup.writeSendDestinationAndMethod(transportType, destinationAddress, messageType);
        Array windowArray = new Array();
        windowArray.addDataType(OctetString.fromString(communicationWindow));
        nbiotPushSetup.writeCommunicationWindow(new Array(windowArray));
        nbiotPushSetup.writeRandomizationStartInterval(new Unsigned16(Integer.parseInt(randomizationStartInterval)));
        nbiotPushSetup.writeNumberOfRetries(new Unsigned8(Integer.parseInt(numberOfRetries)));
        nbiotPushSetup.writeRepetitionDelay(new Unsigned16(Integer.parseInt(repetitionDelay)));
    }

    private void writeOrphanStateThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        int numberOfDays = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, threshold));
        getCosemObjectFactory().getNbiotOrphanState(ORPHAN_STATE).writeThresholds(numberOfDays);
    }
}
