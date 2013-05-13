package com.elster.jupiter.security.thread;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

public interface ThreadPrincipalService {
	Principal getPrincipal();
	String getModule();
	String getAction();
	void set(Principal principal, String module, String action);
	void set(Principal principal);
	void set(String module, String action);
	void clear();
	void runAs(Principal principal , Runnable runnable);
	void setEndToEndMetrics(Connection connection) throws SQLException;
}
