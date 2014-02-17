package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateLogBookTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.LogbookTypeIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link LogBookSpecImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/02/14
 * Time: 11:22
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookSpecImplTest extends CommonDeviceConfigSpecsTest{

    private static final String DEVICE_CONFIGURATION_NAME = LogBookSpecImplTest.class.getName() + "Config";
    private static final String LOGBOOK_TYPE_NAME = LogBookSpecImplTest.class.getName() + "LogBookType";

    private final ObisCode logBookTypeObisCode = ObisCode.fromString("0.0.99.98.0.255");
    private final ObisCode overruledLogBookSpecObisCode = ObisCode.fromString("1.0.99.97.0.255");

    private DeviceConfiguration deviceConfiguration;
    private LogBookType logBookType;

    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration() {
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME, logBookTypeObisCode);
            logBookType.save();

            // Business method
            deviceType.setDescription("For logBookSpec Test purposes only");
            deviceType.addLogBookType(logBookType);
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
            deviceConfiguration = deviceConfigurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }
    }

    private LogBookSpec createDefaultTestingLogBookSpecWithOverruledObisCode() {
        LogBookSpec logBookSpec;
        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = deviceConfiguration.createLogBookSpec(this.logBookType);
            logBookSpecBuilder.setOverruledObisCode(overruledLogBookSpecObisCode);
            logBookSpec = logBookSpecBuilder.add();
            tctx.commit();
        }
        return logBookSpec;
    }

    @Test
    public void createLogBookSpecTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        assertThat(logBookSpec.getLogBookType()).isEqualTo(this.logBookType);
        assertThat(logBookSpec.getDeviceObisCode()).isEqualTo(overruledLogBookSpecObisCode);
        assertThat(logBookSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(logBookSpec.getObisCode()).isEqualTo(this.logBookTypeObisCode);
    }

    @Test
    public void updateLogBookSpecTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LogBookSpec.LogBookSpecUpdater logBookSpecUpdater = this.deviceConfiguration.getLogBookSpecUpdaterFor(logBookSpec);
            logBookSpecUpdater.setOverruledObisCode(null);
            logBookSpecUpdater.update();
            tctx.commit();
        }

        assertThat(logBookSpec.getLogBookType()).isEqualTo(this.logBookType);
        assertThat(logBookSpec.getDeviceObisCode()).isEqualTo(this.logBookTypeObisCode);
        assertThat(logBookSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(logBookSpec.getObisCode()).isEqualTo(this.logBookTypeObisCode);
    }

    @Test(expected = LogbookTypeIsNotConfiguredOnDeviceTypeException.class)
    public void createWithIncorrectLogBookType() {
        LogBookSpec logBookSpec;
        LogBookType logBookType;
        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {

            logBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", ObisCode.fromString("1.0.1.0.1.0"));
            logBookType.save();

            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = deviceConfiguration.createLogBookSpec(logBookType);
            logBookSpecBuilder.setOverruledObisCode(overruledLogBookSpecObisCode);
            logBookSpec = logBookSpecBuilder.add();
            // we will not be able to commit it
        }
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    public void addWithActiveDeviceConfigurationTest() {
        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.deviceConfiguration.activate();
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = deviceConfiguration.createLogBookSpec(this.logBookType);
            logBookSpecBuilder.setOverruledObisCode(overruledLogBookSpecObisCode);
            LogBookSpec logBookSpec = logBookSpecBuilder.add();
            tctx.commit();
        }
    }

    @Test(expected = DuplicateLogBookTypeException.class)
    public void addTwoSpecsWithSameLogBookTypeTest() {
        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(this.logBookType);
            LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(this.logBookType);
            LogBookSpec logBookSpec2 = logBookSpecBuilder2.add();
            tctx.commit();
        }
    }

    @Test(expected = DuplicateObisCodeException.class)
    public void addTwoSpecsWithDiffTypeButSameObisCodeTest() {
        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LogBookType otherLogBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", logBookTypeObisCode);
            otherLogBookType.save();
            this.deviceType.addLogBookType(otherLogBookType);
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(this.logBookType);
            LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(otherLogBookType);
            LogBookSpec logBookSpec2 = logBookSpecBuilder2.add();
            tctx.commit();
        }
    }

    @Test(expected = DuplicateObisCodeException.class)
    public void addTwoSpecsWithDiffObisCodeButOverruledAsSameObisCodeTest() {
        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LogBookType otherLogBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", ObisCode.fromString("1.0.1.0.1.0"));
            otherLogBookType.save();
            this.deviceType.addLogBookType(otherLogBookType);
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(this.logBookType);
            LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(otherLogBookType);
            logBookSpecBuilder2.setOverruledObisCode(logBookTypeObisCode);
            LogBookSpec logBookSpec2 = logBookSpecBuilder2.add();
            tctx.commit();
        }
    }

    @Test(expected = DuplicateObisCodeException.class)
    public void addTwoSpecsWithDiffObisCodeButSameAfterUpdateTest() {
        LogBookSpec logBookSpec2;
        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LogBookType otherLogBookType = this.inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", ObisCode.fromString("1.0.1.0.1.0"));
            otherLogBookType.save();
            this.deviceType.addLogBookType(otherLogBookType);
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(this.logBookType);
            LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
            LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(otherLogBookType);
            logBookSpec2 = logBookSpecBuilder2.add();
            LogBookSpec.LogBookSpecUpdater logBookSpecUpdater = this.deviceConfiguration.getLogBookSpecUpdaterFor(logBookSpec2);
            logBookSpecUpdater.setOverruledObisCode(logBookTypeObisCode);
            logBookSpecUpdater.update();
            tctx.commit();
        }
    }

    @Test
    public void successfulDeleteTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            deviceConfiguration.deleteLogBookSpec(logBookSpec);
            tctx.commit();
        }
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    public void cannotDeleteWhenConfigIsActiveTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.deviceConfiguration.activate();
            this.deviceConfiguration.deleteLogBookSpec(logBookSpec);
            tctx.commit();
        }
    }
}