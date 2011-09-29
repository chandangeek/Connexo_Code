package com.elster.protocolimpl.lis200.registers;

import com.elster.protocolimpl.lis200.objects.GenericArchiveObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * cache of monthly archives
 * <p/>
 * User: heuckeg
 * Date: 06.04.11
 * Time: 16:04
 */
@SuppressWarnings({"unused"})
public class HistoricalArchive {

    private final GenericArchiveObject archiveDevice;

    /* Hash map holding archive data.
     * first string is date YYYYMM
     * second string is archive line
     */
    private final HashMap<String, String> archiveData;

    String units;

    public HistoricalArchive(GenericArchiveObject archiveDevice) {
        archiveData = new HashMap<String, String>();
        this.archiveDevice = archiveDevice;
        units = null;
    }

    public String getArchiveLine(Calendar date) {

        Calendar workDate = (Calendar) date.clone();

        // Datum zu String
        DateFormat dfs = new SimpleDateFormat("yyyyMM");
        dfs.setTimeZone(workDate.getTimeZone());
        String s = dfs.format(workDate.getTime());

        if (archiveData.containsKey(s)) {
            return archiveData.get(s);
        }

        workDate.set(Calendar.DAY_OF_MONTH, 1);
        workDate.set(Calendar.HOUR_OF_DAY, 0);
        workDate.set(Calendar.MINUTE, 0);
        workDate.set(Calendar.SECOND, 0);
        workDate.set(Calendar.MILLISECOND, 0);

        Date from = workDate.getTime();
        workDate.add(Calendar.MONTH, 1);
        Date to = workDate.getTime();

        // Abfrage von ... bis (1.m.yyyy 00:00:00 - 1.m+1.yyyy 00:00:00)
        String line;
        try {
            line = archiveDevice.getIntervals(from, to, 1);
        } catch (IOException e) {
            line = null;
        }

        archiveData.put(s, line);
        return line;
    }

    public String getUnits() {
        if (units == null) {
            try {
                units = this.archiveDevice.getUnits();
            } catch (IOException ignore) {
            }
        }
        return units;
    }
}