package com.energyict.mdc.dynamic;

import com.elster.jupiter.util.proxy.LazyLoadProxy;
import com.elster.jupiter.util.proxy.LazyLoader;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReferenceFactory<T extends IdBusinessObject> extends AbstractValueFactory<T> {

    private IdBusinessObjectFactory<T> factory;
    private Class<T> domainClass;

    public ReferenceFactory () {
        super();
    }

    public ReferenceFactory (IdBusinessObjectFactory<T> factory) {
        this();
        this.factory = factory;
        this.domainClass = factory.getInstanceType();
    }

    @Override
    public int getObjectFactoryId() {
        return this.factory.getId();
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
            return LazyLoadProxy.newInstance(new IdBusinessObjectLazyLoader<>((Number) object, this.domainClass, this.factory));
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
        statement.setInt(offset, value.getId());
    }

    @Override
    public void bind(SqlBuilder builder, T value) {
        builder.bindInt(value.getId());
    }

    @Override
    public T fromStringValue (String stringValue) {
        try {
            Integer id = new Integer(stringValue);
            return LazyLoadProxy.newInstance(new IdBusinessObjectLazyLoader<>(id, this.domainClass, this.factory));
        }
        catch (NumberFormatException e) {
            throw new ApplicationException("Error parsing identifier of an " + this.domainClass.getName() + " from string valu: " + stringValue);
        }
    }

    @Override
    public String toStringValue (T object) {
        return Integer.toString(object.getId());
    }

    private class IdBusinessObjectLazyLoader<T extends IdBusinessObject> implements LazyLoader<T> {

        private final Number id;
        private Class<T> domainClass;
        private IdBusinessObjectFactory<T> factory;

        private IdBusinessObjectLazyLoader (Number id, Class<T> domainClass, IdBusinessObjectFactory<T> factory) {
            super();
            this.id = id;
            this.domainClass = domainClass;
            this.factory = factory;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T load () {
            return this.factory.get(this.id.intValue());
        }

        @Override
        public Class<T> getImplementedInterface () {
            return domainClass;
        }

        @Override
        public ClassLoader getClassLoader() {
            return this.factory.getClass().getClassLoader();
        }
    }

}