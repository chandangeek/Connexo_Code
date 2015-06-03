package com.elster.jupiter.properties;

import java.sql.SQLException;
import java.util.Optional;

public class IdWithNameValueFactory<T extends IdWithNameValue> extends AbstractValueFactory<IdWithNameValue> {

    private FindById<T> finder;

    public IdWithNameValueFactory(FindById<T> finder) {
        this.finder = finder;
    }

    @Override
    public IdWithNameValue fromStringValue(String stringValue) {
        if (stringValue == null) {
            return IdWithNameValue.EMPTY;
        }
        Optional<T> found = finder.findById(stringValue);
        if(found.isPresent()) {
            return found.get();
        }
        return IdWithNameValue.EMPTY;
    }

    @Override
    public String toStringValue(IdWithNameValue object) {
        return object.getId().toString();
    }

    @Override
    public Class<IdWithNameValue> getValueType() {
        return IdWithNameValue.class;
    }

    @Override
    public String getDatabaseTypeName() {
        return "varchar2(80)";
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public IdWithNameValue valueFromDatabase(Object object) throws SQLException {
        return fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(IdWithNameValue object) {
        return toStringValue(object);
    }
}
