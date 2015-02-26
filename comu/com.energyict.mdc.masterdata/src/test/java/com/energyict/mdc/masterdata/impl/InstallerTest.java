package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.masterdata.RegisterType;
import org.junit.After;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

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

    @Test
    public void createRegisterTypesTest() {
        this.inMemoryPersistence = new InMemoryPersistence();

        // Business method is linked to "createMasterData" parameter in the call below
        this.inMemoryPersistence.initializeDatabase("com.energyict.mdc.masterdata.impl.InstallerTest", false, true);

        // Asserts
        List<RegisterType> registerTypes = this.inMemoryPersistence.getMasterDataService().findAllRegisterTypes().find();
        assertThat(registerTypes).hasSize(MasterDataGenerator.FixedRegisterTypes.values().length);
        registerTypes.stream().forEach(registerType -> assertThat(
                Stream.of(MasterDataGenerator.FixedRegisterTypes.values())
                        .filter(fixedRegisterType -> fixedRegisterType.getReadingType().equals(registerType.getReadingType().getMRID()))
                        .findFirst()
                        .isPresent())
                .isTrue());
    }

}