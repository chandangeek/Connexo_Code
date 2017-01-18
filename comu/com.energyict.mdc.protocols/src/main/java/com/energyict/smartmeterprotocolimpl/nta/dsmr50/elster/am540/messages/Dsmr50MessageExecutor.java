package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.generic.messages.ArrayIndexGenerator;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

    public Dsmr50MessageExecutor(AbstractSmartNtaProtocol protocol, Clock clock, TopologyService topologyService, CalendarService calendarService) {
        super(protocol, clock, topologyService, calendarService);
    }

    @Override
    protected boolean isResume(MessageEntry messageEntry) {
        return (messageEntry.getTrackingId() != null) && (messageEntry.getTrackingId().toLowerCase().contains(RESUME));
    }

    protected ActivityCalendarMessage getActivityCalendarParser(Calendar calendar) {
        return new Dsmr50ActivityCalendarParser(calendar, getMeterConfig());
    }

    @Override
    protected void upgradeSpecialDays(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Special Days table");
        String calendarId = messageHandler.getSpecialDaysCalendar();
        if (calendarId == null) {
            throw new IOException("CodeTable-ID can not be empty.");
        } else {
            Calendar calendar = findCalendarOrThrowIOException(calendarId);
            final Map<Long, Integer> dayTypeIds = this.initializeDayTypeIds(calendar);
            final Array specialDays = new Array();
            ArrayIndexGenerator dayIndex = ArrayIndexGenerator.zeroBased();
            calendar
                .getExceptionalOccurrences()
                .forEach(exceptionalOccurrence ->
                        this.addAsSpecialDay(specialDays, dayIndex, dayTypeIds, exceptionalOccurrence));
            Array sortedSpecialDaysArray = sort(specialDays);
            SpecialDaysTable specialDaysTable = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
            if (sortedSpecialDaysArray.nrOfDataTypes() != 0) {
                specialDaysTable.writeSpecialDays(sortedSpecialDaysArray);
            }
        }
    }

    private void addAsSpecialDay(Array specialDays, ArrayIndexGenerator dayIndex, Map<Long, Integer> dayTypeIds, ExceptionalOccurrence exceptionalOccurrence) {
        if (exceptionalOccurrence instanceof RecurrentExceptionalOccurrence) {
            this.addAsSpecialDay(specialDays, dayIndex, dayTypeIds, (RecurrentExceptionalOccurrence) exceptionalOccurrence);
        } else {
            this.addAsSpecialDay(specialDays, dayIndex, dayTypeIds, (FixedExceptionalOccurrence) exceptionalOccurrence);
        }
    }

    private void addAsSpecialDay(Array specialDays, ArrayIndexGenerator dayIndex, Map<Long, Integer> dayTypeIds, RecurrentExceptionalOccurrence exceptionalOccurrence) {
        OctetString timeStamp =
                OctetString.fromByteArray(
                        new byte[]{
                                (byte) (0xff),
                                (byte) (0xff),
                                (byte) ((exceptionalOccurrence.getOccurrence().getMonth().getValue())),
                                (byte) ((exceptionalOccurrence.getOccurrence().getDayOfMonth())),
                                (byte) (0xFF)});
        this.addAsSpecialDay(specialDays, dayIndex, dayTypeIds.get(exceptionalOccurrence.getDayType().getId()), timeStamp);
    }

    private void addAsSpecialDay(Array specialDays, ArrayIndexGenerator dayIndex, Map<Long, Integer> dayTypeIds, FixedExceptionalOccurrence exceptionalOccurrence) {
        OctetString timeStamp =
                OctetString.fromByteArray(
                        new byte[]{
                                (byte) ((exceptionalOccurrence.getOccurrence().getYear() >> 8) & 0xFF),
                                (byte) ((exceptionalOccurrence.getOccurrence().getYear()) & 0xFF),
                                (byte) ((exceptionalOccurrence.getOccurrence().getMonth().getValue())),
                                (byte) (0xFF),
                                (byte)  (exceptionalOccurrence.getOccurrence().getDayOfWeek().getValue())});
        this.addAsSpecialDay(specialDays, dayIndex, dayTypeIds.get(exceptionalOccurrence.getDayType().getId()), timeStamp);
    }

    private void addAsSpecialDay(Array sdArray, ArrayIndexGenerator dayIndex, int dayTypeId, OctetString timeStamp) {
        Unsigned8 dayType = new Unsigned8(dayTypeId);
        Structure specialDayStructure = new Structure();
        specialDayStructure.addDataType(new Unsigned16(dayIndex.next()));
        specialDayStructure.addDataType(timeStamp);
        specialDayStructure.addDataType(dayType);
        sdArray.addDataType(specialDayStructure);
    }

    private Map<Long, Integer> initializeDayTypeIds(Calendar calendar) {
        ArrayIndexGenerator dayTypeIndex = ArrayIndexGenerator.zeroBased();
        return calendar
                    .getDayTypes()
                    .stream()
                    .collect(Collectors.toMap(
                            DayType::getId,
                            dayType1 -> dayTypeIndex.next()));
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
        protocol.getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(messageHandler.getPlainAuthenticationKey());
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
        protocol.getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(messageHandler.getPlainEncryptionKey());

        //Reset frame counter, only if a different key has been written
        if (!oldGlobalKey.equalsIgnoreCase(ProtocolTools.getHexStringFromBytes(messageHandler.getPlainEncryptionKey(), ""))) {
            protocol.getDlmsSession().getAso().getSecurityContext().setFrameCounter(1);
        }
    }
}