package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging.cosem.XemexWatchTalkImageTransfer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author sva
 * @since 24/03/2014 - 9:00
 */
public class XemexWatchTalkMessageExecutor extends Dsmr23MessageExecutor {

    private static final String NORESUME = "noresume";

    public XemexWatchTalkMessageExecutor(AbstractSmartNtaProtocol protocol, TariffCalendarFinder tariffCalendarFinder, TariffCalendarExtractor tariffCalendarExtractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder, NumberLookupExtractor numberLookupExtractor, NumberLookupFinder numberLookupFinder) {
        super(protocol, tariffCalendarFinder, tariffCalendarExtractor, messageFileExtractor, deviceMessageFileFinder, numberLookupExtractor, numberLookupFinder);
    }

    @Override
    protected Dsmr23MbusMessageExecutor getMbusMessageExecutor() {
        return new XemexWatchTalkMbusMessageExecutor(getProtocol());
    }

    @Override
    protected void upgradeCalendar(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Activity calendar");

        String name = messageHandler.getTOUCalendarName();
        String activateDate = messageHandler.getTOUActivationDate();
        String codeTable = messageHandler.getTOUCodeTable();
        String userFile = messageHandler.getTOUUserFile();

        if ((codeTable == null) && (userFile == null)) {
            throw new IllegalArgumentException("CodeTable-ID AND UserFile-ID can not be both empty.");
        } else if ((codeTable != null) && (userFile != null)) {
            throw new IllegalArgumentException("CodeTable-ID AND UserFile-ID can not be both filled in.");
        }

        if (codeTable != null) {
            TariffCalendar tariffCalendar = this.getCalendarFinder().from(codeTable).orElseThrow(() -> new IllegalArgumentException("No CodeTable defined with id '" + codeTable + "'"));
            XemexActivityCalendarParser activityCalendarParser = new XemexActivityCalendarParser(tariffCalendar, this.getCalendarExtractor());
            activityCalendarParser.parse();

            ActivityCalendar ac = getCosemObjectFactory().getActivityCalendar(getMeterConfig().getActivityCalendar().getObisCode());
            ac.writeDayProfileTablePassive(activityCalendarParser.getDayProfile());
            ac.writeWeekProfileTablePassive(activityCalendarParser.getWeekProfile());
            ac.writeSeasonProfilePassive(activityCalendarParser.getSeasonProfile());

            if (name != null) {
                if (name.length() > 8) {
                    name = name.substring(0, 8);
                }
                ac.writeCalendarNamePassive(OctetString.fromString(name));
            }
            if (activateDate != null) {
                ac.writeActivatePassiveCalendarTime(new OctetString(convertUnixToGMTDateTime(activateDate).getBEREncodedByteArray(), 0));
            } else {
                ac.activateNow();
            }
        } else if (userFile != null) {
            throw new IllegalArgumentException("ActivityCalendar by userfile is not supported yet.");
        } else {
            // should never get here
            throw new IllegalArgumentException("CodeTable-ID AND UserFile-ID can not be both empty.");
        }
    }

    @Override
    protected void upgradeSpecialDays(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Special Days table");

        String codeTable = messageHandler.getSpecialDaysCodeTable();
        if (codeTable == null) {
            throw new IOException("CodeTable-ID can not be empty.");
        } else {
            TariffCalendar tariffCalendar = this.getCalendarFinder().from(codeTable).orElseThrow(() -> new IllegalArgumentException("No CodeTable defined with id '" + codeTable + "'"));
            Array sdArray = new Array();
            SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
            List<TariffCalendarExtractor.CalendarRule> rules = this.getCalendarExtractor().rules(tariffCalendar);
            XemexActivityCalendarParser activityCalendarParser = new XemexActivityCalendarParser(tariffCalendar, this.getCalendarExtractor());
            activityCalendarParser.parse();

            int dayIndex = 1;
            for (int i = 0; i < rules.size(); i++) {
                TariffCalendarExtractor.CalendarRule cc = rules.get(i);
                if (!cc.seasonId().isPresent()) {
                    OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((cc.year() == -1) ? 0xff : ((cc.year() >> 8) & 0xFF)), (byte) ((cc.year() == -1) ? 0xff : (cc.year()) & 0xFF),
                            (byte) ((cc.month() == -1) ? 0xFF : cc.month()), (byte) ((cc.day() == -1) ? 0xFF : cc.day()),
                            (byte) ((cc.dayOfWeek() == -1) ? 0xFF : cc.dayOfWeek())});
                    Unsigned8 dayType = new Unsigned8(Integer.parseInt(cc.dayTypeId()));
                    Structure struct = new Structure();
                    struct.addDataType(new Unsigned16(dayIndex));
                    struct.addDataType(os);
                    struct.addDataType(dayType);
                    sdArray.addDataType(struct);
                    dayIndex++;
                }
            }

            if (sdArray.nrOfDataTypes() != 0) {
                sdt.writeSpecialDays(sdArray);
            }
        }
    }

    @Override
    protected void doFirmwareUpgrade(MessageHandler messageHandler, MessageEntry messageEntry) throws IOException, InterruptedException {
        log(Level.INFO, "Handling message Firmware upgrade");
        boolean resume = true;     //Default always resume
        if ((messageEntry.getTrackingId() != null) && messageEntry.getTrackingId().toLowerCase().contains(NORESUME)) {
            resume = false;
        }

        String userFileID = messageHandler.getUserFileId();
        if (!ParseUtils.isInteger(userFileID)) {
            String str = "Not a valid entry for the userFile.";
            throw new IOException(str);
        }

        Optional<DeviceMessageFile> deviceMessageFile = getMessageFileFinder().from(userFileID);

        if (!deviceMessageFile.isPresent()) {
            String str = "Not a valid entry for the userfileID " + userFileID;
            throw new IOException(str);
        }

        byte[] imageData = getMessageFileExtractor().binaryContents(deviceMessageFile.get());
        XemexWatchTalkImageTransfer it = new XemexWatchTalkImageTransfer(getCosemObjectFactory().getProtocolLink());
        it.setBooleanValue(getBooleanValue());
        it.setUsePollingInit(true);    //Poll image transfer status during init
        it.setInitPollingDelay(2000);
        it.setInitPollingRetries(15);
        it.setDelayBeforeSendingBlocks(0);  // Not needed, because we use polling

        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(30);
        String imageIdentifier = messageHandler.getImageIdentifier();

        if (resume) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }

        if (imageIdentifier != null && imageIdentifier.length() > 0) {
            it.upgrade(imageData, true, imageIdentifier, false);
        } else {
            it.upgrade(imageData, true);
        }
        if (messageHandler.getActivationDate().equalsIgnoreCase("")) { // Do an execute now
            it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
            log(Level.INFO, "Activating the image");
            it.imageActivation();
        } else if (!messageHandler.getActivationDate().equalsIgnoreCase("")) {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            String strDate = messageHandler.getActivationDate();
            Array dateArray = convertUnixToDateTimeArray(strDate);
            sas.writeExecutionTime(dateArray);
        }
    }

    /**
     * Default value, subclasses can override. This value is used to set the image_transfer_enable attribute.
     */
    protected int getBooleanValue() {
        return 0xFF;
    }
}