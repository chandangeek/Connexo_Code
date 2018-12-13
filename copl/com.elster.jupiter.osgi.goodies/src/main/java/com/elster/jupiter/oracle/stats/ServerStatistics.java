/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

public class ServerStatistics extends Statistics {

	@Override
	String getSql() {
		return 
			"select name, sum(value) from gv$sesstat a join v$statname b on a.statistic# = b.statistic# where value > 0 and (inst_id, sid) in (" +
			SERVERSESSIONS + 
			") group by name";
	}

	@Override
	String getTopic() {
		return "com/elster/jupiter/oracle/stats/statistic/SESSION";
	}
	
}
