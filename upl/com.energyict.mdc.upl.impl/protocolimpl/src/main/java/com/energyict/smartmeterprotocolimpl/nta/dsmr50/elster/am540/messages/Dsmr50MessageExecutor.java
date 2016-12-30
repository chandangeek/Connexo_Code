package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Mostly reuses the DSMR4.0 functionality, but changes a few things.
 * Important: for DSMR5.0, the new keys (message to change AK and/or EK) are used immediately, instead of only at the start of the next message!
 * Also, when changing the encryption key, the framecounter is restarted.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/06/2014 - 15:20
 */
public class Dsmr50MessageExecutor extends Dsmr40MessageExecutor {

    private static final String RESUME = "resume";

    public Dsmr50MessageExecutor(AbstractSmartNtaProtocol protocol, TariffCalendarFinder calendarFinder, DeviceMessageFileFinder messageFileFinder, Extractor extractor) {
        super(protocol, calendarFinder, extractor, messageFileFinder);
    }

    @Override
    protected boolean isResume(MessageEntry messageEntry) {
        return (messageEntry.getTrackingId() != null) && (messageEntry.getTrackingId().toLowerCase().contains(RESUME));
    }

    protected ActivityCalendarMessage getActivityCalendarParser(TariffCalendar calendar) {
        return new Dsmr50ActivityCalendarParser(calendar, this.getExtractor(), getMeterConfig());
    }

    @Override
    protected void upgradeSpecialDays(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Special Days table");

        String codeTable = messageHandler.getSpecialDaysCodeTable();

        if (codeTable == null) {
            throw new IOException("CodeTable-ID can not be empty.");
        } else {

            TariffCalendar tariffCalendar = this.getCalendarFinder().from(codeTable).orElseThrow(() -> new IllegalArgumentException("No CodeTable defined with id '" + codeTable + "'"));
            List<Extractor.CalendarRule> rules = this.getExtractor().rules(tariffCalendar);
            Array sdArray = new Array();
            SpecialDaysTable specialDaysTable = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
            //Create day type IDs (incremental 0-based)
            Map<String, Integer> dayTypeIds = new HashMap<>();  //Map the DB id's of the day types to a proper 0-based index that can be used in the AXDR array
            List<Extractor.CalendarDayType> dayTypes = this.getExtractor().dayTypes(tariffCalendar);
            for (int dayTypeIndex = 0; dayTypeIndex < dayTypes.size(); dayTypeIndex++) {
                Extractor.CalendarDayType dayType = dayTypes.get(dayTypeIndex);
                if (!dayTypeIds.containsKey(dayType.id())) {
                    dayTypeIds.put(dayType.id(), dayTypeIndex);
                }
            }

            int dayIndex = 0;
            for (Extractor.CalendarRule calendar : rules) {
                if (!calendar.seasonId().isPresent()) {
                    OctetString timeStamp = OctetString.fromByteArray(new byte[]{(byte) ((calendar.year() == -1) ? 0xff : ((calendar.year() >> 8) & 0xFF)), (byte) ((calendar.year() == -1) ? 0xff : (calendar
                            .year()) & 0xFF),
                            (byte) ((calendar.month() == -1) ? 0xFF : calendar.month()), (byte) ((calendar.day() == -1) ? 0xFF : calendar.day()),
                            (byte) ((calendar.dayOfWeek() == -1) ? 0xFF : calendar.dayOfWeek())});
                    Unsigned8 dayType = new Unsigned8(dayTypeIds.get(calendar.dayTypeId()));
                    Structure specialDayStructure = new Structure();
                    specialDayStructure.addDataType(new Unsigned16(dayIndex));
                    specialDayStructure.addDataType(timeStamp);
                    specialDayStructure.addDataType(dayType);
                    sdArray.addDataType(specialDayStructure);
                    dayIndex++;
                }
            }

            sdArray = sort(sdArray);

            if (sdArray.nrOfDataTypes() != 0) {
                specialDaysTable.writeSpecialDays(sdArray);
            }
        }
    }

    protected Array sort(Array specialDaysArray) {
        return specialDaysArray;    //No sorting needed, subclasses can override
    }

