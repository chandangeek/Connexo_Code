/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ColumnEqualsFragment extends ColumnFragment implements SqlFragment {

    private final Object value;

    public ColumnEqualsFragment(ColumnImpl column, Object value, String alias) {
        super(column, alias);
        this.value = value;
    }

    @Override
    public int bind(PreparedStatement statement, int position) throws SQLException {
        return bind(statement, position, value);
    }

    @Override
    public String getText() {
        return " " + getColumn().getName(getAlias()) + " = ? ";
    }
}
