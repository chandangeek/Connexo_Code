package com.elster.jupiter.properties;

public class StringReferenceFactory<T extends HasIdAndName> extends AbstractValueFactory<T> {

    private CanFindByStringKey<T> finder;
    private Class<T> domainClass;

    public StringReferenceFactory(CanFindByStringKey<T> finder) {
        this.finder = finder;
        this.domainClass = finder.valueDomain();
    }

    @Override
    public T fromStringValue(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        return finder.find(stringValue).orElse(null);
    }

    @Override
    public String toStringValue(T object) {
        return object.getId().toString();
    }

    @Override
    public Class<T> getValueType() {
        return this.domainClass;
    }

    @Override
    public String getDatabaseTypeName() {
        return "varchar2(256)";
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public T valueFromDatabase(Object object) {
        return fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(T object) {
        return toStringValue(object);
    }
}
