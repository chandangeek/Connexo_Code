package com.energyict.dynamicattributes;

import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.StaticMethodSeed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueAdapter;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-02 (14:22)
 */
public abstract class AbstractValueFactory<S> implements ValueFactory<S> {

    /**
     * Creates a new instance of AbstractValueFactory
     */
    public AbstractValueFactory() {
    }

    protected String getDefaultEditorFactoryClassName() {
        return "com.energyict.swing.dynamicattributes.DefaultEditorFactory";
    }

    public Seed getEditorSeed(String factoryClassName, String methodName, DynamicAttributeOwner model, AttributeType attType) {
        return new StaticMethodSeed(
                factoryClassName,
                methodName,
                this,
                ValueFactory.class,
                model,
                DynamicAttributeOwner.class,
                attType,
                AttributeType.class);
    }

    public Seed getEditorSeed(String factoryClassName, String methodName, DynamicAttributeOwner model,
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

    public String getHtmlString(S object) {
        if (object == null) {
            return "";
        } else {
            return doGetHtmlString(object);
        }
    }

    public boolean supportsReferentialIntegrity(RelationAttributeType attribType) {
        return false;
    }

    protected abstract String doGetHtmlString(S object);

    public boolean isNumeric() {
        return false;
    }

    public boolean isStringLike() {
        return false;
    }

    public boolean isTime() {
        return false;
    }

    public boolean isReference() {
        return false;
    }

    public String getStructType() {
        return null;
    }

    public boolean requiresIndex() {
        return false;
    }

    public String getIndexType() {
        return null;
    }

    public void setObject(PreparedStatement preparedStatement, int offset, S value) throws SQLException {
        preparedStatement.setObject(offset, value);
    }

    public ValueAdapter getAdapter(DynamicAttributeOwner model, String aspect) {
        throw new UnsupportedOperationException("ValueFactory#getAdapter(DynamicAttributeOwner) is no longer supported");
    }

    public Seed getCellRendererSeed() {
        throw new UnsupportedOperationException("ThreeStateFactory#getCellRendererSeed(DynamicAttributeOwner) is no longer supported");
    }

    protected boolean isNull(String strToTest) {
        if (strToTest == null) {
            return true;
        }
        for (int index=0; index<strToTest.length(); index++) {
            if (!Character.isWhitespace(strToTest.charAt(index))) {
                return false;
            }
        }
        return true;
    }

}