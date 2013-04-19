package com.elster.jupiter.bootstrap;

import javax.sql.DataSource;

public interface BootstrapService {
	DataSource getDataSource();
}
