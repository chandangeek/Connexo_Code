package com.elster.protocolimpl.dlms.tariff.objects;

import com.energyict.mdw.core.CodeDayTypeDef;

import java.io.Serializable;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 14:50
 */
@SuppressWarnings("unused")
public class CodeDayTypeDefObject implements Serializable, Comparable<CodeDayTypeDefObject> {

    private int dayTypeId;
    private String dayTypeName;
    private int from;
    private int codeValue;

    public static CodeDayTypeDefObject fromCodeDayTypeDef(CodeDayTypeDef def) {
        CodeDayTypeDefObject dtd = new CodeDayTypeDefObject();
        dtd.setCodeValue(def.getCodeValue());
        dtd.setFrom(def.getTstampFrom());
        dtd.setDayTypeId(def.getDayType().getId());
        dtd.setDayTypeName(def.getDayType().getName());
        return dtd;
    }

    public int getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(int codeValue) {
        this.codeValue = codeValue;
    }

    public int getDayTypeId() {
        return dayTypeId;
    }

    public void setDayTypeId(int dayTypeId) {
        this.dayTypeId = dayTypeId;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public String getDayTypeName() {
        return dayTypeName;
    }

    public void setDayTypeName(String dayTypeName) {
        this.dayTypeName = dayTypeName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CodeDayTypeDefObject");
        sb.append("{codeValue=").append(codeValue);
        sb.append(", dayTypeId=").append(dayTypeId);
        sb.append(", dayTypeName='").append(dayTypeName).append('\'');
        sb.append(", from=").append(from);
        sb.append('}');
        return sb.toString();
    }

    public int compareTo(CodeDayTypeDefObject other) {
        return this.from - (other == null ? 0 : other.from);
    }
}
