package com.elster.jupiter.bootstrap;

import javax.sql.DataSource;

/**
 * This Service is responsible for creating a DataSource on demand.
 */
public interface BootstrapService {

    /**
     * @return a newly created DataSource instance.
     */
	DataSource createDataSource();
}
