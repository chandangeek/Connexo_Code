package com.elster.jupiter.bootstrap.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import org.h2.jdbcx.JdbcDataSource;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.text.MessageFormat;

public class H2BootStrapService implements BootstrapService {

    private static final String JDBC_URL_PATTERN = "jdbc:h2:mem:{0};MVCC=TRUE;lock_timeout={1}";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final String DATABASE_NAME_BASE = "DB";

    @Inject
    public H2BootStrapService() {
    }

    @Override
    public DataSource createDataSource() {
        String jdbcUrl = MessageFormat.format(JDBC_URL_PATTERN, DATABASE_NAME_BASE, 5000L);
        JdbcDataSource source = new JdbcDataSource();
        source.setURL(jdbcUrl);
        source.setUser(USER);
        source.setPassword(PASSWORD);
        return new DecoratedDataSource(source);
    }


}
