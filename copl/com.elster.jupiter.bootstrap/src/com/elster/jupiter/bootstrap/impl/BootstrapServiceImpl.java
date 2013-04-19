package com.elster.jupiter.bootstrap.impl;

import javax.sql.DataSource;

import com.elster.jupiter.bootstrap.BootstrapService;

class BootstrapServiceImpl implements BootstrapService {

	private final DataSource dataSource;
	
	public BootstrapServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

}
