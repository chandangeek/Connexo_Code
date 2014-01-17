package com.energyict.dynamicattributes;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

/**
 * User: gde
 * Date: 17/04/13
 */
public class HexStringFactory extends AbstractValueFactory<HexString> {

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getHexStringEditor", model, attType, aspect);
    }

    public String getDbType() {
        return "varchar2(4000)";
    }

    public HexString valueFromDb(Object object, ValueDomain domain) {
        return object==null ? null : new HexString((String)object);
    }

    public Object valueToDb(HexString object) {
        return object==null ? null : object.toString();
    }

    public HexString valueFromWS(Object object, ValueDomain domain) throws BusinessException {
        return (HexString) object;
    }

    public Object valueToWS(HexString object) {
        return object;
    }

    public Class<HexString> getValueType() {
        return HexString.class;
    }

    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    protected String doGetHtmlString(HexString object) {
        return object==null ? "" : object.toString();
    }

    @Override
    public boolean isStringLike() {
        return true;
    }

    @Override
    public HexString fromStringValue(String stringValue, ValueDomain domain) {
        return stringValue == null ? null : new HexString(stringValue);
    }

    @Override
    public String toStringValue(HexString object) {
        return object==null ? "" : object.toString();
    }

}