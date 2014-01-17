package com.energyict.dynamicattributes;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Karel
 */
public class DateFactory extends AbstractDateFactory {

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getDateEditor", model, attType, aspect);
    }

    public String getDbType() {
        return "date";
    }

    public Date valueFromDb(Object object, ValueDomain domain) {
        return object != null ? new java.util.Date(((java.util.Date) object).getTime()) : null;
    }

    public Object valueToDb(Date object) {
        if (object == null) {
            return null;
        }
        return new java.sql.Date(object.getTime());
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
        return java.sql.Types.DATE;
    }

    protected String doGetHtmlString(Date object) {
        DateFormat df = Environment.DEFAULT.get().getFormatPreferences().getDateFormat();
        return df.format(object);
    }

    public boolean isTime() {
        return true;
    }

    public boolean isUtcTimeStamp() {
        return false;
    }

    @Override
    public Date fromStringValue(String stringValue, ValueDomain domain) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return stringValue==null || stringValue.length()==0 ? null : format.parse(stringValue);//new Date(Long.parseLong(stringValue));
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    @Override
    public String toStringValue(Date object) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return object == null ? "" : format.format(object);//Long.toString(object.getTime());
    }

}