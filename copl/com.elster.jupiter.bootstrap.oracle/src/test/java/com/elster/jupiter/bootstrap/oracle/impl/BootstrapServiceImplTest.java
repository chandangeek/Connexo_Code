/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.oracle.impl;

import com.elster.jupiter.bootstrap.PropertyNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BootstrapServiceImplTest {

    @Mock
    private BundleContext bundleContext;
    private BootstrapServiceImpl bootstrapService;

    @Before
    public void setUp() throws Exception {
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcurl")).thenReturn("http://url.com");
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcuser")).thenReturn("user");
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcpassword")).thenReturn("password");
        when(bundleContext.getProperty("com.elster.jupiter.datasource.pool.maxlimit")).thenReturn("47");
        when(bundleContext.getProperty("com.elster.jupiter.datasource.pool.maxstatements")).thenReturn("53");
        bootstrapService = new BootstrapServiceImpl();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testActivateNoExceptionsWhenAllPropertiesDefined() throws Exception {
        bootstrapService.activate(bundleContext);
    }
    @Test(expected = PropertyNotFoundException.class)
    public void testActivateChecksRequiredJdbcUrlProperty() throws Exception {
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcurl")).thenReturn(null);
        bootstrapService.activate(bundleContext);
    }

    @Test(expected = PropertyNotFoundException.class)
    public void testActivateChecksRequiredJdbcUserProperty() throws Exception {
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcuser")).thenReturn(null);
        bootstrapService.activate(bundleContext);
    }

    @Test(expected = PropertyNotFoundException.class)
    public void testActivateChecksRequiredJdbcPasswordProperty() throws Exception {
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcpassword")).thenReturn(null);
        bootstrapService.activate(bundleContext);
    }

    @Test
    public void testActivateNoExceptionsIfMaxLimitNotSpecified() throws Exception {
        when(bundleContext.getProperty("com.elster.jupiter.datasource..pool.maxlimit")).thenReturn(null);
        bootstrapService.activate(bundleContext);
    }

    @Test
    public void testActivateNoExceptionsIfMaxStatementsNotSpecified() throws Exception {
        when(bundleContext.getProperty("com.elster.jupiter.datasource.pool.maxstatements")).thenReturn(null);
        bootstrapService.activate(bundleContext);
    }

    @Test
    public void testCreateDataSource() {
        bootstrapService.activate(bundleContext);
        assertThat(bootstrapService.createDataSource()).isNotNull();
    }


}
