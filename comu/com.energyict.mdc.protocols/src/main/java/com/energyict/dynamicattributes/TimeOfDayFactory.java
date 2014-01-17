package com.energyict.dynamicattributes;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

public class TimeOfDayFactory extends AbstractValueFactory<TimeOfDay> {

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getTimeOfDayEditor", model, attType, aspect);
    }

    public String getDbType() {
        return "number";
    }

    public TimeOfDay valueFromDb(Object object, ValueDomain domain) {
        return object != null ? new TimeOfDay(((Number) object).intValue()) : null;
    }

    public Object valueToDb(TimeOfDay object) {
        return object != null ? object.getSeconds() : null;
    }


    public TimeOfDay valueFromWS(Object object, ValueDomain domain) throws BusinessException {
        return (TimeOfDay) object;
    }

    public Object valueToWS(TimeOfDay object) {
        // TODO Auto-generated method stub
        return object;
    }

    public Class<TimeOfDay> getValueType() {
        return TimeOfDay.class;
    }

    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    protected String doGetHtmlString(TimeOfDay object) {
        return object.toString();
    }

    public boolean isTime() {
        return true;
    }

    @Override
    public TimeOfDay fromStringValue(String stringValue, ValueDomain domain) {
        return stringValue==null || stringValue.length()==0 ? new TimeOfDay(0) : new TimeOfDay(Integer.parseInt(stringValue));
    }

    @Override
    public String toStringValue(TimeOfDay object) {
        return object==null ? "" : Integer.toString(object.getSeconds());
    }
}