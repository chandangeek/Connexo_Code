package com.energyict.dynamicattributes;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ean.Ean13;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

import java.sql.SQLException;
import java.text.ParseException;

/**
 * @author Karel
 */
public class Ean13Factory extends AbstractValueFactory<Ean13> {

    public String getDbType() {
        return "char(13)";
    }

    public Ean13 valueFromDb(Object object, ValueDomain domain) throws SQLException {
        try {
            if (object == null || ((String) object).trim().length() == 0) {
                return null;
            }
            else {
                return new Ean13((String) object);
            }
        }
        catch (ParseException ex) {
            throw new SQLException(ex.toString());
        }
    }

    public Object valueToDb(Ean13 object) {
        if (object == null) {
            return null;
        }
        else {
            return object.toString();
        }
    }

    public Ean13 valueFromWS(Object object, ValueDomain domain) throws BusinessException {
        try {
            if (object != null) {
                return new Ean13((String) object);
            }
            else {
                return null;
            }
        }
        catch (ParseException e) {
            throw new BusinessException(e);
        }
    }

    public Object valueToWS(Ean13 object) {
        if (object != null) {
            return object.toString();
        }
        else {
            return null;
        }
    }

    public Class<Ean13> getValueType() {
        return Ean13.class;
    }

    public int getJdbcType() {
        return java.sql.Types.CHAR;
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getEditor", model, attType, aspect);
    }

    protected String doGetHtmlString(Ean13 object) {
        return object.toString();
    }

    public boolean isStringLike() {
        return true;
    }

    @Override
    public Ean13 fromStringValue(String stringValue, ValueDomain domain) {
        try {
            if (stringValue == null || stringValue.length() != 13) {
                return null;
            }
            else {
                return new Ean13(stringValue);
            }
        }
        catch (ParseException x) {
            return null;
        }
    }

    @Override
    public String toStringValue(Ean13 object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }

}