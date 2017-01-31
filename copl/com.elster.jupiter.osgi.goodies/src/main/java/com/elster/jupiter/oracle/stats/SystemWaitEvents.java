/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

public class SystemWaitEvents extends WaitEvents {

	@Override
	String getSql() {
		return "select event , sum(total_waits), sum(time_waited_micro) from gv$system_event group by event";
	}
	
	@Override
	String getTopic() {
		return "com/elster/jupiter/oracle/stats/wait/SYSTEM";
	}
}
