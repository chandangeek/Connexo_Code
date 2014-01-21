package com.energyict.mdw.dynamicattributes;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-02 (14:25)
 */
public class StringFactory extends AbstractValueFactory<String> {

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getStringEditor", model, attType, aspect);
    }

    public String getDbType() {
        return "varchar2(4000)";
    }

    public String valueFromDb(Object object, ValueDomain domain) {
        return (String) object;
    }

    public Object valueToDb(String object) {
        return object;
    }

    public String valueFromWS(Object object, ValueDomain domain) throws BusinessException {
        return (String) object;
    }

    public Object valueToWS(String object) {
        return object;
    }

    public Class<String> getValueType() {
        return String.class;
    }

    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    protected String doGetHtmlString(String object) {
        return object;
    }

    public boolean isStringLike() {
        return true;
    }

    public String fromStringValue(String stringValue, ValueDomain domain) {
        return stringValue == null ? "" : stringValue;
    }

    public String toStringValue(String object) {
        return object;
    }
}