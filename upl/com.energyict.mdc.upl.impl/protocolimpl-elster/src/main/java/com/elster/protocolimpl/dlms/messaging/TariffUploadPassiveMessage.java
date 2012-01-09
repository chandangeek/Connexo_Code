package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.classes.class11.SpecialDayEntry;
import com.elster.dlms.cosem.classes.class20.*;
import com.elster.dlms.cosem.simpleobjectmodel.*;
import com.elster.dlms.types.basic.*;
import com.elster.protocolimpl.dlms.tariff.CodeObjectValidator;
import com.elster.protocolimpl.dlms.tariff.CodeTableBase64Parser;
import com.elster.protocolimpl.dlms.tariff.objects.*;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;

public class TariffUploadPassiveMessage extends AbstractDlmsMessage {

    private static final String MESSAGE_DESCRIPTION = "Upload a new passive tariff scheme";

    public static final String MESSAGE_TAG = "UploadPassiveTariff";
    public static final String ATTR_ACTIVATION_TIME = "ActivationTime";
    public static final String ATTR_CODE_TABLE_ID = "CodeTableId";

    public TariffUploadPassiveMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        try {
            String activationTimeAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_ACTIVATION_TIME);
            String codeTableBase64Attr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_CODE_TABLE_ID);
            Date activationTime = validateAndGetActivationTime(activationTimeAttr);
            CodeObject codeObject = validateAndGetCodeObject(codeTableBase64Attr);
            writeCodeTable(codeObject, activationTime);
        } catch (IOException e) {
            throw new BusinessException("Unable to write new tariff to device: " + e.getMessage(), e);
        }
    }

    private CodeObject validateAndGetCodeObject(String codeTableBase64) throws IOException, BusinessException {
        CodeObject codeObject = CodeTableBase64Parser.getCodeTableFromBase64(codeTableBase64);
        CodeObjectValidator.validateCodeObject(codeObject);
        return codeObject;
    }

    private Date validateAndGetActivationTime(String activationTimeAttr) throws BusinessException {
        if (activationTimeAttr == null) {
            throw new BusinessException("Activation time cannot be 'null'!");
        }
        Date activationDate = ProtocolTools.getEpochDateFromString(activationTimeAttr);
        if (activationDate == null) {
            throw new BusinessException("Unable to get Date object from activation time attribute [" + activationTimeAttr + "]. Maybe wrong format.");
        }
        Date after = ProtocolTools.getDateFromYYYYMMddhhmmss("2000-01-01 00:00:00");
        if (activationDate.before(after)) {
            throw new BusinessException("Invalid activation date [" + activationDate + "]. Date should be after [" + after + "]");
        }
        return activationDate;
    }

    private void writeCodeTable(CodeObject codeObject, Date activationDate) throws BusinessException, IOException {

        Calendar activeDate = GregorianCalendar.getInstance(getExecutor().getDlms().getTimeZone());
        activeDate.setTime(activationDate);

        try {
            writeSpecialDaysTable(codeObject.getCalendars(), activeDate);
        } catch (IOException ioe) {
            throw new BusinessException("Error writing CodeTable: SpecialDaysTable - " + ioe.getMessage());
        }

        SeasonAndDate billingPeriod;
        billingPeriod = getNextBillingPeriod(codeObject.getSeasonSet().getSeasons(), activeDate);
        if (billingPeriod == null) {
            throw new BusinessException("Error writing CodeTable: no BillingPeriod after " + activeDate.getTime().toString());
        }

        String cmp = getDayTypeAbbreviation(billingPeriod.getSeason().getName());
        if (cmp == null) {
            throw new BusinessException("Error writing CodeTable: wrong billing period name " + billingPeriod);
        }

        DayProfile d1 = getDayProfile(codeObject.getDayTypes(), 1, "WEEKDAY", cmp);
        DayProfile d2 = getDayProfile(codeObject.getDayTypes(), 2, "SATURDAY", cmp);
        DayProfile d3 = getDayProfile(codeObject.getDayTypes(), 3, "HOLIDAY", cmp);

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();
        SimpleActivityCalendarObject activityCalendar = objectManager.getSimpleCosemObject(
                Ek280Defs.ACTIVITY_CALENDAR, SimpleActivityCalendarObject.class);

        SimpleActivityCalendarProfiles profilesPassive = activityCalendar.getProfilesPassive();

        profilesPassive.setCalendarName(billingPeriod.getSeason().getName());

        Calendar cal = billingPeriod.getCalendar();
        DlmsDate dd = new DlmsDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        DlmsTime dt = new DlmsTime(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 0);
        Season s1 = new Season("The season", new DlmsDateTime(dd, dt), "Profile_1");

        // only one season setable
        profilesPassive.setSeasons(new Season[]{s1});

        WeekProfile w1 = new WeekProfile("Profile_1", 1, 1, 1, 1, 1, 2, 3); //Nur genau das Profil scheint m�glich zu sein.
        profilesPassive.setWeekProfiles(new WeekProfile[]{w1}); //Genau 1 Profil möglich

        profilesPassive.setDayProfiles(new DayProfile[]{d1, d2, d3});

        activityCalendar.setActivatePassiveCalendarTime(new DlmsDateTime(dd, dt));
    }

    private void writeSpecialDaysTable(List<CodeCalendarObject> calendars, Calendar activeDate) throws IOException {

        TreeMap<Calendar, CodeCalendarObject> holidays = new TreeMap<Calendar, CodeCalendarObject>();

        Calendar tst = (Calendar) activeDate.clone();
        tst.set(Calendar.HOUR_OF_DAY, 0);
        tst.set(Calendar.MINUTE, 0);
        tst.set(Calendar.SECOND, 0);
        tst.set(Calendar.MILLISECOND, 0);

        for (CodeCalendarObject entry : calendars) {
            if (entry.getDayTypeName().toUpperCase().contains("HOLIDAY")) {
                int year = entry.getYear();
                if (year < 0) {
                    int startYear = tst.get(Calendar.YEAR);
                    for (int entryYear = startYear; entryYear <= startYear + 1; entryYear++) {
                        addEntryToHolidays(holidays, entry, entryYear, tst);
                    }
                } else {
                    addEntryToHolidays(holidays, entry, year, tst);
                }
            }
        }

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();
        final SimpleSpecialDaysTable specialDaysTable = objectManager.getSimpleCosemObject(Ek280Defs.SPECIAL_DAYS_TABLE, SimpleSpecialDaysTable.class);

        System.out.println("Holiday list:");
        ArrayList<SpecialDayEntry> specialDays = new ArrayList<SpecialDayEntry>();
        int i = 0;
        for (Map.Entry<Calendar, CodeCalendarObject> entry : holidays.entrySet()) {
            Calendar c = entry.getKey();
            DlmsDate dd = new DlmsDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
            System.out.println(dd.toString());
            specialDays.add(new SpecialDayEntry(++i, dd, 3));
            if (i >= 20) {
                break;
            }
        }
        specialDaysTable.setEntries(specialDays.toArray(new SpecialDayEntry[specialDays.size()]));
    }

    private void addEntryToHolidays(TreeMap<Calendar, CodeCalendarObject> holidays, CodeCalendarObject entry, int year, Calendar testDate) {
        Calendar c = new GregorianCalendar(year, entry.getMonth() - 1, entry.getDay());
        if (!c.before(testDate) && !holidays.containsKey(c)) {
            holidays.put(c, entry);
        }
    }

    private SeasonAndDate getNextBillingPeriod(List<SeasonObject> seasons, Calendar activeDate) {

        TreeMap<Calendar, SeasonObject> ssos = new TreeMap<Calendar, SeasonObject>();

        for (SeasonObject entry : seasons) {
            List<SeasonTransitionObject> transitions = entry.getTransitions();
            for (SeasonTransitionObject transition : transitions) {
                ssos.put(transition.getStartCalendar(), entry);
            }
        }

        for (Map.Entry<Calendar, SeasonObject> entry : ssos.entrySet()) {
            if (entry.getKey().after(activeDate)) {
                return new SeasonAndDate(entry.getValue(), entry.getKey());
            }
        }
        return null;
    }

    private String getDayTypeAbbreviation(String billingPeriod) {
        if (billingPeriod.trim().toUpperCase().endsWith("1")) {
            return "PT1";
        }
        if (billingPeriod.trim().toUpperCase().endsWith("2")) {
            return "PT2";
        }
        return null;
    }

    private DayProfile getDayProfile(List<CodeDayTypeObject> dayTypes, int dayTypeNo, String day, String cmp) {

        List<CodeDayTypeDefObject> bands;

        List<DayProfile.DayProfileAction> dayProfileActions = new ArrayList<DayProfile.DayProfileAction>();
        for (CodeDayTypeObject dayType : dayTypes) {
            String name = dayType.getExternalName().toUpperCase();
            if (name.startsWith(cmp) && name.endsWith(day)) {
                bands = dayType.getDayTypeDefs();
                CodeDayTypeDefObject lastEntry = bands.get(bands.size() - 1);
                for (CodeDayTypeDefObject dayTypeDef : bands) {
                    if ((dayTypeDef.getFrom() == 0) && (bands.size() > 1)
                            && (dayTypeDef.getCodeValue() == lastEntry.getCodeValue())) {
                        continue;
                    }

                    int hour = dayTypeDef.getFrom() / 10000;
                    int minute = (dayTypeDef.getFrom() % 10000) / 100;
                    int sec = (dayTypeDef.getFrom() % 100);

                    DayProfile.DayProfileAction dpa = new DayProfile.DayProfileAction(new DlmsTime(hour, minute, sec, 0),
                            new ObisCode(0, 0, 10, 0, 100, 255), dayTypeDef.getCodeValue());

                    dayProfileActions.add(dpa);
                }
            }
        }
        return new DayProfile(dayTypeNo, dayProfileActions.toArray(new DayProfile.DayProfileAction[dayProfileActions.size()]));
    }


    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_CODE_TABLE_ID, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_ACTIVATION_TIME, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    class SeasonAndDate {

        private SeasonObject season;
        private Calendar cal;

        public SeasonAndDate(SeasonObject season, Calendar cal) {
            this.season = season;
            this.cal = cal;
        }

        public SeasonObject getSeason() {
            return this.season;
        }

        public Calendar getCalendar() {
            return this.cal;
        }
    }
}
