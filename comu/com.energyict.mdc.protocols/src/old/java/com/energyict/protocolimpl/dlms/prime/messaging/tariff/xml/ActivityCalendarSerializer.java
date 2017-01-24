package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 7/09/12
 * Time: 12:58
 * Author: khe
 */
public class ActivityCalendarSerializer {

    private static final ObisCode CONTRACT_THRESHOLDS = ObisCode.fromString("0.1.94.34.0.255");
    private static final ObisCode ACTIVITY_CALENDAR_OBISCODE = ObisCode.fromString("0.0.13.0.0.255");
    private static final ObisCode SPECIAL_DAYS_TABLE_OBISCODE = ObisCode.fromString("0.0.11.0.0.255");

    private CosemObjectFactory cosemObjectFactory;
    private TimeZone timeZone = null;
    private String result = "";
    private S23 s23;

    public ActivityCalendarSerializer(CosemObjectFactory cof, TimeZone timeZone) {
        this.cosemObjectFactory = cof;
        this.timeZone = timeZone;
    }

    public void parseAllContractDefinitions() throws IOException {

        try {

            s23 = new S23();
            s23.setFh(getTimeStampFromDate());


            //PCact
            PCact pCact = new PCact();
            OctetString activationDateOctetString = getContractActivatedTimeStamp(1);
            pCact.setActDate(getTimeStampFromOctetString(activationDateOctetString));
            Contrato1 contrato1 = new Contrato1();

            long tr1 = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 1)).getValue();
            long tr2 = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 2)).getValue();
            long tr3 = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 3)).getValue();
            long tr4 = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 4)).getValue();
            long tr5 = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 5)).getValue();
            long tr6 = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 6)).getValue();

            contrato1.setTR1((int) tr1);
            contrato1.setTR2((int) tr2);
            contrato1.setTR3((int) tr3);
            contrato1.setTR4((int) tr4);
            contrato1.setTR5((int) tr5);
            contrato1.setTR6((int) tr6);

            pCact.setContrato1(contrato1);
            pCact.setPResidual(null);

            s23.setPCact(pCact);


            //PCLatent
            PCLatent pcLatent = new PCLatent();
            OctetString activationTime = getActivityCalendar(1).readActivatePassiveCalendarTime();
            pcLatent.setActDate(getTimeStampFromOctetString(activationTime));
            Contrato1 contrato1Passive = new Contrato1();

            long tr1Passive = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 11)).getValue();
            long tr2Passive = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 12)).getValue();
            long tr3Passive = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 13)).getValue();
            long tr4Passive = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 14)).getValue();
            long tr5Passive = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 15)).getValue();
            long tr6Passive = cosemObjectFactory.getRegister(ProtocolTools.setObisCodeField(CONTRACT_THRESHOLDS, 4, (byte) 16)).getValue();

            contrato1Passive.setTR1((int) tr1Passive);
            contrato1Passive.setTR2((int) tr2Passive);
            contrato1Passive.setTR3((int) tr3Passive);
            contrato1Passive.setTR4((int) tr4Passive);
            contrato1Passive.setTR5((int) tr5Passive);
            contrato1Passive.setTR6((int) tr6Passive);

            pcLatent.setContrato1(contrato1Passive);
            pcLatent.setPResidual(null);

            s23.setPCLatent(pcLatent);


            //ActiveCalendars
            Contract activeContract1 = getActiveContract(1);
            Contract activeContract2 = getActiveContract(2);
            Contract activeContract3 = getActiveContract(3);

            ActiveCalendars activeCalendars = new ActiveCalendars();
            activeCalendars.addContract(activeContract1);
            activeCalendars.addContract(activeContract2);
            activeCalendars.addContract(activeContract3);
            s23.setActiveCalendars(activeCalendars);

            //PassiveCalendars
            Contract passiveContract1 = getPassiveContract(1);
            Contract passiveContract2 = getPassiveContract(2);
            Contract passiveContract3 = getPassiveContract(3);

            LatentCalendars passiveCalendars = new LatentCalendars();
            passiveCalendars.addContract(passiveContract1);
            passiveCalendars.addContract(passiveContract2);
            passiveCalendars.addContract(passiveContract3);
            s23.setLatentCalendars(passiveCalendars);

            result = ProtocolTools.serializeToBase64(s23);

        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }

    }

    private OctetString getContractActivatedTimeStamp(int contractId) throws IOException {
        final Data data = cosemObjectFactory.getData(ObisCode.fromString("1.0.94.34.130.255"));
        final Array activatedTimeStamps = data.getValueAttr(Array.class);
        return activatedTimeStamps.getDataType(contractId - 1, OctetString.class);
    }

    private Contract getActiveContract(int contractId) throws IOException {
        ActivityCalendar activityCalendar = getActivityCalendar(contractId);
        SpecialDaysTable specialDaysTable = getSpecialDaysTableActive(contractId);

        Contract contract = new Contract();
        contract.setC(contractId);
        contract.setCalendarType(1);
        contract.setCalendarName(getHexStringFromOctetString(activityCalendar.readCalendarNameActive()));
        contract.setActDate(getTimeStampFromOctetString(getContractActivatedTimeStamp(contractId)));

        //Season array
        Array seasons = activityCalendar.readSeasonProfileActive();
        for (AbstractDataType abstractSeason : seasons) {
            Season season = new Season();
            Structure seasonStructure = (Structure) abstractSeason;
            OctetString name = (OctetString) seasonStructure.getDataType(0);
            OctetString start = (OctetString) seasonStructure.getDataType(1);
            OctetString week = (OctetString) seasonStructure.getDataType(2);
            season.setName(getHexStringFromOctetString(name));
            season.setStart(getHexStringFromOctetString(start));
            season.setWeek(getHexStringFromOctetString(week));

            contract.addSeason(season);
        }

        //Week array
        Array weeks = activityCalendar.readWeekProfileTableActive();
        for (AbstractDataType abstractWeek : weeks) {
            Week week = new Week();
            Structure weekStructure = (Structure) abstractWeek;
            OctetString name = (OctetString) weekStructure.getDataType(0);
            Unsigned8 day1 = (Unsigned8) weekStructure.getDataType(1);
            Unsigned8 day2 = (Unsigned8) weekStructure.getDataType(2);
            Unsigned8 day3 = (Unsigned8) weekStructure.getDataType(3);
            Unsigned8 day4 = (Unsigned8) weekStructure.getDataType(4);
            Unsigned8 day5 = (Unsigned8) weekStructure.getDataType(5);
            Unsigned8 day6 = (Unsigned8) weekStructure.getDataType(6);
            Unsigned8 day7 = (Unsigned8) weekStructure.getDataType(7);
            week.setName(getHexStringFromOctetString(name));
            week.setWeek(pad(day1) + pad(day2) + pad(day3) + pad(day4) + pad(day5) + pad(day6) + pad(day7));

            contract.addWeek(week);
        }

        //Day array
        Array days = activityCalendar.readDayProfileTableActive();
        for (AbstractDataType abstractDay : days) {
            Day day = new Day();
            Structure dayStructure = (Structure) abstractDay;
            Unsigned8 dayId = (Unsigned8) dayStructure.getDataType(0);
            day.setId(dayId.getValue());
            Array actions = (Array) dayStructure.getDataType(1);
            for (AbstractDataType abstractAction : actions) {
                Change change = new Change();
                Structure actionStructure = (Structure) abstractAction;
                OctetString startTime = (OctetString) actionStructure.getDataType(0);
                Unsigned16 tariffRate = (Unsigned16) actionStructure.getDataType(2);
                change.setHour(getHexStringFromOctetString(startTime));
                change.setTariffRate(tariffRate.getValue());
                day.addChange(change);
            }

            contract.addDay(day);
        }

        //Special days
        Array specialDaysArray = specialDaysTable.readSpecialDays();
        for (AbstractDataType abstractSpecialDay : specialDaysArray) {
            SpecialDays specialDay = new SpecialDays();
            Structure specialDayStructure = (Structure) abstractSpecialDay;
            OctetString date = (OctetString) specialDayStructure.getDataType(1);
            byte[] dateTimeBytes = ProtocolTools.concatByteArrays(date.getOctetStr(), new byte[7]);
            dateTimeBytes[9] = (byte) 0x80;
            dateTimeBytes[10] = (byte) 0x00;

            if (timeZone.inDaylightTime(new AXDRDateTime(OctetString.fromByteArray(dateTimeBytes).getBEREncodedByteArray()).getValue().getTime())) {
                dateTimeBytes[11] = (byte) 0x80;
            }

            Unsigned8 dayId = (Unsigned8) specialDayStructure.getDataType(2);
            specialDay.setDT(getTimeStampFromOctetString(OctetString.fromByteArray(dateTimeBytes)));
            specialDay.setDTCard(isIgnoreYear(date) ? "Y" : "N");
            specialDay.setDayID(dayId.getValue());
            contract.addSpecialDay(specialDay);
        }

        return contract;
    }

    private Contract getPassiveContract(int contractId) throws IOException {
        ActivityCalendar activityCalendar = getActivityCalendar(contractId);
        SpecialDaysTable specialDaysTable = getSpecialDaysTablePassive(contractId);

        Contract contract = new Contract();
        contract.setC(contractId);
        contract.setCalendarType(1);
        contract.setCalendarName(getHexStringFromOctetString(activityCalendar.readCalendarNamePassive()));
        contract.setActDate(getHexStringFromOctetString(activityCalendar.readActivatePassiveCalendarTime()));

        //Season array
        Array seasons = activityCalendar.readSeasonProfilePassive();
        for (AbstractDataType abstractSeason : seasons) {
            Season season = new Season();
            Structure seasonStructure = (Structure) abstractSeason;
            OctetString name = (OctetString) seasonStructure.getDataType(0);
            OctetString start = (OctetString) seasonStructure.getDataType(1);
            OctetString week = (OctetString) seasonStructure.getDataType(2);
            season.setName(getHexStringFromOctetString(name));
            season.setStart(getHexStringFromOctetString(start));
            season.setWeek(getHexStringFromOctetString(week));

            contract.addSeason(season);
        }

        //Week array
        Array weeks = activityCalendar.readWeekProfileTablePassive();
        for (AbstractDataType abstractWeek : weeks) {
            Week week = new Week();
            Structure weekStructure = (Structure) abstractWeek;
            OctetString name = (OctetString) weekStructure.getDataType(0);
            Unsigned8 day1 = (Unsigned8) weekStructure.getDataType(1);
            Unsigned8 day2 = (Unsigned8) weekStructure.getDataType(2);
            Unsigned8 day3 = (Unsigned8) weekStructure.getDataType(3);
            Unsigned8 day4 = (Unsigned8) weekStructure.getDataType(4);
            Unsigned8 day5 = (Unsigned8) weekStructure.getDataType(5);
            Unsigned8 day6 = (Unsigned8) weekStructure.getDataType(6);
            Unsigned8 day7 = (Unsigned8) weekStructure.getDataType(7);
            week.setName(getHexStringFromOctetString(name));
            week.setWeek(pad(day1) + pad(day2) + pad(day3) + pad(day4) + pad(day5) + pad(day6) + pad(day7));

            contract.addWeek(week);
        }

        //Day array
        Array days = activityCalendar.readDayProfileTablePassive();
        for (AbstractDataType abstractDay : days) {
            Day day = new Day();
            Structure dayStructure = (Structure) abstractDay;
            Unsigned8 dayId = (Unsigned8) dayStructure.getDataType(0);
            day.setId(dayId.getValue());
            Array actions = (Array) dayStructure.getDataType(1);
            for (AbstractDataType abstractAction : actions) {
                Change change = new Change();
                Structure actionStructure = (Structure) abstractAction;
                OctetString startTime = (OctetString) actionStructure.getDataType(0);
                Unsigned16 tariffRate = (Unsigned16) actionStructure.getDataType(2);
                change.setHour(getHexStringFromOctetString(startTime));
                change.setTariffRate(tariffRate.getValue());
                day.addChange(change);
            }

            contract.addDay(day);
        }

        //Special days
        Array specialDaysArray = specialDaysTable.readSpecialDays();
        for (AbstractDataType abstractSpecialDay : specialDaysArray) {
            SpecialDays specialDay = new SpecialDays();
            Structure specialDayStructure = (Structure) abstractSpecialDay;
            OctetString date = (OctetString) specialDayStructure.getDataType(1);
            byte[] dateTimeBytes = ProtocolTools.concatByteArrays(date.getOctetStr(), new byte[7]);
            dateTimeBytes[9] = (byte) 0x80;
            dateTimeBytes[10] = (byte) 0x00;

            if (timeZone.inDaylightTime(new AXDRDateTime(OctetString.fromByteArray(dateTimeBytes).getBEREncodedByteArray()).getValue().getTime())) {
                dateTimeBytes[11] = (byte) 0x80;
            }

            Unsigned8 dayId = (Unsigned8) specialDayStructure.getDataType(2);
            specialDay.setDT(getTimeStampFromOctetString(OctetString.fromByteArray(dateTimeBytes)));
            specialDay.setDTCard(isIgnoreYear(date) ? "Y" : "N");
            specialDay.setDayID(dayId.getValue());
            contract.addSpecialDay(specialDay);
        }

        return contract;
    }

    /**
     * Check if the first 2 bytes (reprenting the year) of the octetstring are 0xFF
     */
    private boolean isIgnoreYear(OctetString date) {
        return (((date.getOctetStr()[0] & 0xFF) == 0xFF) && ((date.getOctetStr()[1] & 0xFF) == 0xFF));
    }

    private String pad(Unsigned8 day) {
        String dayString = String.valueOf(day.getValue());
        while (dayString.length() < 2) {
            dayString = "0" + dayString;
        }
        return dayString;
    }

    private ActivityCalendar getActivityCalendar(int contractId) throws IOException {
        ObisCode obisCode = ProtocolTools.setObisCodeField(ACTIVITY_CALENDAR_OBISCODE, 4, (byte) contractId);
        return cosemObjectFactory.getActivityCalendar(obisCode);
    }

    private SpecialDaysTable getSpecialDaysTableActive(int contractId) throws IOException {
        ObisCode obisCode = ProtocolTools.setObisCodeField(SPECIAL_DAYS_TABLE_OBISCODE, 4, (byte) contractId);
        return cosemObjectFactory.getSpecialDaysTable(obisCode);
    }

    private SpecialDaysTable getSpecialDaysTablePassive(int contractId) throws IOException {
        ObisCode obisCode = ProtocolTools.setObisCodeField(SPECIAL_DAYS_TABLE_OBISCODE, 4, (byte) (contractId + 3));
        return cosemObjectFactory.getSpecialDaysTable(obisCode);
    }

    private String getHexStringFromOctetString(OctetString time) {
        return ProtocolTools.getHexStringFromBytes(time.getOctetStr(), "");
    }

    private String getTimeStampFromDate() {
        return getTimeStampFromDate(null);
    }

    private String getTimeStampFromOctetString(OctetString date) throws IOException {
        final DateTime dateTime = new DateTime(date.getBEREncodedByteArray(), 0, timeZone);
        return getTimeStampFromDate(dateTime.getValue().getTime());
    }

    private String getTimeStampFromDate(Date date) {
        Calendar cal = Calendar.getInstance(timeZone);
        if (date != null) {
            cal.setTime(date);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        formatter.setCalendar(cal);
        String formattedDate = formatter.format(cal.getTime());
        if (timeZone.inDaylightTime(cal.getTime())) {
            formattedDate += "S";
        } else {
            formattedDate += "W";
        }
        return formattedDate;
    }

    public String getFullXML() {
        return result;
    }
}