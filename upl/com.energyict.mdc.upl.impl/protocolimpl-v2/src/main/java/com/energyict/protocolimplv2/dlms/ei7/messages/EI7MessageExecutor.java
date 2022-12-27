package com.energyict.protocolimplv2.dlms.ei7.messages;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.CosemTime;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.NbiotPushScheduler;
import com.energyict.dlms.cosem.NbiotPushSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a2.messages.A2MessageExecutor;
import com.energyict.protocolimplv2.dlms.ei7.EI7Const;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.google.common.base.Strings;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Calendar;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.obisCode;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.startOfConventionalGasDay;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.threshold;

public class EI7MessageExecutor extends A2MessageExecutor {

    private static final ObisCode PUSH_SCHEDULER_1 = ObisCode.fromString("0.1.15.0.4.255");
    private static final ObisCode PUSH_SCHEDULER_2 = ObisCode.fromString("0.2.15.0.4.255");
    private static final ObisCode PUSH_SCHEDULER_3 = ObisCode.fromString("0.3.15.0.4.255");
    private static final ObisCode PUSH_SCHEDULER_4 = ObisCode.fromString("0.4.15.0.4.255");

    // NB-IoT Push Setup objects
    private static final ObisCode PUSH_SETUP_1 = ObisCode.fromString("0.1.25.9.0.255");
    private static final ObisCode PUSH_SETUP_2 = ObisCode.fromString("0.2.25.9.0.255");
    private static final ObisCode PUSH_SETUP_3 = ObisCode.fromString("0.3.25.9.0.255");
    private static final ObisCode PUSH_SETUP_4 = ObisCode.fromString("0.4.25.9.0.255");
    // GSM GPRS Push Setup objects
    private static final ObisCode PUSH_SETUP_11 = ObisCode.fromString("0.11.25.9.0.255");
    private static final ObisCode PUSH_SETUP_12 = ObisCode.fromString("0.12.25.9.0.255");
    private static final ObisCode PUSH_SETUP_13 = ObisCode.fromString("0.13.25.9.0.255");
    private static final ObisCode PUSH_SETUP_14 = ObisCode.fromString("0.14.25.9.0.255");

    private static final ObisCode ORPHAN_STATE = ObisCode.fromString("0.0.94.39.10.255");

    private static final ObisCode START_OF_CONVENTIONAL_GAS_DAY = ObisCode.fromString("7.0.0.9.3.255");

