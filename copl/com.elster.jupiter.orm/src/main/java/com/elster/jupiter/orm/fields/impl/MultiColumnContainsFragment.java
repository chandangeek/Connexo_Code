package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.util.conditions.Contains;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class MultiColumnContainsFragment extends MultiColumnFragment {

    private final Contains contains;

    public MultiColumnContainsFragment(MultiColumnMapping fieldMapping, Contains contains, String alias) {
        super(fieldMapping, alias);
        this.contains = contains;
    }

    @Override
    public int bind(PreparedStatement statement, int position) throws SQLException {
        for (Object value : contains.getCollection()) {
            position = bind(statement, position, value);
        }
        return position;
    }

    @Override
    public String getText() {
        return contains.getCollection().isEmpty() ? getSqlText(contains.getCollection()) : decorate(contains.getCollection().stream()).partitionPer(1000)
                .map(this::getSqlText)
                .collect(Collectors.joining(" OR ", "(", ")"));
    }

    private String getSqlText(Collection collection) {
        int keyParts = getFieldMapping().getColumns().size();
        StringBuilder builder = new StringBuilder("(");
        String separator = "";
        for (int i = 0; i < keyParts; i++) {
            builder.append(separator);
            builder.append(getFieldMapping().getColumns().get(i).getName(getAlias()));
            separator = ", ";
        }
        builder.append(") ");
        builder.append(contains.getOperator().getSymbol());
        builder.append(" (");
        String outerSeparator = "";
        for (int i = 0; i < collection.size(); i++) {
            builder.append(outerSeparator);
            String innerSeparator = "";
            builder.append("(");
            for (int j = 0; j < keyParts; j++) {
                builder.append(innerSeparator);
                builder.append("?");
                innerSeparator = ",";
            }
            builder.append(")");
            outerSeparator = ",";
        }
        builder.append(")");
        return builder.toString();
    }

}


