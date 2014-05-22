package com.elster.jupiter.oracle.stats;

public class SystemStatistics extends Statistics {

	@Override
	String getSql() {
		return "select name, sum(value) from gv$sysstat where value > 0 group by name";
	}

	@Override
	String getTopic() {
		return "com/elster/jupiter/oracle/stats/statistic/SYSTEM";
	}
}
