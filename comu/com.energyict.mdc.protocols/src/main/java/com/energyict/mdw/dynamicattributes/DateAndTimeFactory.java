package com.energyict.mdw.dynamicattributes;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author Karel
 */
public class DateAndTimeFactory extends AbstractDateFactory {


    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getDateAndTimeEditor", model, attType, aspect);
    }

    public String getDbType() {
        return "number";
    }

    public Date valueFromDb(Object object, ValueDomain domain) {
        return object != null ? new Date(((Number) object).longValue() * 1000L) : null;
    }

    public Object valueToDb(Date object) {
        return object != null ? object.getTime() / 1000L : null;
    }

    public Date valueFromWS(Object object, ValueDomain domain) {
        return (Date) object;
    }

    public Object valueToWS(Date object) {
        return object;
    }

    public Class<Date> getValueType() {
        return Date.class;
    }

    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    protected String doGetHtmlString(Date object) {
        DateFormat df = Environment.DEFAULT.get().getFormatPreferences().getDateTimeFormat(false);
        return df.format(object);
    }

    public boolean isTime() {
        return true;
    }

    public boolean isUtcTimeStamp() {
        return true;
    }

    @Override
    public Date fromStringValue(String stringValue, ValueDomain domain) {
        return stringValue == null || stringValue.length() == 0 ? null : valueFromDb(Long.parseLong(stringValue), domain);
    }

    @Override
    public String toStringValue(Date object) {
        return object != null ? valueToDb(object).toString() : "";
    }

}