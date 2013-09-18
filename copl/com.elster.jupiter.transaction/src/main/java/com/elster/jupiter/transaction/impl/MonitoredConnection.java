package com.elster.jupiter.transaction.impl;

import java.sql.*;

class MonitoredConnection extends ConnectionWrapper {

	MonitoredConnection(Connection connection) {
		super(connection);
	}
	
	@Override
	PreparedStatement wrap(PreparedStatement statement,String text) {
		return new MonitoredStatement(statement,text);
	}


}
