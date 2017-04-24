/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.elster.jupiter.calendar.DayType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CodeDayTypeObject implements Serializable {

    public static final String DEFAULT = "Default";
    public static final String FORCED_1 = "Forced 1";
    public static final String FORCED_2 = "Forced 2";
    public static final String FORCED_3 = "Forced 3";
    public static final String PT1_HOLIDAY = "PT1_Holiday";
    public static final String PT1_SATURDAY = "PT1_Saturday";
    public static final String PT1_WEEKDAY = "PT1_Weekday";
    public static final String PT2_HOLIDAY = "PT2_Holiday";
    public static final String PT2_SATURDAY = "PT2_Saturday";
    public static final String PT2_WEEKDAY = "PT2_Weekday";

    public static final String[] DAYTYPE_NAMES = new String[]{
            FORCED_1,
            FORCED_2,
            FORCED_3,
            PT1_WEEKDAY,
            PT1_SATURDAY,
            PT1_HOLIDAY,
            PT2_WEEKDAY,
            PT2_SATURDAY,
            PT2_HOLIDAY,
            DEFAULT
    };

    private long id;
    private String name;
    private String externalName;
    private List<CodeDayTypeDefObject> dayTypeDefs = new ArrayList<>();

    public static CodeDayTypeObject from(DayType dayType) {
        CodeDayTypeObject dt = new CodeDayTypeObject();
        dt.setId(dayType.getId());
        dt.setName(dayType.getName());
        dt.setExternalName(null);
        dt.setDayTypeDefs(
                dayType.getEventOccurrences()
                        .stream()
                        .map(CodeDayTypeDefObject::from)
                        .collect(Collectors.toList()));
        return dt;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CodeDayTypeDefObject> getDayTypeDefs() {
        return dayTypeDefs;
    }

    public void setDayTypeDefs(List<CodeDayTypeDefObject> dayTypeDefs) {
        this.dayTypeDefs = dayTypeDefs;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CodeDayTypeObject" +
               "{dayTypeDefs=" + dayTypeDefs +
               ", id=" + id +
               ", name='" + name + '\'' +
               ", externalName='" + externalName + '\'' +
               '}';
    }

    public boolean isDefault() {
        return DEFAULT.equals(getName());
    }

    public boolean isWeekday() {
        return (getName() != null) && (getName().equals(PT1_WEEKDAY) || getName().equals(PT2_WEEKDAY));
    }

    public boolean isSaturday() {
        return (getName() != null) && (getName().equals(PT1_SATURDAY) || getName().equals(PT2_SATURDAY));
    }

    public boolean isHoliday() {
        return (getName() != null) && (getName().equals(PT1_HOLIDAY) || getName().equals(PT2_HOLIDAY));
    }


    public boolean isPeriod(int period) {
        return (getName() != null) && !isForced() && getName().contains("" + period);
    }

    private boolean isForced() {
        return (getName() != null) && (getName().equals(FORCED_1) || getName().equals(FORCED_2) || getName().equals(FORCED_3));
    }

    public static void main(String[] args) {
        System.out.println(DEFAULT.equals(null));
    }
}