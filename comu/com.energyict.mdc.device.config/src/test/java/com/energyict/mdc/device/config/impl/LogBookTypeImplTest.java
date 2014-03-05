package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLogBookTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link LogBookTypeImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (14:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookTypeImplTest extends PersistenceTest {

    private static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode OBIS_CODE_2 = ObisCode.fromString("1.0.99.97.0.255");

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Test
    @Transactional
    public void testLogBookTypeCreation() {
        String logBookTypeName = "testLogBookTypeCreation";
        LogBookType logBookType;
        // Business method
        logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
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
        LogBookType logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.setDescription("For testing purposes only");
        logBookType.save();

        // Business method
        LogBookType logBookType2 = inMemoryPersistence.getDeviceConfigurationService().findLogBookTypeByName(logBookTypeName);

        // Asserts
        assertThat(logBookType2).isNotNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    public void testLogBookTypeCreationWithoutName() {
        LogBookType logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(null, OBIS_CODE);

        // Business method
        logBookType.save();

        // Asserts: See ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    public void testLogBookTypeCreationWithEmptyName() {
        LogBookType logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType("", OBIS_CODE);

        // Business method
        logBookType.save();

        // Asserts: See ExpectedConstraintViolation rule
    }

    @Test(expected = DuplicateNameException.class)
    @Transactional
    public void testDuplicateLogBookType() {
        String logBookTypeName = "testDuplicateLogBookType";
        LogBookType logBookType;
        // Setup first LogBookType
        logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.setDescription("For testing purposes only");
        logBookType.save();

        try {
            // Business method
            logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
            logBookType.setDescription("For testing purposes only");
            logBookType.save();
        }
        catch (DuplicateNameException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOG_BOOK_TYPE_ALREADY_EXISTS);
            throw e;
        }
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED_KEY + "}")
    public void testLogBookTypeCreationWithoutObisCode() {
        String logBookTypeName = "testDuplicateLogBookType";
        LogBookType logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, null);

        // Business method
        logBookType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testUpdateObisCodeWhenNotInUse() {
        String logBookTypeName = "testUpdateObisCodeWhenNotInUse";
        LogBookType logBookType;
        logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.setDescription("For testing purposes only");

        // Business method
        logBookType.setObisCode(OBIS_CODE_2);

        // Asserts
        assertThat(logBookType.getName()).isEqualTo(logBookTypeName);
        assertThat(logBookType.getDescription()).isNotEmpty();
        assertThat(logBookType.getObisCode()).isEqualTo(OBIS_CODE_2);
    }

    @Test(expected = CannotUpdateObisCodeWhenLogBookTypeIsInUseException.class)
    @Transactional
    public void testCannotUpdateObisCodeWhenInUse() {
        String logBookTypeName = "testCannotUpdateObisCodeWhenInUse";
        LogBookType logBookType;
        // Create LogBookType
        logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.save();

        // Use the LogBookType in a DeviceType
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(logBookTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLogBookType(logBookType);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        configurationBuilder.newLogBookSpec(logBookType);
        configurationBuilder.add();
        deviceType.save();


        // Business method
        logBookType.setObisCode(OBIS_CODE_2);

        // Asserts: expected CannotUpdateObisCodeWhenLogBookTypeIsInUseException
    }

    @Test
    @Transactional
    public void testDelete() {
        String logBookTypeName = "testDelete";
        LogBookType logBookType;
        logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.save();

        // Business method
        logBookType.delete();

        // Asserts
        LogBookType expectedNull = inMemoryPersistence.getDeviceConfigurationService().findLogBookTypeByName(logBookTypeName);

        assertThat(expectedNull).isNull();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testDeleteWhileInUse() {
        String logBookTypeName = "testDeleteWhileInUse";
        LogBookType logBookType;
        // Create LogBookType
        logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
        logBookType.save();

        // Use the LogBookType in a DeviceType
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(logBookTypeName, this.deviceProtocolPluggableClass);
        deviceType.setDescription("For testing purposes only");
        deviceType.addLogBookType(logBookType);
        deviceType.save();


        try {
            // Business method
            logBookType.delete();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS);
            throw e;
        }
    }

}