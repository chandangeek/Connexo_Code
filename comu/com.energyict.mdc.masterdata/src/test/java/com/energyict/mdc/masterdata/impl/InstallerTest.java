package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.masterdata.RegisterType;
import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the installer component amongst others
 *
 * Copyrights EnergyICT
 * Date: 24/02/14
 * Time: 11:54
 */
public class InstallerTest {

    private static InMemoryPersistence inMemoryPersistence;

    @BeforeClass
    static public void setUp() throws Exception {
        inMemoryPersistence = new InMemoryPersistence();
        // Business method is linked to "createMasterData" parameter in the call below
        inMemoryPersistence.initializeDatabase("com.energyict.mdc.masterdata.impl.InstallerTest", false, true);
    }

    @AfterClass
    static public void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void installWithMasterData () {
        // Asserts
        assertThat(inMemoryPersistence.getMasterDataService().findAllMeasurementTypes().find()).isNotEmpty();
        List<RegisterType> registerTypes = inMemoryPersistence.getMasterDataService().findAllRegisterTypes().find();
        assertThat(registerTypes).hasSize(MasterDataGenerator.FixedRegisterTypes.values().length);
        registerTypes.stream().forEach(registerType -> assertThat(
                Stream.of(MasterDataGenerator.FixedRegisterTypes.values())
                        .filter(fixedRegisterType -> fixedRegisterType.getReadingType().equals(registerType.getReadingType().getMRID()))
                        .findFirst()
                        .isPresent())
                .isTrue());
    }

    @Test
    public void testGetAllRegisterTypesSupportsPaging() {
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(Optional.of(10));
        when(queryParameters.getStart()).thenReturn(Optional.of(10));
        List<RegisterType> registerTypes2 = inMemoryPersistence.getMasterDataService().findAllRegisterTypes().from(queryParameters).find();
        Assertions.assertThat(registerTypes2).hasSize(11);

    }

    @Test
    public void testGetAllRegisterTypesIsSortedByReadingTypesFullAliasName() {
        // Asserts
        List<RegisterType> registerTypes = inMemoryPersistence.getMasterDataService().findAllRegisterTypes().find();

        Assertions.assertThat(registerTypes).isSortedAccordingTo((a,b)->a.getReadingType().getFullAliasName().toUpperCase().compareTo(b.getReadingType().getFullAliasName().toUpperCase()));
    }

}