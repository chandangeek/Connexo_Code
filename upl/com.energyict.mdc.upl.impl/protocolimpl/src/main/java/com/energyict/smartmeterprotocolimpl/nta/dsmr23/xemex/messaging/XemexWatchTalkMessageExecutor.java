package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging.cosem.XemexWatchTalkImageTransfer;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * @author sva
 * @since 24/03/2014 - 9:00
 */
public class XemexWatchTalkMessageExecutor extends Dsmr23MessageExecutor {

    private static final String NORESUME = "noresume";

    public XemexWatchTalkMessageExecutor(AbstractSmartNtaProtocol protocol) {
        super(protocol);
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
            throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
        } else if ((codeTable != null) && (userFile != null)) {
            throw new IOException("CodeTable-ID AND UserFile-ID can not be both filled in.");
        }

        if (codeTable != null) {

            Code ct = mw().getCodeFactory().find(Integer.parseInt(codeTable));
            if (ct == null) {
                throw new IOException("No CodeTable defined with id '" + codeTable + "'");
            } else {
                XemexActivityCalendarParser activityCalendarParser = new XemexActivityCalendarParser(ct);
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
            }
        } else if (userFile != null) {
            throw new IOException("ActivityCalendar by userfile is not supported yet.");
        } else {
            // should never get here
            throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
        }
    }

    @Override
    protected void upgradeSpecialDays(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Special Days table");

        String codeTable = messageHandler.getSpecialDaysCodeTable();
        if (codeTable == null) {
            throw new IOException("CodeTable-ID can not be empty.");
        } else {
            Code ct = mw().getCodeFactory().find(Integer.parseInt(codeTable));
            if (ct == null) {
                throw new IOException("No CodeTable defined with id '" + codeTable + "'");
            } else {
                List calendars = ct.getCalendars();
                Array sdArray = new Array();

                SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
                XemexActivityCalendarParser activityCalendarParser = new XemexActivityCalendarParser(ct);
                activityCalendarParser.parse();

                int dayIndex = 1;
                for (int i = 0; i < calendars.size(); i++) {
                    CodeCalendar cc = (CodeCalendar) calendars.get(i);
                    if (cc.getSeason() == 0) {
                        OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((cc.getYear() == -1) ? 0xff : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xff : (cc.getYear()) & 0xFF),
                                (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                                (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek())});
                        Unsigned8 dayType = new Unsigned8(activityCalendarParser.getDayTypeName(cc.getDayType())); // Re-use the day type name of ActivityCalendarParser
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
        UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileID));
        if (uf == null) {
            String str = "Not a valid entry for the userfileID " + userFileID;
            throw new IOException(str);
        }

        byte[] imageData = uf.loadFileInByteArray();
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