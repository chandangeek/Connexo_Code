package com.energyict.dynamicattributes;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author Karel
 */
public class BigDecimalFactory extends AbstractValueFactory<BigDecimal> {

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getBigDecimalEditor", model, attType, aspect);
    }

    public String getDbType() {
        return "number";
    }

    public BigDecimal valueFromDb(Object object, ValueDomain domain) {
        return (BigDecimal) object;
    }

    public Object valueToDb(BigDecimal object) {
        return object;
    }

    public BigDecimal valueFromWS(Object object, ValueDomain domain) {
        return (BigDecimal) object;
    }

    public Object valueToWS(BigDecimal object) {
        return object;
    }

    public Class<BigDecimal> getValueType() {
        return BigDecimal.class;
    }

    public int getJdbcType() {
        return java.sql.Types.NUMERIC;
    }

    protected String doGetHtmlString(BigDecimal object) {
        DecimalFormat df = Environment.DEFAULT.get().getFormatPreferences().getNumberFormat();
        return df.format(object);
    }

    public boolean isNumeric() {
        return true;
    }

    public BigDecimal fromStringValue(String stringValue, ValueDomain domain) {
        return new BigDecimal(stringValue);
    }

    public String toStringValue(BigDecimal object) {
        return object==null ? "" : object.toString();
    }

}