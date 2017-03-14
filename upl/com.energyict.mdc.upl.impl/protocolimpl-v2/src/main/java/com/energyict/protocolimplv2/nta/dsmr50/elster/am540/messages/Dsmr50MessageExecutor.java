package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessageExecutor;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.DSMR40ActivityCalendarController;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newWrappedEncryptionKeyAttributeName;

/**
 * Messsage executor implementation for DSMR 5.0
 * <p>
 * Mostly reuses the DSMR4.0 functionality, but changes a few things.<br/>
 * <b>Important:</b> for DSMR5.0, the new keys (message to change AK and/or EK) are used immediately, instead of only at the start of the next message!
 * Also, when changing the encryption key, the framecounter is restarted.
 *
 * @author sva
 * @since 6/01/2015 - 16:52
 */
public class Dsmr50MessageExecutor extends Dsmr40MessageExecutor {

    private static final String RESUME = "resume";
    private AbstractMessageExecutor mbusMessageExecutor;

    public Dsmr50MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected AbstractMessageExecutor getMbusMessageExecutor() {
        if (this.mbusMessageExecutor == null) {
            this.mbusMessageExecutor = new IDISMBusMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.mbusMessageExecutor;
    }

    @Override
    protected boolean isResume(OfflineDeviceMessage pendingMessage) {
        return (pendingMessage.getTrackingId() != null) && (pendingMessage.getTrackingId().toLowerCase().contains(RESUME));
    }

    @Override
    protected void upgradeFirmwareWithActivationDateAndImageIdentifier(OfflineDeviceMessage pendingMessage) throws IOException {
        String path = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getValue();   // Will return empty string if the MessageAttribute could not be found
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getValue(); // Will return empty string if the MessageAttribute could not be found

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();

        try (RandomAccessFile file = new RandomAccessFile(new File(path), "r")) {

            if (imageIdentifier.isEmpty()) {
                imageIdentifier = getImageIdentifierFromFile(file);
            }

            if (isResume(pendingMessage)) {
                int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
                if (lastTransferredBlockNumber > 0) {
                    it.setStartIndex(lastTransferredBlockNumber - 1);
                }
            }

            it.setBooleanValue(getBooleanValue());
            it.setUsePollingVerifyAndActivate(true);    //Poll verification
            it.setPollingDelay(10000);
            it.setPollingRetries(30);
            it.setDelayBeforeSendingBlocks(5000);
            it.setCheckNumberOfBlocksInPreviousSession(((Dsmr50Properties) getProtocol().getDlmsSessionProperties()).getCheckNumberOfBlocksDuringFirmwareResume());
            if (imageIdentifier.isEmpty()) {
                it.upgrade(new ImageTransfer.RandomAccessFileImageBlockSupplier(file), false, ImageTransfer.DEFAULT_IMAGE_NAME, false);
            } else {
                it.upgrade(new ImageTransfer.RandomAccessFileImageBlockSupplier(file), false, imageIdentifier, false);
            }
        }

        if (activationDate.isEmpty()) {
            try {
                it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
                it.imageActivation();
            } catch (DataAccessResultException e) {
                if (isTemporaryFailure(e)) {
                    getProtocol().getLogger().log(Level.INFO, "Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
                } else {
                    throw e;
                }
            }
        } else {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            Array dateArray = convertActivationDateEpochToDateTimeArray(activationDate);
            sas.writeExecutionTime(dateArray);
        }
    }

    private String getImageIdentifierFromFile(RandomAccessFile file) throws IOException {
        file.seek(0);
        byte[] fileStart = new byte[1024];
        file.readFully(fileStart);
        file.seek(0);

        int first = -1;
        int last = -1;
        for (int i = 0; i < fileStart.length; i++)
            if (first == -1) {
                if (isAsciiPrintable((char) (fileStart[i] & 0xFF))) {
                    first = i;
                }
            } else {
                if (!isAsciiPrintable((char) (fileStart[i] & 0xFF))) {
                    last = i;
                    i = fileStart.length;
                }
            }
        if (first == -1) {
            return "";
        } else if (last == -1) {
            last = fileStart.length;
        }
        return new String(fileStart, first, last - first);
    }

    /**
     * <p>Checks whether the character is ASCII 7 bit printable.</p>
     * <p>
     * <pre>
     *   CharUtils.isAsciiPrintable('a')  = true
     *   CharUtils.isAsciiPrintable('A')  = true
     *   CharUtils.isAsciiPrintable('3')  = true
     *   CharUtils.isAsciiPrintable('-')  = true
     *   CharUtils.isAsciiPrintable('\n') = false
     *   CharUtils.isAsciiPrintable('&copy;') = false
     * </pre>
     *
     * @param ch the character to check
     * @return true if between 32 and 126 inclusive
     */
    private boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    @Override
    protected void changeAuthenticationKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String newAuthenticationKey = getDeviceMessageAttributeValue(pendingMessage, newAuthenticationKeyAttributeName);
        String newWrappedAuthenticationKey = getDeviceMessageAttributeValue(pendingMessage, newWrappedAuthenticationKeyAttributeName);
        byte[] authenticationKeysBytes = ProtocolTools.getBytesFromHexString(newWrappedAuthenticationKey);

        Array authenticationKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(authenticationKeysBytes));
        authenticationKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(authenticationKeyArray);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(newAuthenticationKey, ""));
    }

    @Override
    protected void changeEncryptionKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String newHexKey = getDeviceMessageAttributeValue(pendingMessage, newEncryptionKeyAttributeName);
        String wrappedHexKey = getDeviceMessageAttributeValue(pendingMessage, newWrappedEncryptionKeyAttributeName);
        String oldHexKey = ProtocolTools.getHexStringFromBytes(getProtocol().getDlmsSession().getProperties().getSecurityProvider().getGlobalKey(), "");

        Array encryptionKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: encryptionKey (global key)
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey)));
        encryptionKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(encryptionKeyArray);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(newHexKey, ""));

        //Reset frame counter, only if a different key has been written
        if (!newHexKey.equalsIgnoreCase(oldHexKey)) {
            getProtocol().getDlmsSession().getAso().getSecurityContext().setFrameCounter(1);
        }
    }

    /**
     * Convert the given epoch activation date to a proper DateTimeArray<br/>
     * The conversion is slightly different then the DSMR4.0 implementation:
     * <ul>
     * <li>Day of week should be masked 0xFF</li>
     * <li>Milliseconds should be masked 0xFF</li>
     * </ul>
     */
    @Override
    protected Array convertActivationDateEpochToDateTimeArray(String strDate) {
        Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
        cal.setTimeInMillis(Long.parseLong(strDate));
        byte[] dateBytes = new byte[5];
        dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
        dateBytes[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
        dateBytes[2] = (byte) ((cal.get(Calendar.MONTH) & 0xFF) + 1);
        dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0xFF);
        dateBytes[4] = (byte) 0xFF;
        OctetString date = OctetString.fromByteArray(dateBytes);
        byte[] timeBytes = new byte[4];
        timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
        timeBytes[2] = (byte) 0x00;
        timeBytes[3] = (byte) 0xFF;
        OctetString time = OctetString.fromByteArray(timeBytes);

        Array dateTimeArray = new Array();
        Structure strDateTime = new Structure();
        strDateTime.addDataType(time);
        strDateTime.addDataType(date);
        dateTimeArray.addDataType(strDateTime);
        return dateTimeArray;
    }

    /**
     * The correct controller, who will use 0-based day profile IDs;
     * all other logic is equal to the DSMR40 variant
     */
    @Override
    protected DSMR40ActivityCalendarController getActivityCalendarController() {
        return new DSMR50ActivitiyCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone());
    }
}