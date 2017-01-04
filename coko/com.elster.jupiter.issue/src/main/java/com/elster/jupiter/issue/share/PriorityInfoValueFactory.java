package com.elster.jupiter.issue.share;

import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class PriorityInfoValueFactory implements ValueFactory<HasName> {


    @Override
    public HasName fromStringValue(String stringValue) {
        List<String> contents = Arrays.asList(stringValue.split(":"));
        if (contents.size() == 2) {
            return new PriorityInfo(Priority.get(Integer.parseInt(contents.get(0)), Integer.parseInt(contents.get(1))));
        } else {
            return null;
        }
    }

    @Override
    public String toStringValue(HasName object) {
        return String.valueOf(((PriorityInfo)object).getPriority().toString());
    }

    @Override
    public Class<HasName> getValueType() {
        return HasName.class;
    }

    @Override
    public HasName valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(HasName object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, HasName value) throws SQLException {

    }

    @Override
    public void bind(SqlBuilder builder, HasName value) {

    }
}
