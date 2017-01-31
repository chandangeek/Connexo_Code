/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

public class SessionWaitEvents extends WaitEvents {
	
	@Override
	String getSql() {
		return "select event , total_waits, time_waited_micro from v$session_event where sid = sys_context('userenv','sid')";
	}
	
	@Override
	String getTopic() {
		return "com/elster/jupiter/oracle/stats/wait/SESSION";
	}
}
