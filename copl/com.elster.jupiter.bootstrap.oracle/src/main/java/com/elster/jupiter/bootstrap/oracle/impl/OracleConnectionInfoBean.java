/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.oracle.impl;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

/**
 * Created by bbl on 21/10/2015.
 */
@Component(name = "com.elster.jupiter.bootstrap.oracle.jmx",
        property = {"jmx.objectname=com.elster.jupiter:type=ConnectionPool"})
public class OracleConnectionInfoBean implements ConnectionInfoMBean {

    private String jdbcUrl;
    private String userName;

    @Activate
    public void activate(BundleContext context) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        context.registerService(MBeanServer.class.getName(), mbs, null);

        jdbcUrl = context.getProperty(BootstrapServiceImpl.JDBC_DRIVER_URL);
        userName = context.getProperty(BootstrapServiceImpl.JDBC_USER);
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String getSchemaName() {
        return userName;
    }
}
