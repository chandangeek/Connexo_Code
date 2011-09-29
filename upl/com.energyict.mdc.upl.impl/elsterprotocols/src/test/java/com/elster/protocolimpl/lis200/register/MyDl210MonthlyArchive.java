package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.objects.ClockObject;
import com.elster.protocolimpl.lis200.objects.GenericArchiveObject;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.util.*;

/**
 * User: heuckeg
 * Date: 15.04.11
 * Time: 15:54
 */
public class MyDl210MonthlyArchive extends GenericArchiveObject {

    private TimeZone timeZone = TimeZone.getTimeZone("GMT+1");

    private HashMap<Date, String> archiveData;

    private String units;

    public MyDl210MonthlyArchive(ProtocolLink link, int archiveInstance) {

        super(link, archiveInstance);

        archiveData = new HashMap<Date, String>();

        if (archiveInstance == 1) {
            // (GONr)(AONr)(ZEIT)(V1)(V1.P)(V1M.L)(ZEIT)(STAT)(V1T.L)(ZEIT)(STAT)(ST.1)(ST.SY)(Er.Ch)

            // archive type 50
            units = "()()()(m3)(m3)(m3)()()(m3)()()()()()";
            archiveData.put(makeDate("2010-02-01,06:00:00"), "(11388)(15)(2010-02-01,06:00:00)(1173533)(171302)(80)(2010-01-19,12:00:00)(0)(845)(2010-01-20,06:00:00)(0)(14)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-03-01,06:00:00"), "(12604)(16)(2010-03-01,06:00:00)(1183651)(181420)(74)(2010-02-22,13:00:00)(0)(786)(2010-02-03,06:00:00)(0)(14)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-04-01,06:00:00"), "(14119)(17)(2010-04-01,06:00:00)(1195986)(193755)(79)(2010-03-05,12:00:00)(0)(894)(2010-03-11,06:00:00)(0)(14)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-05-01,06:00:00"), "(15732)(18)(2010-05-01,06:00:00)(1208214)(205983)(73)(2010-04-22,13:00:00)(0)(663)(2010-04-15,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-06-01,06:00:00"), "(16971)(19)(2010-06-01,06:00:00)(1217048)(214817)(74)(2010-05-05,10:00:00)(0)(653)(2010-05-12,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-07-01,06:00:00"), "(18277)(20)(2010-07-01,06:00:00)(1229382)(227151)(72)(2010-06-17,11:00:00)(0)(670)(2010-06-18,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-08-01,06:00:00"), "(19461)(21)(2010-08-01,06:00:00)(1240434)(238203)(65)(2010-07-01,10:00:00)(0)(607)(2010-07-09,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-09-01,06:00:00"), "(20880)(22)(2010-09-01,06:00:00)(1254240)(252009)(67)(2010-08-31,10:00:00)(0)(679)(2010-09-01,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-10-01,06:00:00"), "(22409)(23)(2010-10-01,06:00:00)(1267717)(265486)(69)(2010-09-28,11:00:00)(0)(656)(2010-09-28,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-11-01,06:00:00"), "(24135)(24)(2010-11-01,06:00:00)(1278917)(276686)(72)(2010-10-27,17:00:00)(0)(654)(2010-10-09,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2010-12-01,06:00:00"), "(25841)(25)(2010-12-01,06:00:00)(1292570)(290339)(71)(2010-11-19,15:00:00)(0)(735)(2010-11-20,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2011-01-01,06:00:00"), "(27574)(26)(2011-01-01,06:00:00)(1299358)(297127)(72)(2010-12-03,12:00:00)(0)(670)(2010-12-08,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2011-02-01,06:00:00"), "(29398)(27)(2011-02-01,06:00:00)(1308311)(306080)(67)(2011-01-28,11:00:00)(0)(754)(2011-01-25,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2011-03-01,06:00:00"), "(30999)(28)(2011-03-01,06:00:00)(1322237)(320006)(75)(2011-02-18,08:00:00)(0)(1185)(2011-02-25,06:00:00)(0)(0)(0)(CRC Ok)");
            archiveData.put(makeDate("2011-04-01,06:00:00"), "(32556)(29)(2011-04-01,06:00:00)(1335888)(333657)(73)(2011-03-18,11:00:00)(0)(676)(2011-03-26,06:00:00)(0)(0)(0)(CRC Ok)");
        }
    }

    private Date makeDate(String rawDate) {
        return ClockObject.parseCalendar(rawDate, false, timeZone).getTime();
    }

    @Override
    public String getIntervals(Date from, Date to, int blockCount) {

        StringBuilder s = new StringBuilder();
        Date[] dates = archiveData.keySet().toArray(new Date[0]);
        java.util.Arrays.sort(dates);
        for (Date d: dates) {
            if ((d.getTime() >= from.getTime()) &&
                    (d.getTime() < to.getTime())) {
                        s.append(archiveData.get(d));
                        s.append("\n\r");
            }
        }
        return s.toString();
    }

    @Override
    public String getUnits(){
        return units;
    }
}
