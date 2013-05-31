package com.elster.jupiter.bootstrap;

import java.sql.SQLException;

import javax.sql.DataSource;

public interface BootstrapService {
	DataSource createDataSource() throws SQLException;
}
