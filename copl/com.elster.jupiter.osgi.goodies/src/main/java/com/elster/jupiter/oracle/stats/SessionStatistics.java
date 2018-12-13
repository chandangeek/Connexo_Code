/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

public class SessionStatistics extends Statistics {

	@Override
	String getSql() {
		return "select name, value from v$mystat a join v$statname b on a.statistic# = b.statistic# where value > 0 ";
	}

	@Override
	String getTopic() {
		return "com/elster/jupiter/oracle/stats/statistic/SESSION";
	}
	
}
