package com.elster.insight.usagepoint.config.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationCPSUsageTestIT {
    private static MetrologyInMemoryBootstrapModule inMemoryBootstrapModule = new MetrologyInMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }


}
