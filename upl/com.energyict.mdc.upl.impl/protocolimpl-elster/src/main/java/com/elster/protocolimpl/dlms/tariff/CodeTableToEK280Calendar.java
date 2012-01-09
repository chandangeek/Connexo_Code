package com.elster.protocolimpl.dlms.tariff;

import com.elster.protocolimpl.dlms.tariff.objects.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * User: heuckeg
 * Date: 28.07.11
 * Time: 08:52
 */
public class CodeTableToEK280Calendar {

    public static void main(String[] args) throws IOException {
        File xmldata = new File("D:\\repositorysvn\\protocols\\branches\\8.9\\protocolimpl\\test4.xml");

        CodeObject co = CodeTableBase64Parser.getCodeTableFromBase64(xmldata);

        Calendar startDate = new GregorianCalendar(2011, 6, 7);
        int startYear = startDate.get(Calendar.YEAR);

        List<CodeCalendarObject> cal = co.getCalendars();
        TreeMap<Calendar, CodeCalendarObject> holidays = new TreeMap<Calendar, CodeCalendarObject>();

        System.out.println("All holidays in object:");
        for (CodeCalendarObject entry : cal) {
            System.out.println(entry.getDayTypeName() + " " + entry.getYear() + "-" + entry.getMonth() + "-" + entry.getDay());
            if (entry.getDayTypeName().toUpperCase().contains("HOLIDAY")) {
                int year = entry.getYear();
                if (year < 0) {
                    for (int i = startYear; i <= startYear + 1; i++) {
                        Calendar c = new GregorianCalendar(i, entry.getMonth() - 1, entry.getDay());
                        if (!holidays.containsKey(c)) {
                            holidays.put(c, entry);
                        }
                    }
                } else {
                    Calendar c = new GregorianCalendar(year, entry.getMonth() - 1, entry.getDay());
                    if (!holidays.containsKey(c)) {
                        holidays.put(c, entry);
                    }
                }
            }
        }

        System.out.println("Holiday list:");
        int i = 0;
        for (Map.Entry<Calendar, CodeCalendarObject> entry : holidays.entrySet()) {
            String s = entry.getKey().before(startDate) ? "--" : "up";
            System.out.println("" + (++i) + "  " + s + "   " + entry.getKey().get(Calendar.DAY_OF_MONTH) + "." + (entry.getKey().get(Calendar.MONTH) + 1) + "." + entry.getKey().get(Calendar.YEAR));
        }

        List<SeasonObject> seasons = co.getSeasonSet().getSeasons();
        TreeMap<Calendar, SeasonObject> ssos = new TreeMap<Calendar, SeasonObject>();

        for (SeasonObject entry : seasons) {
            List<SeasonTransitionObject> transitions = entry.getTransitions();

            for (SeasonTransitionObject transition : transitions) {
                ssos.put(transition.getStartCalendar(), entry);
            }
        }

        System.out.println("Season transitions:");
        i = 0;
        String usedBP = "";
        String mark;
        for (Map.Entry<Calendar, SeasonObject> entry : ssos.entrySet()) {
            mark = "   ";
            if ((usedBP.length() == 0) && (entry.getKey().after(startDate))) {
                usedBP = entry.getValue().getName();
                mark = "==>";
            }
            System.out.println("" + (++i) + "   " + mark + "   " + entry.getKey().get(Calendar.DAY_OF_MONTH) + "." + (entry.getKey().get(Calendar.MONTH) + 1) + "." + entry.getKey().get(Calendar.YEAR) + "   " + entry.getValue().getName());
        }

        System.out.println("");
        System.out.println("Used billing period: " + usedBP);
        String cmp = "";
        if (usedBP.trim().toUpperCase().endsWith("1")) {
            cmp = "PT1";
        }
        if (usedBP.trim().toUpperCase().endsWith("2")) {
            cmp = "PT2";
        }

        List<CodeDayTypeObject> dayTypes = co.getDayTypes();
        for (CodeDayTypeObject dayType : dayTypes) {
            String name = dayType.getExternalName().toUpperCase();
            if (name.startsWith(cmp)) {
                System.out.println(dayType.getExternalName());
                if (name.endsWith("WEEKDAY")) {
                    displayBeginTimes(dayType.getDayTypeDefs());
                }
                if (name.endsWith("SATURDAY")) {
                    displayBeginTimes(dayType.getDayTypeDefs());
                }
                if (name.endsWith("HOLIDAY")) {
                    displayBeginTimes(dayType.getDayTypeDefs());
                }
            }
        }
    }

    private static void displayBeginTimes(List<CodeDayTypeDefObject> dayTypeDefs) {
        for (CodeDayTypeDefObject dayTypeDef : dayTypeDefs) {
            if (dayTypeDef.getCodeValue() > 0) {
                String t = String.format("%06d", dayTypeDef.getFrom());
                System.out.println(String.format("   %d   %s:%s:%s", dayTypeDef.getCodeValue(), t.subSequence(0, 2), t.subSequence(2, 4), t.subSequence(4, 6)));
            }
        }
    }
}
