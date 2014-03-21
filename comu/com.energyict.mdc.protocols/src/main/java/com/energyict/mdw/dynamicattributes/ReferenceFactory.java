package com.energyict.mdw.dynamicattributes;

import com.energyict.mdw.cpo.PersistentIdObjectProxy;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeValueSelectionMode;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.StaticMethodSeed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory;

public class ReferenceFactory extends AbstractValueFactory {

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getReferenceEditor", model, attType, aspect);
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, PropertySpec propertySpec, boolean required) {
        Object[] objects = new Object[7];
        Class[] objectTypes = new Class[7];
        objects[0] = this;
        objects[1] = model;
        objects[2] = required;
        objects[3] = propertySpec.getSelectionMode();
        objects[4] = propertySpec.getDomain();
        objects[5] = propertySpec.getName(); // display name (to be done in the future)
        objects[6] = propertySpec.getName(); // aspect name
        objectTypes[0] = ValueFactory.class;
        objectTypes[1] = DynamicAttributeOwner.class;
        objectTypes[2] = boolean.class;
        objectTypes[3] = AttributeValueSelectionMode.class;
        objectTypes[4] = ValueDomain.class;
        objectTypes[5] = String.class;
        objectTypes[6] = String.class;
        return new StaticMethodSeed(getDefaultEditorFactoryClassName(), "getReferenceEditor", objects, objectTypes);
    }

    public String getDbType() {
        return "number(10)";
    }

    public Object valueFromDb(Object object, ValueDomain domain) {
        if (object == null) {
            return null;
        }
        Number numObject = (Number) object;
        return new PersistentIdObjectProxy((IdBusinessObjectFactory) domain.getFactory(), numObject.intValue());
    }

    public Object valueToDb(Object object) {
        if (object instanceof IdBusinessObject) {
            return ((IdBusinessObject) object).getId();
        } else {
            return null;
        }
    }

    public Object valueFromWS(Object object, ValueDomain domain) throws BusinessException {
        throw new UnsupportedOperationException("LegacyReferenceFactory#valueFromWS(Object, ValueDomain) is no longer supported");
    }

    public Object valueToWS(Object object) {
        throw new UnsupportedOperationException("LegacyReferenceFactory#valueToWS(Object, ValueDomain) is no longer supported");
    }

    public Class getValueType() {
        return IdBusinessObject.class;
    }

    public int getJdbcType() {
        return java.sql.Types.NUMERIC;
    }

    protected String doGetHtmlString(Object object) {
        return object == null ? "" : object.toString();
    }

    public boolean supportsReferentialIntegrity(AttributeType attribType) {
        return true;
    }

    public boolean requiresIndex() {
        return true;
    }

    public boolean isReference() {
        return true;
    }

    public IdBusinessObject fromStringValue(String stringValue, ValueDomain domain) {
        return ((IdBusinessObjectFactory)domain.getFactory()).get(Integer.parseInt(stringValue));
    }

    public String toStringValue(Object object) {
        if (object instanceof IdBusinessObject) {
            return Integer.toString(((IdBusinessObject) object).getId());
        }
        throw new ApplicationException("Unsupported object type");
    }

}