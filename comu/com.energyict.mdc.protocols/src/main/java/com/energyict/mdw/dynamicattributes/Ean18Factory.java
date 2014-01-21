package com.energyict.mdw.dynamicattributes;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ean.Ean18;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

import java.sql.SQLException;
import java.text.ParseException;

/**
 * @author Karel
 */
public class Ean18Factory extends AbstractValueFactory<Ean18> {

    public String getDbType() {
        return "char(18)";
    }

    public Ean18 valueFromDb(Object object, ValueDomain domain) throws SQLException {
        try {
            if (object == null || ((String) object).trim().length() == 0) {
                return null;
            }
            else {
                return new Ean18((String) object);
            }
        }
        catch (ParseException ex) {
            throw new SQLException(ex.toString());
        }
    }

    public Object valueToDb(Ean18 object) {
        if (object == null) {
            return null;
        }
        else {
            return object.toString();
        }
    }

    public Ean18 valueFromWS(Object object, ValueDomain domain) throws BusinessException {
        try {
            if (object != null) {
                return new Ean18((String) object);
            }
            else {
                return null;
            }
        }
        catch (ParseException e) {
            throw new BusinessException(e);
        }
    }

    public Object valueToWS(Ean18 object) {
        if (object != null) {
            return object.toString();
        }
        else {
            return null;
        }
    }

    public Class<Ean18> getValueType() {
        return Ean18.class;
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

    protected String doGetHtmlString(Ean18 object) {
        return object.toString();
    }

    public boolean isStringLike() {
        return true;
    }

    @Override
    public Ean18 fromStringValue(String stringValue, ValueDomain domain) {
        try {
            if (stringValue == null || stringValue.length() != 18) {
                return null;
            }
            else {
                return new Ean18(stringValue);
            }
        }
        catch (ParseException x) {
            return null;
        }
    }

    @Override
    public String toStringValue(Ean18 object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }

}