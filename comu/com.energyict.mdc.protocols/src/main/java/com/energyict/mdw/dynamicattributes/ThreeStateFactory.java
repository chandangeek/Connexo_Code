package com.energyict.mdw.dynamicattributes;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

public class ThreeStateFactory extends AbstractValueFactory<Boolean> {


    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getThreeStateEditor", model, attType, aspect);
    }

    public String getDbType() {
        return "number(1)";
    }

    public Boolean valueFromDb(Object object, ValueDomain domain) {
        if (object == null) {
            return null;
        }
        return ((Number) object).intValue() != 0;
    }

    public Object valueToDb(Boolean object) {
        if (object == null) {
            return null;
        }
        return object ? 1 : 0;
    }

    public Boolean valueFromWS(Object object, ValueDomain domain) {
        return (Boolean) object;
    }

    public Object valueToWS(Boolean object) {
        return object;
    }

    public Class<Boolean> getValueType() {
        return Boolean.class;
    }

    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    protected String doGetHtmlString(Boolean object) {
        StringBuffer result = new StringBuffer(100);

        result.append(" <input type\"radio\" name=\"tri\" ");
        if (object != null && object) {
            result.append("checked ");
        }
        result.append("disabled=\"true\" > ");
        result.append(UserEnvironment.getDefault().getTranslation("yes"));

        result.append(" <input type\"radio\" name=\"tri\" ");
        if (object != null && !object) {
            result.append("checked ");
        }
        result.append("disabled=\"true\" > ");
        result.append(UserEnvironment.getDefault().getTranslation("no"));

        result.append(" <input type\"radio\" name=\"tri\" ");
        if (object == null) {
            result.append("checked ");
        }
        result.append("disabled=\"true\" > ");
        result.append(UserEnvironment.getDefault().getTranslation("unknown"));

        return result.toString();
    }

    @Override
    public Boolean fromStringValue(String stringValue, ValueDomain domain) {
        return !this.isNull(stringValue) && "1".equals(stringValue.trim()) ? Boolean.TRUE: Boolean.FALSE;
    }

    @Override
    public String toStringValue(Boolean object) {
        return Boolean.TRUE.equals(object) ? "1" : "0";
    }
}