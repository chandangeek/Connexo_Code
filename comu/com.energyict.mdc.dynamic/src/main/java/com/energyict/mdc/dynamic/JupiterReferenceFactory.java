package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.util.proxy.LazyLoadProxy;
import com.elster.jupiter.util.proxy.LazyLoader;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.elster.jupiter.util.HasId;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link ValueFactory} interface
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
    public T valueFromDatabase (Object object) throws SQLException {
        if (object instanceof Number) {
            return LazyLoadProxy.newInstance(new HasIdLazyLoader<>((Number) object, this.domainClass, this.finder));
        }
        else {
            return null;
        }
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
            Long id = new Long(stringValue);
            return LazyLoadProxy.newInstance(new HasIdLazyLoader<>(id, this.domainClass, this.finder));
        }
        catch (NumberFormatException e) {
            throw new ApplicationException("Error parsing identifier of an " + this.domainClass.getName() + " from string value: " + stringValue);
        }
    }

    @Override
    public String toStringValue (T object) {
        return Long.toString(object.getId());
    }

    private class HasIdLazyLoader<T extends HasId> implements LazyLoader<T> {

        private final Number id;
        private Class<T> domainClass;
        private CanFindByLongPrimaryKey<T> factory;

        private HasIdLazyLoader(Number id, Class<T> domainClass, CanFindByLongPrimaryKey<T> factory) {
            super();
            this.id = id;
            this.domainClass = domainClass;
            this.factory = factory;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T load () {
            return this.factory.findByPrimaryKey(this.id.longValue()).orElse(null);
        }

        @Override
        public Class<T> getImplementedInterface () {
            return domainClass;
        }

        public ClassLoader getClassLoader() {
            return this.factory.getClass().getClassLoader();
        }
    }

}