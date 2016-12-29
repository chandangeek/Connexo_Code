package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.energyict.mdc.upl.messages.legacy.Extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 14:34
 */
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

    private int id;
    private String name;
    private List<CodeDayTypeDefObject> dayTypeDefs = new ArrayList<>();

    public static CodeDayTypeObject fromCodeDayType(Extractor.CalendarDayType dayType) {
        CodeDayTypeObject dt = new CodeDayTypeObject();
        dt.setId(dayType.id());
        dt.setName(dayType.name());
        dt.setDayTypeDefs(dayType.slices().stream().map(CodeDayTypeDefObject::fromCodeDayTypeDef).collect(Collectors.toList()));
        return dt;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId(String id) {
        this.setId(Integer.parseInt(id));
    }

    @Override
    public String toString() {
        return "CodeDayTypeObject" +
                "{dayTypeDefs=" + dayTypeDefs +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public boolean isDefault() {
        return DEFAULT.equals(getName() != null ? getName() : "");
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

}