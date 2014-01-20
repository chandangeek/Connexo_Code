package com.energyict.mdw.dynamicattributes;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory;

/**
 * Provides {@link ValueFactory} services for {@link ObisCode}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2010-07-13 (14:20)
 */
public class ObisCodeValueFactory extends AbstractValueFactory<ObisCode> {

    /**
     * Constructs a new ObisCodeValueFactory.
     */
    public ObisCodeValueFactory() {
        super();
    }

    public String getDbType() {
        return "varchar2(23)";
    }

    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    public Class<ObisCode> getValueType() {
        return ObisCode.class;
    }

    public ObisCode valueFromDb(Object object, ValueDomain domain) {
        if (object == null) {
            return null;
        } else {
            String obisCodeValue = (String) object;
            return ObisCode.fromString(obisCodeValue);
        }
    }

    public Object valueToDb(ObisCode object) {
        if (object == null) {
            return null;
        } else {
            return object.toString();
        }
    }

    public ObisCode valueFromWS(Object object, ValueDomain domain) throws BusinessException {
        return this.valueFromDb(object, domain);
    }

    public Object valueToWS(ObisCode object) {
        return this.valueToDb(object);
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getSmartObisCodeEditor", model, attType, aspect);
    }

    protected String doGetHtmlString(ObisCode object) {
        return object.toString();
    }

    public boolean isStringLike() {
        return true;
    }

    @Override
    public ObisCode fromStringValue(String stringValue, ValueDomain domain) {
        return stringValue==null || stringValue.length()==0 ? null : ObisCode.fromString(stringValue);
    }

    @Override
    public String toStringValue(ObisCode object) {
        return object==null ? "" : object.toString();
    }

}