package com.elster.jupiter.bootstrap;

import com.elster.jupiter.util.exception.BaseException;

import java.sql.SQLException;

public class DataSourceSetupException extends BaseException {

    public DataSourceSetupException(SQLException cause) {
        super(ExceptionTypes.DATASOURCE_SETUP_FAILED, cause);
    }
}
