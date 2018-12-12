/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

public class ServerWaitEvents extends WaitEvents {

	@Override
	String getSql() {
		return 
			"select event , sum(total_waits), sum(time_waited_micro) from gv$session_event where (inst_id,sid) in (" +
			SERVERSESSIONS +
			") group by event";
	}
	
	@Override
	String getTopic() {
		return "com/elster/jupiter/oracle/stats/wait/SYSTEM";
	}
}
