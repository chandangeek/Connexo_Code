package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link com.elster.jupiter.properties.ValueFactory} interface
 * for references to objects that have already been ported to
 * the new jupiter ORM framework.
 *
 * @param <T>
 */
public class JupiterReferenceFactory<T extends HasId> extends AbstractValueFactory<T> {

    private CanFindByLongPrimaryKey<T> finder;
    private Class<T> domainClass;

    public JupiterReferenceFactory() {
        super();
    }

    public JupiterReferenceFactory(CanFindByLongPrimaryKey<T> finder) {
        this();
        this.finder = finder;
        this.domainClass = finder.valueDomain();
    }

    public CanFindByLongPrimaryKey<T> getFinder() {
        return finder;
    }

    @Override
    public int getObjectFactoryId() {
        return this.finder.factoryId().id();
    }

    @Override
    public boolean isReference () {
        return true;
    }

    @Override
    public Class<T> getValueType () {
        return this.domainClass;
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.NUMERIC;
    }

    @Override
    public String getDatabaseTypeName () {
        return "number(10)";
    }

    @Override
    public boolean requiresIndex () {
        return true;
    }

    @Override
    public boolean isPersistent (T businessObject) {
        return businessObject.getId() != 0;
    }

    @Override
    public T valueFromDatabase (Object object) {
        if (object instanceof Number) {
            return this.valueFromDatabase((Number) object);
        }
        else {
            throw new IllegalArgumentException(JupiterReferenceFactory.class.getSimpleName() + " expects numeric values from database but got " + object.getClass().getName());
        }
    }

    private T valueFromDatabase (Number id) {
        return this.finder.findByPrimaryKey(id.longValue()).get();
    }

    @Override
    public Object valueToDatabase (T object) {
        return object.getId();
    }

    @Override
    public void bind(PreparedStatement statement, int offset, T value) throws SQLException {
        statement.setLong(offset, value.getId());
    }

    @Override
    public void bind(SqlBuilder builder, T value) {
        builder.addLong(value.getId());
    }

    @Override
    public T fromStringValue (String stringValue) {
        try {
            return this.valueFromDatabase(new Long(stringValue));
        }
        catch (NumberFormatException e) {
            throw new ApplicationException("Error parsing identifier of an " + this.domainClass.getName() + " from string value: " + stringValue);
        }
    }

    @Override
    public String toStringValue (T object) {
        return Long.toString(object.getId());
    }

}