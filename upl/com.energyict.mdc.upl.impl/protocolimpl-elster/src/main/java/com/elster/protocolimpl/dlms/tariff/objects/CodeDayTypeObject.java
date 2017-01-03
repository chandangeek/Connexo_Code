package com.elster.protocolimpl.dlms.tariff.objects;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;

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
    public static final String PT1_HOLIDAY = "PT1_Holiday";
    public static final String PT1_SATURDAY = "PT1_Saturday";
    public static final String PT1_WEEKDAY = "PT1_Weekday";
    public static final String PT2_HOLIDAY = "PT2_Holiday";
    public static final String PT2_SATURDAY = "PT2_Saturday";
    public static final String PT2_WEEKDAY = "PT2_Weekday";

    public static final String[] DAYTYPE_NAMES = new String[]{
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
    private String externalName;
    private List<CodeDayTypeDefObject> dayTypeDefs = new ArrayList<CodeDayTypeDefObject>();

    public static CodeDayTypeObject fromCodeDayType(TariffCalendarExtractor.CalendarDayType dayType) {
        CodeDayTypeObject dt = new CodeDayTypeObject();
        dt.setId(Integer.parseInt(dayType.id()));
        dt.setName(dayType.name());
        dt.setExternalName(null);
        dt.setDayTypeDefs(dayType.slices().stream().map(CodeDayTypeDefObject::fromCodeDayTypeDef).collect(Collectors.toList()));
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CodeDayTypeObject");
        sb.append("{dayTypeDefs=").append(dayTypeDefs);
        sb.append(", id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", externalName='").append(externalName).append('\'');
        sb.append('}');
        return sb.toString();
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
        return (getName() != null) && getName().contains("" + period);
    }
}
