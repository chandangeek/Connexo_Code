/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap;

import com.elster.jupiter.util.exception.BaseException;

import java.sql.SQLException;

public class DataSourceSetupException extends BaseException {

	private static final long serialVersionUID = 1L;

	public DataSourceSetupException(SQLException cause) {
        super(MessageSeeds.DATASOURCE_SETUP_FAILED, cause);
    }
}
