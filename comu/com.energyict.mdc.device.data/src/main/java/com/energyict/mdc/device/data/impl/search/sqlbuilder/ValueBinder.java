/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ValueBinder {
    void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException;
}
