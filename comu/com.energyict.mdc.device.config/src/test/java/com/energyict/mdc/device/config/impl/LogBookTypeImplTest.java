package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLogBookTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ObisCodeIsRequiredException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LogBookTypeImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (14:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookTypeImplTest {

    private static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode OBIS_CODE_2 = ObisCode.fromString("1.0.99.97.0.255");
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    private InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase("LogBookTypeImplTest.mdc.device.config");
        this.initializeMocks();
    }

    private void initializeMocks() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testLogBookTypeCreation() {
        String logBookTypeName = "testLogBookTypeCreation";
        LogBookType logBookType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
            logBookType.setDescription("For testing purposes only");
            logBookType.save();
            ctx.commit();
        }

        // Asserts
        assertThat(logBookType).isNotNull();
        assertThat(logBookType.getId()).isGreaterThan(0);
        assertThat(logBookType.getName()).isEqualTo(logBookTypeName);
        assertThat(logBookType.getDescription()).isNotEmpty();
        assertThat(logBookType.getObisCode()).isEqualTo(OBIS_CODE);
    }

    @Test
    public void testFindLogBookTypeAfterCreation() {
        String logBookTypeName = "testFindLogBookTypeAfterCreation";
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LogBookType logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
            logBookType.setDescription("For testing purposes only");
            logBookType.save();
            ctx.commit();
        }

        // Business method
        LogBookType logBookType = this.inMemoryPersistence.getDeviceConfigurationService().findLogBookTypeByName(logBookTypeName);

        // Asserts
        assertThat(logBookType).isNotNull();
    }

    @Test(expected = NameIsRequiredException.class)
    public void testLogBookTypeCreationWithoutName() {
        // Business method
        this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(null, OBIS_CODE);

        // Asserts: Should be getting a NameIsRequiredException
    }

    @Test(expected = NameIsRequiredException.class)
    public void testLogBookTypeCreationWithEmptyName() {
        // Business method
        this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType("", OBIS_CODE);

        // Asserts: Should be getting a NameIsRequiredException
    }

    @Test(expected = DuplicateNameException.class)
    public void testDuplicateLogBookType() {
        String logBookTypeName = "testDuplicateLogBookType";
        LogBookType logBookType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Setup first LogBookType
            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
            logBookType.setDescription("For testing purposes only");
            logBookType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
            logBookType.setDescription("For testing purposes only");
            logBookType.save();
            ctx.commit();
        }
        catch (DuplicateNameException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOG_BOOK_TYPE_ALREADY_EXISTS);
            throw e;
        }
    }

    @Test(expected = ObisCodeIsRequiredException.class)
    public void testLogBookTypeCreationWithoutObisCode() {
        String logBookTypeName = "testDuplicateLogBookType";
        // Business method
        this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, null);

        // Asserts: Should be getting a NameIsRequiredException
    }

    @Test
    public void testUpdateObisCodeWhenNotInUse () {
        String logBookTypeName = "testUpdateObisCodeWhenNotInUse";
        LogBookType logBookType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
            logBookType.setDescription("For testing purposes only");
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            logBookType.setObisCode(OBIS_CODE_2);
            ctx.commit();
        }

        // Asserts
        assertThat(logBookType.getName()).isEqualTo(logBookTypeName);
        assertThat(logBookType.getDescription()).isNotEmpty();
        assertThat(logBookType.getObisCode()).isEqualTo(OBIS_CODE_2);
    }

    @Test(expected = CannotUpdateObisCodeWhenLogBookTypeIsInUseException.class)
    public void testCannotUpdateObisCodeWhenInUse () {
        String logBookTypeName = "testCannotUpdateObisCodeWhenInUse";
        LogBookType logBookType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Create LogBookType
            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
            logBookType.save();

            // Use the LogBookType in a DeviceType
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(logBookTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLogBookType(logBookType);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            configurationBuilder.newLogBookSpec(logBookType);
            configurationBuilder.add();
            deviceType.save();

            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            logBookType.setObisCode(OBIS_CODE_2);
            ctx.commit();
        }

        // Asserts: expected CannotUpdateObisCodeWhenLogBookTypeIsInUseException
    }

    @Test
    public void testDelete () {
        String logBookTypeName = "testDelete";
        LogBookType logBookType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
            logBookType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            logBookType.delete();
            ctx.commit();
        }

        // Asserts
        LogBookType expectedNull = this.inMemoryPersistence.getDeviceConfigurationService().findLogBookTypeByName(logBookTypeName);

        assertThat(expectedNull).isNull();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testDeleteWhileInUse () {
        String logBookTypeName = "testDeleteWhileInUse";
        LogBookType logBookType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Create LogBookType
            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(logBookTypeName, OBIS_CODE);
            logBookType.save();

            // Use the LogBookType in a DeviceType
            DeviceType deviceType = this.inMemoryPersistence.getDeviceConfigurationService().newDeviceType(logBookTypeName, this.deviceProtocolPluggableClass);
            deviceType.setDescription("For testing purposes only");
            deviceType.addLogBookType(logBookType);
            deviceType.save();

            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            logBookType.delete();
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS);
            throw e;
        }
    }

}