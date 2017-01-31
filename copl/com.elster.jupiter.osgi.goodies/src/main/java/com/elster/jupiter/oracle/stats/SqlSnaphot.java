/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlSnaphot extends SnapShot<SqlExecution> {

	@Override
	String getSql() {
		return
				"select sql_id , sum(executions) , sum(elapsed_time) , dbms_lob.substr(sql_fulltext,4000,1)from gv$sql " +
				"where parsing_schema_name = user " +
				"group by sql_id , dbms_lob.substr(sql_fulltext,4000,1) ";
	}

	@Override
	SqlExecution newRecord(ResultSet rs) throws SQLException {
		return new SqlExecution(rs,1);
	}

	@Override
	SqlExecution newRecord(String name) {
		return new SqlExecution(name);
	}

	@Override
	String getTopic() {
		return "com/elster/jupiter/oracle/stats/sql/EXECUTION";
	}

}
