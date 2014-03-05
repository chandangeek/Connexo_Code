package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.transaction.TransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.sql.SQLException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the installer component
 *
 * Copyrights EnergyICT
 * Date: 24/02/14
 * Time: 11:54
 */
public class InstallerTest {

    static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabase("com.energyict.mdc.device.config.impl.InstallerTest", false, true);
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    @Transactional
    public void withoutInstallingTest() {
        assertThat(inMemoryPersistence.getDeviceConfigurationService().findAllProductSpecs()).isNotEmpty();
        assertThat(inMemoryPersistence.getDeviceConfigurationService().findAllRegisterMappings().find()).isNotEmpty();
        assertThat(inMemoryPersistence.getDeviceConfigurationService().findAllPhenomena()).isNotEmpty();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

}
