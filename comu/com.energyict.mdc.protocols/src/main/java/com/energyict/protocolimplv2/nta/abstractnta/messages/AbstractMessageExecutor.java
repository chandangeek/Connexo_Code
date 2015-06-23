package com.energyict.protocolimplv2.nta.abstractnta.messages;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Provides functionality to handle the DeviceMessageSpec.
 * <p/>
 *
 * @author sva
 * @since 29/11/13 - 15:20
 */
public abstract class AbstractMessageExecutor {

    private final AbstractDlmsProtocol protocol;
    private final IssueService issueService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final CollectedDataFactory collectedDataFactory;

    public AbstractMessageExecutor(AbstractDlmsProtocol protocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory) {
        this.protocol = protocol;
        this.issueService = issueService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.collectedDataFactory = collectedDataFactory;
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    protected MdcReadingTypeUtilService getReadingTypeUtilService() {
        return readingTypeUtilService;
    }

    public abstract CollectedMessageList executePendingMessages(final List<OfflineDeviceMessage> pendingMessages);

    /**
     * Nothing to do here. Sub classes can override.
     */
    public CollectedMessageList updateSentMessages(final List<OfflineDeviceMessage> sentMessages) {
        return getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return getProtocol().getDlmsSession().getCosemObjectFactory();
    }

    protected DLMSMeterConfig getMeterConfig() {
        return getProtocol().getDlmsSession().getMeterConfig();
    }

    public AbstractDlmsProtocol getProtocol() {
        return protocol;
    }

    /**
     * Searches for the {@link OfflineDeviceMessageAttribute}
     * in the given {@link OfflineDeviceMessage} which corresponds
     * with the provided name. If no match is found, then an IOException is thrown
     * attribute is returned
     *
     * @param offlineDeviceMessage the offlineDeviceMessage to search in
     * @param attributeName        the name of the OfflineDeviceMessageAttribute to return
     * @return the requested OfflineDeviceMessageAttribute
     */
    protected String getDeviceMessageAttributeValue(OfflineDeviceMessage offlineDeviceMessage, String attributeName) throws IOException {
        for (OfflineDeviceMessageAttribute offlineDeviceMessageAttribute : offlineDeviceMessage.getDeviceMessageAttributes()) {
            if (offlineDeviceMessageAttribute.getName().equals(attributeName)) {
                return offlineDeviceMessageAttribute.getDeviceMessageAttributeValue();
            }
        }
        throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR,new ProtocolException("DeviceMessage didn't contain a value found for MessageAttribute " + attributeName));
    }

    protected int getMbusAddress(String serialNumber) {
        return this.protocol.getPhysicalAddressFromSerialNumber(serialNumber) - 1;
    }

    protected MBusClient getMBusClient(String serialNumber) throws IOException {
        return getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MbusClientAttributes.VERSION9);
    }

    private ObisCode getMbusClientObisCode(String serialNumber) throws IOException {
        return getMeterConfig().getMbusClient(getMbusAddress(serialNumber)).getObisCode();
    }

    protected Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) {
        return this.issueService.newIssueCollector().addWarning(
                pendingMessage,
                com.energyict.mdc.protocol.api.MessageSeeds.DEVICEMESSAGE_NOT_SUPPORTED.getKey(),
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueService.newIssueCollector().addWarning(
                pendingMessage,
                com.energyict.mdc.protocol.api.MessageSeeds.DEVICEMESSAGE_FAILED.getKey(),
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    protected CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return getCollectedDataFactory().createCollectedMessage(message.getIdentifier());
    }

    protected CollectedMessage createCollectedMessageWithLoadProfileData(OfflineDeviceMessage message, CollectedLoadProfile collectedLoadProfile) {
        return getCollectedDataFactory().createCollectedMessageWithLoadProfileData(message.getIdentifier(), collectedLoadProfile);
    }

    protected CollectedMessage createCollectedMessageWithRegisterData(OfflineDeviceMessage message, List<CollectedRegister> collectedRegisters) {
        return getCollectedDataFactory().createCollectedMessageWithRegisterData(message.getDeviceIdentifier(), message.getIdentifier(), collectedRegisters);
    }

    protected CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineDeviceMessage pendingMessage) {
        CollectedRegister deviceRegister =
                getCollectedDataFactory()
                        .createDefaultCollectedRegister(
                                new RegisterDataIdentifierByObisCodeAndDevice(
                                        registerValue.getObisCode(),
                                        registerValue.getObisCode(),
                                        pendingMessage.getDeviceIdentifier()),
                                this.readingTypeUtilService.getReadingTypeFrom(
                                        registerValue.getObisCode(),
                                        registerValue.getQuantity().getUnit()));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(
                registerValue.getReadTime().toInstant(),
                registerValue.getFromTime().toInstant(),
                registerValue.getToTime().toInstant());
        return deviceRegister;
    }

    protected Array convertEpochToDateTimeArray(String strDate) {
        Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
        cal.setTimeInMillis(Long.parseLong(strDate));
        byte[] dateBytes = new byte[5];
        dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
        dateBytes[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
        dateBytes[2] = (byte) ((cal.get(Calendar.MONTH) & 0xFF) + 1);
        dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0xFF);
        dateBytes[4] = getDLMSDayOfWeek(cal);
        OctetString date = OctetString.fromByteArray(dateBytes);
        byte[] timeBytes = new byte[4];
        timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
        timeBytes[2] = (byte) 0x00;
        timeBytes[3] = (byte) 0x00;
        OctetString time = OctetString.fromByteArray(timeBytes);

        Array dateTimeArray = new Array();
        Structure strDateTime = new Structure();
        strDateTime.addDataType(time);
        strDateTime.addDataType(date);
        dateTimeArray.addDataType(strDateTime);
        return dateTimeArray;
    }

    private byte getDLMSDayOfWeek(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (dow == 0) {
            dow = 7;
        }
        return (byte) dow;
    }

    protected CollectedDataFactory getCollectedDataFactory() {
        return this.collectedDataFactory;
    }

    protected int getIntegerAttribute(OfflineDeviceMessage pendingMessage) {
        return Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
    }

    protected boolean getBooleanAttribute(OfflineDeviceMessage pendingMessage) {
        return Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
    }
}
