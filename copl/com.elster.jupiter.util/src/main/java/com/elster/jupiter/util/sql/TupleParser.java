/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface TupleParser<T> {
	T construct(ResultSet resultSet) throws SQLException;
}
