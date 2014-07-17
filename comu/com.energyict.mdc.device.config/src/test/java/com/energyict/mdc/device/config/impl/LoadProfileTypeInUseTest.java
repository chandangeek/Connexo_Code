package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateIntervalWhenLoadProfileTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.google.common.base.Optional;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the event handlers that deal with {@link LoadProfileType}s
 * and veto the changes/deletion when the LoadProfileType is still in use.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-16 (09:08)
 */
public class LoadProfileTypeInUseTest extends PersistenceTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.MINUTES);
    private static final ObisCode OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");

    private ReadingType readingType;
    private Unit unit;
    private Phenomenon phenomenon;

    @Before
    public void registerEventHandlers () {
        inMemoryPersistence.registerEventHandlers();
    }

    @After
    public void unregisterEventHandlers () {
        inMemoryPersistence.unregisterEventHandlers();
    }

    @Test(expected = CannotUpdateIntervalWhenLoadProfileTypeIsInUseException.class)
    @Transactional
    public void testUpdateIntervalWhileInUse() {
        MasterDataService masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        DeviceConfigurationService deviceConfigurationService = PersistenceTest.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testUpdateIntervalWhileInUse";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        // Setup LoadProfileType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Setup DeviceType with a DeviceConfiguration and a LoadProfileSpec that uses the LoadProfileType
        DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        configurationBuilder.newLoadProfileSpec(loadProfileType);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        TimeDuration updatedInterval = new TimeDuration(1, TimeDuration.HOURS);
        loadProfileType.setInterval(updatedInterval);
        loadProfileType.save();

        // Asserts: expecting CannotUpdateIntervalWhenLoadProfileTypeIsInUseException
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testRemoveRegisterTypeWhileInUse() {
        MasterDataService masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        DeviceConfigurationService deviceConfigurationService = PersistenceTest.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testRemoveRegisterTypeWhileInUse";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterType registerType;
        LoadProfileType loadProfileType;
        this.setupPhenomenaInExistingTransaction();
        this.setupReadingTypeInExistingTransaction();

        // Setup RegisterType
        registerType = masterDataService.newRegisterType("testCreateWithRegisterType", OBIS_CODE, unit, readingType, readingType.getTou());
        registerType.save();

        // Setup LoadProfileType with RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        ChannelType channelTypeForRegisterType = loadProfileType.createChannelTypeForRegisterType(registerType);
        loadProfileType.save();

        // Setup DeviceType with a DeviceConfiguration and LoadProfileSpec and ChannelSpec that uses the LoadProfileType
        DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(loadProfileType);
        configurationBuilder.newChannelSpec(channelTypeForRegisterType, this.phenomenon, loadProfileSpecBuilder);
        configurationBuilder.add();
        deviceType.save();

        try {
            // Business method
            loadProfileType.removeChannelType(channelTypeForRegisterType);
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.CHANNEL_TYPE_STILL_USED_BY_CHANNEL_SPEC);
            throw e;
        }
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testDeleteWhenInUseByDeviceType() throws SQLException {
        MasterDataService masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        DeviceConfigurationService deviceConfigurationService = PersistenceTest.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testDeleteWhenInUseByDeviceType";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        this.setupPhenomenaInExistingTransaction();
        this.setupReadingTypeInExistingTransaction();

        // Setup RegisterType
        RegisterType registerType = masterDataService.newRegisterType("testCreateWithRegisterType", OBIS_CODE, unit, readingType, readingType.getTou());
        registerType.save();

        // Setup LoadProfileType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        ChannelType channelTypeForRegisterType = loadProfileType.createChannelTypeForRegisterType(registerType);
        loadProfileType.save();

        // Setup DeviceType with a DeviceConfiguration and LoadProfileSpec and ChannelSpec that uses the LoadProfileType
        DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
        deviceType.addRegisterType(registerType);
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.save();
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(loadProfileType);
        configurationBuilder.newChannelSpec(channelTypeForRegisterType, this.phenomenon, loadProfileSpecBuilder);
        configurationBuilder.add();


        // Business method
        try {
            loadProfileType.delete();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_DEVICE_TYPES);
            throw e;
        }

        this.assertLoadProfileTypeDoesNotExist(loadProfileType);
    }

    private void assertLoadProfileTypeDoesNotExist(LoadProfileType loadProfileType) {
        Optional<LoadProfileType> shouldBeNull = PersistenceTest.inMemoryPersistence.getMasterDataService().findLoadProfileType(loadProfileType.getId());
        assertThat(shouldBeNull.isPresent()).as("Was not expecting to find any LoadProfileTypes after deletinon.").isFalse();
    }

    private void setupPhenomenaInExistingTransaction() {
        this.unit = Unit.get("kWh");
        this.phenomenon = this.createPhenomenonIfMissing(unit);
    }

    private Phenomenon createPhenomenonIfMissing(Unit unit) {
        Optional<Phenomenon> phenomenonByUnit = inMemoryPersistence.getMasterDataService().findPhenomenonByUnit(unit);
        if (!phenomenonByUnit.isPresent()) {
            Phenomenon phenomenon = inMemoryPersistence.getMasterDataService().newPhenomenon(LoadProfileTypeInUseTest.class.getSimpleName(), unit);
            phenomenon.save();
            return phenomenon;
        }
        else {
            return phenomenonByUnit.get();
        }
    }

    private void setupReadingTypeInExistingTransaction() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED)
                .flow(FORWARD)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .period(TimeAttribute.MINUTE15)
                .accumulate(Accumulation.DELTADELTA)
                .code();
        this.readingType = PersistenceTest.inMemoryPersistence.getMeteringService().getReadingType(code).get();
    }

}