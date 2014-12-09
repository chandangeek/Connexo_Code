package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-09 (11:46)
 */
public class StringFactory implements ValueFactory<String> {

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed("com.energyict.swing.dynamicattributes.DefaultEditorFactory", "getStringEditor", model, attType, aspect);
    }

    public Seed getEditorSeed(
            String factoryClassName, String methodName, DynamicAttributeOwner model,
            AttributeType attType, String aspect) {
        Object[] objects = new Object[4];
        Class[] objectTypes = new Class[4];
        objects[0] = this;
        objects[1] = model;
        objects[2] = attType;
        objects[3] = aspect;
        objectTypes[0] = ValueFactory.class;
        objectTypes[1] = DynamicAttributeOwner.class;
        objectTypes[2] = AttributeType.class;
        objectTypes[3] = String.class;
        return new StaticMethodSeed(factoryClassName, methodName, objects, objectTypes);
    }

    public ValueAdapter getAdapter(DynamicAttributeOwner model, String aspect) {
        throw new UnsupportedOperationException("ValueFactory#getAdapter(DynamicAttributeOwner) is no longer supported");
    }

    public Seed getCellRendererSeed() {
        throw new UnsupportedOperationException("ValueFactory#getCellRendererSeed(DynamicAttributeOwner) is no longer supported");
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isTime() {
        return false;
    }

    @Override
    public String getStructType() {
        return null;
    }

    public boolean supportsReferentialIntegrity(RelationAttributeType attribType) {
        return false;
    }

    public boolean requiresIndex() {
        return false;
    }

    public String getIndexType() {
        return null;
    }

    public void setObject(PreparedStatement preparedStatement, int offset, String value) throws SQLException {
        preparedStatement.setString(offset, value);
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

    public boolean isStringLike() {
        return true;
    }

    public String fromStringValue(String stringValue, ValueDomain domain) {
        return stringValue == null ? "" : stringValue;
    }

    public String toStringValue(String object) {
        return object;
    }

    public String getHtmlString(String object) {
        if (object == null) {
            return "";
        }
        else {
            return object;
        }
    }

}