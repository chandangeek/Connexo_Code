package com.energyict.mdc.masterdata.impl;

import org.junit.After;
import org.junit.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the installer component
 *
 * Copyrights EnergyICT
 * Date: 24/02/14
 * Time: 11:54
 */
public class InstallerTest {

    private InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void installWithoutMasterData() {
        this.inMemoryPersistence = new InMemoryPersistence();

        // Business method is linked to "createMasterData" parameter in the call below
        this.inMemoryPersistence.initializeDatabase("com.energyict.mdc.masterdata.impl.InstallerTest", false, false);

        // Asserts
        assertThat(inMemoryPersistence.getMasterDataService().findAllMeasurementTypes().find()).isEmpty();
    }

    @Test
    public void installWithMasterData () {
        this.inMemoryPersistence = new InMemoryPersistence();

        // Business method is linked to "createMasterData" parameter in the call below
        this.inMemoryPersistence.initializeDatabase("com.energyict.mdc.masterdata.impl.InstallerTest", false, true);

        // Asserts
        assertThat(inMemoryPersistence.getMasterDataService().findAllMeasurementTypes().find()).isNotEmpty();
    }

}