    @Override
    protected void changeAuthenticationKey(MessageHandler messageHandler) throws IOException {
        protocol.getLogger().info("Received [ChangeAuthenticationKeyMessage], wrapped key is '" + ProtocolTools.getHexStringFromBytes(messageHandler.getNewAuthenticationKey(), "") + "'");
        Array authenticationKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(messageHandler.getNewAuthenticationKey()));
        authenticationKeyArray.addDataType(keyData);

        protocol.getDlmsSession().getCosemObjectFactory().getSecuritySetup().transferGlobalKey(authenticationKeyArray);

        //Update the key in the security provider, it is used instantly
        protocol.getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(messageHandler.getPlainAuthenticationKey(), ""));
    }

    @Override
    protected void changeGlobalKey(MessageHandler messageHandler) throws IOException {
        String oldGlobalKey = ProtocolTools.getHexStringFromBytes(protocol.getDlmsSession().getProperties().getSecurityProvider().getGlobalKey(), "");
        protocol.getLogger().info("Received [ChangeEncryptionKeyMessage], wrapped key is '" + ProtocolTools.getHexStringFromBytes(messageHandler.getNewEncryptionKey(), "") + "'");
        Array encryptionKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: encryptionKey (global key)
        keyData.addDataType(OctetString.fromByteArray(messageHandler.getNewEncryptionKey()));
        encryptionKeyArray.addDataType(keyData);

        protocol.getDlmsSession().getCosemObjectFactory().getSecuritySetup().transferGlobalKey(encryptionKeyArray);

        //Update the key in the security provider, it is used instantly
        protocol.getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(messageHandler.getPlainEncryptionKey(), ""));

        //Reset frame counter, only if a different key has been written
        if (!oldGlobalKey.equalsIgnoreCase(ProtocolTools.getHexStringFromBytes(ProtocolTools.getBytesFromHexString(messageHandler.getPlainEncryptionKey(), ""), ""))) {
            protocol.getDlmsSession().getAso().getSecurityContext().setFrameCounter(1);
        }
    }

    /**
     * Convert the given unix activation date to a proper DateTimeArray<br/>
     * The conversion is slightly different then the DSMR4.0 implementation:
     * <ul>
     * <li>Day of week should be masked 0xFF</li>
     * <li>Milliseconds should be masked 0xFF</li>
     * </ul>
     */
    @Override
    protected Array convertActivationDateUnixToDateTimeArray(String strDate) throws IOException {
        try {
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
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Could not parse " + strDate + " to a long value");
        }
    }

    protected void doFirmwareUpgrade(MessageHandler messageHandler, MessageEntry messageEntry) throws IOException, InterruptedException {
        log(Level.INFO, "Handling message Firmware upgrade");

        String userFileID = messageHandler.getUserFileId();
        if (!ParseUtils.isInteger(userFileID)) {
            String str = "Not a valid entry for the userFile.";
            throw new IOException(str);
        }
        DeviceMessageFile deviceMessageFile = this.getMessageFileFinder().from(userFileID).orElseThrow(() -> new IllegalArgumentException("Not a valid entry for the userfileID " + userFileID));
        byte[] imageData = this.getExtractor().binaryContents(deviceMessageFile);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        if (isResume(messageEntry)) {
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
        it.setCheckNumberOfBlocksInPreviousSession(((Dsmr50Properties)getProtocol().getProperties()).getCheckNumberOfBlocksDuringFirmwareResume());
        String imageIdentifier = messageHandler.getImageIdentifier();
        if (imageIdentifier != null && !imageIdentifier.isEmpty()) {
            it.upgrade(imageData, false, imageIdentifier, false);
        } else {
            it.upgrade(imageData, false);
        }
        if ("".equalsIgnoreCase(messageHandler.getActivationDate())) { // Do an execute now
            try {
                it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
                log(Level.INFO, "Activating the image");
                it.imageActivation();
            } catch (DataAccessResultException e) {
                if (isTemporaryFailure(e)) {
                    log(Level.INFO, "Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
                } else {
                    throw e;
                }
            }
        } else if (!"".equalsIgnoreCase(messageHandler.getActivationDate())) {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            String strDate = messageHandler.getActivationDate();
            Array dateArray = convertActivationDateUnixToDateTimeArray(strDate);
            sas.writeExecutionTime(dateArray);
        }
    }
}