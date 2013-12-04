package com.energyict.protocolimplv2.nta.abstractnta.messages;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.meterdata.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaProtocol;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Provides functionality to handle the {@link com.energyict.mdc.messages.DeviceMessageSpec}s.
 * <p/>
 *
 * @author sva
 * @since 29/11/13 - 15:20
 */
public abstract class AbstractNtaMessageExecutor {

    private AbstractNtaProtocol protocol;

    protected AbstractNtaMessageExecutor(AbstractNtaProtocol protocol) {
        this.protocol = protocol;
    }

    public abstract CollectedMessageList executePendingMessages(final List<OfflineDeviceMessage> pendingMessages);

    public abstract CollectedMessageList updateSentMessages(final List<OfflineDeviceMessage> sentMessages);

    protected CosemObjectFactory getCosemObjectFactory() {
        return getProtocol().getDlmsSession().getCosemObjectFactory();
    }

    protected DLMSMeterConfig getMeterConfig() {
        return getProtocol().getDlmsSession().getMeterConfig();
    }

    public AbstractNtaProtocol getProtocol() {
        return protocol;
    }

    /**
     * Searches for the {@link com.energyict.mdw.offline.OfflineDeviceMessageAttribute}
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
        throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("DeviceMessage didn't contain a value found for MessageAttribute " + attributeName));
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

    protected CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    protected CollectedMessage createCollectedMessageWithLoadProfileData(OfflineDeviceMessage message, CollectedLoadProfile collectedLoadProfile) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithLoadProfileData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLoadProfile);
    }

    protected CollectedMessage createCollectedMessageWithRegisterData(OfflineDeviceMessage message, List<CollectedRegister> collectedRegisters) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithRegisterData(new DeviceIdentifierById(message.getDeviceId()), new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedRegisters);
    }

    protected CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineDeviceMessage pendingMessage) {
        CollectedRegister deviceRegister = new DefaultDeviceRegister(new RegisterDataIdentifierByObisCodeAndDevice(registerValue.getObisCode(), new DeviceIdentifierById(pendingMessage.getDeviceId())));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime());
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
}
