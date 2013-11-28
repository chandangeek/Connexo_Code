package com.elster.jupiter.bootstrap.impl;

import com.elster.jupiter.bootstrap.BootstrapService;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        try {
            Class<?> clazz = Class.forName("org.h2.jdbcx.JdbcDataSource");
            DataSource source = (DataSource) clazz.newInstance();
            invokeSetter("setUrl", source, jdbcUrl);
            invokeSetter("setUser", source, USER);
            invokeSetter("setPassword", source, PASSWORD);
            return new DecoratedDataSource(source);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeSetter(String setterName, DataSource source, String jdbcUrl) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName("org.h2.jdbcx.JdbcDataSource");
        Method setUrl = clazz.getMethod(setterName, String.class);
        setUrl.invoke(source, jdbcUrl);
    }


}
