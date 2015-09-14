package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import java.util.Optional;
import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link LogBookTypeImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (14:40)
 */
public class LogBookTypeImplTest extends PersistenceTest {

    private static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode OBIS_CODE_2 = ObisCode.fromString("1.0.99.97.0.255");

    @Test
    @Transactional
    public void testLogBookTypeCreation() {
        String logBookTypeName = "testLogBookTypeCreation";
        LogBookType logBookType;
        // Business method
        logBookType = PersistenceTest.inMemoryPersistence.getMasterDataService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.setDescription("For testing purposes only");
        logBookType.save();

        // Asserts
        assertThat(logBookType).isNotNull();
        assertThat(logBookType.getId()).isGreaterThan(0);
        assertThat(logBookType.getName()).isEqualTo(logBookTypeName);
        assertThat(logBookType.getDescription()).isNotEmpty();
        assertThat(logBookType.getObisCode()).isEqualTo(OBIS_CODE);
    }

    @Test
    @Transactional
    public void testFindLogBookTypeAfterCreation() {
        String logBookTypeName = "testFindLogBookTypeAfterCreation";
        LogBookType logBookType = PersistenceTest.inMemoryPersistence.getMasterDataService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.setDescription("For testing purposes only");
        logBookType.save();

        // Business method
        Optional<LogBookType> logBookType2 = PersistenceTest.inMemoryPersistence.getMasterDataService().findLogBookTypeByName(logBookTypeName);

        // Asserts
        assertThat(logBookType2.isPresent()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}", strict = false)
    public void testLogBookTypeCreationWithoutName() {
        LogBookType logBookType = PersistenceTest.inMemoryPersistence.getMasterDataService().newLogBookType(null, OBIS_CODE);

        // Business method
        logBookType.save();

        // Asserts: See ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}", strict = false)
    public void testLogBookTypeCreationWithEmptyName() {
        LogBookType logBookType = PersistenceTest.inMemoryPersistence.getMasterDataService().newLogBookType("", OBIS_CODE);

        // Business method
        logBookType.save();

        // Asserts: See ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_UNIQUE + "}")
    public void testDuplicateLogBookType() {
        String logBookTypeName = "testDuplicateLogBookType";
        LogBookType logBookType;
        // Setup first LogBookType
        logBookType = PersistenceTest.inMemoryPersistence.getMasterDataService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.setDescription("For testing purposes only");
        logBookType.save();

        // Business method
        logBookType = PersistenceTest.inMemoryPersistence.getMasterDataService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.setDescription("For testing purposes only");
        logBookType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED + "}")
    public void testLogBookTypeCreationWithoutObisCode() {
        String logBookTypeName = "testDuplicateLogBookType";
        LogBookType logBookType = PersistenceTest.inMemoryPersistence.getMasterDataService().newLogBookType(logBookTypeName, null);

        // Business method
        logBookType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testUpdateObisCodeWhenNotInUse() {
        String logBookTypeName = "testUpdateObisCodeWhenNotInUse";
        LogBookType logBookType;
        logBookType = PersistenceTest.inMemoryPersistence.getMasterDataService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.setDescription("For testing purposes only");

        // Business method
        logBookType.setObisCode(OBIS_CODE_2);

        // Asserts
        assertThat(logBookType.getName()).isEqualTo(logBookTypeName);
        assertThat(logBookType.getDescription()).isNotEmpty();
        assertThat(logBookType.getObisCode()).isEqualTo(OBIS_CODE_2);
    }

    @Test
    @Transactional
    public void testDelete() {
        String logBookTypeName = "testDelete";
        LogBookType logBookType;
        logBookType = PersistenceTest.inMemoryPersistence.getMasterDataService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.save();

        // Business method
        logBookType.delete();

        // Asserts
        Optional<LogBookType> expectedNull = PersistenceTest.inMemoryPersistence.getMasterDataService().findLogBookTypeByName(logBookTypeName);

        assertThat(expectedNull.isPresent()).isFalse();
    }

}