package com.elster.jupiter.time;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by borunova on 01.10.2014.
 */
public enum RelativeField {
    YEAR(1, ChronoField.YEAR),                      // Used to add or subtract years
    MONTH(2, ChronoUnit.MONTHS),                    // Used to add or subtract months
    WEEK(3, ChronoUnit.WEEKS),                      // Used to add or subtract weeks
    DAY(4, ChronoUnit.DAYS),                        // Used to add or subtract days
    HOUR(5, ChronoUnit.HOURS),                      // Used to add or subtract hours
    MINUTES(6, ChronoUnit.MINUTES),                 // Used to add or subtract minutes
    START_NOW(7),                                   // Marker field, indicates that date should be calculated when invoked
    DAY_IN_WEEK(8, ChronoField.DAY_OF_WEEK),        // Used to set fixed day of week
    DAY_IN_MONTH(9, ChronoField.DAY_OF_MONTH),      // Used to set fixed day of month
    MONTH_IN_YEAR(10, ChronoField.MONTH_OF_YEAR),   // Used to set fixed month of year
    HOUR_OF_DAY(11, ChronoField.HOUR_OF_AMPM),       // Used to set fixed hour of day
    MINUTES_OF_HOUR(12, ChronoField.MINUTE_OF_HOUR),// Used to set fixed minute of hour
    STRING_DAY_OF_WEEK(13, ChronoField.DAY_OF_WEEK),// Indicates that day of week should be represented as sting
    CURRENT_DAY_OF_MONTH(14),                       // Indicates that day of month should be calculated when invoked
    AMPM_OF_DAY(15, ChronoField.AMPM_OF_DAY);


    private ChronoUnit chronoUnit;
    private ChronoField chronoField;
    int id;
    private static Map<Integer, RelativeField> ID_MAP = new HashMap<Integer, RelativeField>();

    private RelativeField(int id) {
        this.id = id;
    }

    private RelativeField(int id, ChronoUnit chronoUnit) {
        this(id);
        this.chronoUnit = chronoUnit;
    }

    private RelativeField(int id, ChronoField chronoField) {
        this(id);
        this.chronoField = chronoField;
    }

    public boolean isChronoUnitBased() {
        return this.getChronoUnit() != null;
    }

    public boolean isChronoFieldBased() {
        return this.getChronoField() != null;
    }

    public ChronoUnit getChronoUnit() {
        return this.chronoUnit;
    }

    public ChronoField getChronoField() {
        return this.chronoField;
    }

    public int getId() {
        return this.id;
    }

    public boolean isValid(long value) {
        return chronoField != null ? chronoField.range().isValidValue(value) : value >= 0;
    }

    public static RelativeField from(int id) {
        if(ID_MAP.isEmpty()) {
            initialiseIdMap();
        }
        return ID_MAP.get(id);
    }

    private static void initialiseIdMap() {
        for (RelativeField relativeField : RelativeField.values()) {
            ID_MAP.put(relativeField.getId(), relativeField);
        }
    }
}
