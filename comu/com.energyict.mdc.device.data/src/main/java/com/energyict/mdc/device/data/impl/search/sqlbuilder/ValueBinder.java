package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ValueBinder {
    void bindSingleValue(PreparedStatement statement, Object value, int bindPosition) throws SQLException;
}
