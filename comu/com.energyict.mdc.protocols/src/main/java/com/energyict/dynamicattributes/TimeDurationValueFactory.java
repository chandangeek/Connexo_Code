package com.energyict.dynamicattributes;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

import java.sql.SQLException;
import java.sql.Types;

public class TimeDurationValueFactory extends AbstractValueFactory<TimeDuration> {

    public static final String VALUE_UNIT_SEPARATOR = ":";

    private final boolean onlySmallUnits;

    public TimeDurationValueFactory() {
        this.onlySmallUnits = false;
    }

    /**
     * Constructor with the option to show only the small TimeDuration Units in the editor.
     * @param onlySmallUnits true to show only the small TimeDuration units, false will show them all
     */
    public TimeDurationValueFactory(boolean onlySmallUnits) {
        this.onlySmallUnits = onlySmallUnits;
    }

    public String getDbType() {
        return "varchar2(32)";
    }

    @Override
    public TimeDuration valueFromDb (Object object, ValueDomain domain) throws SQLException {
        return getValueFromObject(object);
    }

    public Object valueToDb(final TimeDuration duration) {
        return getObjectFromValue(duration);
    }

    @Override
    public TimeDuration valueFromWS (Object object, ValueDomain domain) throws BusinessException {
        return getValueFromObject(object);
    }

    public Object valueToWS(final TimeDuration duration) {
        return getObjectFromValue(duration);
    }

    public Class<TimeDuration> getValueType() {
        return TimeDuration.class;
    }

    public int getJdbcType() {
        return Types.VARCHAR;
    }

    public Seed getEditorSeed(final DynamicAttributeOwner model, final AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    @Override
    public Seed getEditorSeed (DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), getTimeDurationEditorString(), model, attType, attType.getName());
    }

    private String getTimeDurationEditorString() {
        if(onlySmallUnits){
            return "getTimeDurationWithSmallValuesEditor";
        } else {
            return "getTimeDurationEditor";
        }
    }

    protected String doGetHtmlString(TimeDuration object) {
        return object.toString();
    }

    private TimeDuration getValueFromObject(final Object object) {
        if (object == null) {
            return null;
        }
        String value = (String) object;
        String[] valueAndUnit = value.split(VALUE_UNIT_SEPARATOR);
        int timeUnits = Integer.parseInt(valueAndUnit[0]);
        int unit = Integer.parseInt(valueAndUnit[1]);
        return new TimeDuration(timeUnits, unit);
    }

    private String getObjectFromValue(final TimeDuration duration) {
        if (duration == null) {
            return null;
        }
        return duration.getCount() + VALUE_UNIT_SEPARATOR + duration.getTimeUnitCode();
    }

    @Override
    public TimeDuration fromStringValue(String stringValue, ValueDomain domain) {
        return getValueFromObject(stringValue);
    }

    @Override
    public String toStringValue(TimeDuration object) {
        return getObjectFromValue(object);
    }

    public boolean isOnlySmallUnits() {
        return onlySmallUnits;
    }

}