    protected final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    public EI7MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory);
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    @Override
    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_PUSH_SCHEDULER)) {
            writePushScheduler(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_PUSH_SETUP)) {
            writePushSetup(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_ORPHAN_STATE_THRESHOLD)) {
            writeOrphanStateThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_NETWORK_TIMEOUT)) {
            writeNetworkTimeout(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.KEY_RENEWAL_EI6_7)) {
            renewKey(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.GAS_DAY_CONFIGURATION)) {
            setGasDay(pendingMessage);
        } else {
            super.executeMessage(pendingMessage, collectedMessage);
        }
        // Error handling is done by the A2::executePendingMessages()
        return collectedMessage;
    }

    private void writeNetworkTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        final String timeoutObjectString = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.timeoutObject);
        final int sessionMaxDuration = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.sessionMaxDuration));
        final int inactivityTimeout = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.inactivityTimeoutAttributeName));
        final int networkAttachTimeout = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.networkAttachTimeout));

        Structure valueStructure = new Structure();
        valueStructure.addDataType(new Unsigned32(sessionMaxDuration));
        valueStructure.addDataType(new Unsigned32(inactivityTimeout));
        valueStructure.addDataType(new Unsigned32(networkAttachTimeout));

        final NetworkConnectivityMessage.TimeoutType timeoutType = NetworkConnectivityMessage.TimeoutType.valueOf(timeoutObjectString);

        ObisCode timeoutObisCode;
        switch (timeoutType) {
            case GPRS:
                timeoutObisCode = EI7Const.GPRS_TIMEOUT;
                break;
            case NBIOT:
                timeoutObisCode = EI7Const.NBIoT_TIMEOUT;
                break;
            default:
                throw new ProtocolException("Unknown timeout type, expected either GPRS or NBIOT.");
        }

        final Data timeoutObject = getCosemObjectFactory().getData(timeoutObisCode);
        timeoutObject.setValueAttr(valueStructure);
    }

    private void writePushScheduler(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode pushObisCode = null;
        int schedulerNumber = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.schedulerNumber));
        switch (schedulerNumber) {
            case 1:
                pushObisCode = PUSH_SCHEDULER_1;
                break;
            case 2:
                pushObisCode = PUSH_SCHEDULER_2;
                break;
            case 3:
                pushObisCode = PUSH_SCHEDULER_3;
                break;
            case 4:
                pushObisCode = PUSH_SCHEDULER_4;
                break;
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
        switch (pushNumber) {
            case 1:
                pushObisCode = PUSH_SETUP_1;
                break;
            case 2:
                pushObisCode = PUSH_SETUP_2;
                break;
            case 3:
                pushObisCode = PUSH_SETUP_3;
                break;
            case 4:
                pushObisCode = PUSH_SETUP_4;
                break;
            case 11:
                pushObisCode = PUSH_SETUP_11;
                break;
            case 12:
                pushObisCode = PUSH_SETUP_12;
                break;
            case 13:
                pushObisCode = PUSH_SETUP_13;
                break;
            case 14:
                pushObisCode = PUSH_SETUP_14;
                break;
        }
        String pushObjectList = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.pushObjectList);
        String transportTypeString = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.transportTypeAttributeName);
        // Destination address string should be like host:port
        String destinationAddress = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.destinationAddressAttributeName);
        String messageTypeString = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.messageTypeAttributeName);

        String communicationWindowStartTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.communicationWindowStartTime).getValue();
        String communicationWindowStopTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.communicationWindowStopTime).getValue();
        String randomizationStartInterval = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.randomizationStartInterval);
        String numberOfRetries = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.numberOfRetries);
        String repetitionDelay = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.repetitionDelay);

        NbiotPushSetup nbiotPushSetup = getCosemObjectFactory().getNbiotPushSetup(pushObisCode);

        Structure objectDefinitionStructure = new Structure();
        objectDefinitionStructure.addDataType(new Unsigned16(62));
        objectDefinitionStructure.addDataType(OctetString.fromObisCode(ObisCode.fromString(pushObjectList)));
        objectDefinitionStructure.addDataType(new Integer8(2));
        objectDefinitionStructure.addDataType(new Unsigned16(0));
        nbiotPushSetup.writePushObjectList(new Array(objectDefinitionStructure));

        int transportType = NetworkConnectivityMessage.TransportType.valueOf(transportTypeString).getId();

        // for GPRS transport type seems to be either 1 (UDP) or 0 (TCP)
        // for NB-IoT it is 255 (UDP) or 254 (TCP) - handled by default in the TransportType enum
        if (pushNumber > 4) {
            transportType = transportType == 255 ? 1 : 0;
        }

        int messageType = NetworkConnectivityMessage.MessageType.valueOf(messageTypeString).getId();
        nbiotPushSetup.writeSendDestinationAndMethod(transportType, destinationAddress, messageType);

        Array windowArray = new Array();
        if (Strings.isNullOrEmpty(communicationWindowStartTime) || Strings.isNullOrEmpty(communicationWindowStopTime)) {
            nbiotPushSetup.writeCommunicationWindow(new Array());
        } else {
            Calendar startTime = Calendar.getInstance(getProtocol().getTimeZone());
            startTime.setTimeInMillis(Long.valueOf(communicationWindowStartTime));
            windowArray.addDataType(new AXDRDateTime(startTime));
            Calendar stopTime = Calendar.getInstance(getProtocol().getTimeZone());
            stopTime.setTimeInMillis(Long.valueOf(communicationWindowStopTime));
            windowArray.addDataType(new AXDRDateTime(stopTime));
            nbiotPushSetup.writeCommunicationWindow(windowArray);
        }
        nbiotPushSetup.writeRandomizationStartInterval(new Unsigned16(Integer.parseInt(randomizationStartInterval)));
        nbiotPushSetup.writeNumberOfRetries(new Unsigned8(Integer.parseInt(numberOfRetries)));
        nbiotPushSetup.writeRepetitionDelay(new Unsigned16(Integer.parseInt(repetitionDelay)));
    }

    private void writeOrphanStateThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        int numberOfDays = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, threshold));
        getCosemObjectFactory().getNbiotOrphanState(ORPHAN_STATE).writeThresholds(numberOfDays);
    }

    protected void renewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String newAuthenticationKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, newAuthenticationKeyAttributeName).getValue();
        String newEncryptionKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, newEncryptionKeyAttributeName).getValue();
        String obis = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, obisCode).getValue();
        if (newAuthenticationKey == null || newEncryptionKey == null) {
            throw new ProtocolException("The security accessor corresponding to the provided keyAccessorType does not have a valid passive value.");
        }
        if (!newAuthenticationKey.equals(newEncryptionKey)) {
            throw new ProtocolException("The authentication key must match the encryption key.");
        }
        byte[] akWrappedKey = DatatypeConverter.parseHexBinary(newAuthenticationKey);
        getProtocol().getDlmsSession().getCosemObjectFactory().getAssociationLN(ObisCode.fromString(obis)).changeHLSSecret(akWrappedKey);
    }

    private void setGasDay(OfflineDeviceMessage pendingMessage) throws IOException {
        final String gasDayTime = getDeviceMessageAttributeValue(pendingMessage, startOfConventionalGasDay);

        final Calendar gasDayTimeCal = Calendar.getInstance();
        gasDayTimeCal.setTimeInMillis(Long.parseLong(gasDayTime));
        int hour = gasDayTimeCal.get(Calendar.HOUR);
        int minutes = gasDayTimeCal.get(Calendar.MINUTE);

        byte[] cosemTimeBytes = new byte[] {
                AxdrType.TIME27.getTag(),
                (byte) (hour),
                (byte) (minutes),
                (byte) (0x00), // seconds
                (byte) (0x00), // hundreds
        };

        final CosemTime cosemTime = new CosemTime(cosemTimeBytes, 0);
        Data gasDay = getProtocol().getDlmsSession().getCosemObjectFactory().getData(START_OF_CONVENTIONAL_GAS_DAY);
        gasDay.setValueAttr(cosemTime);
        getProtocol().journal("Start of gas day set to " + hour + ":" + minutes);
    }
}
