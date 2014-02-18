package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateIntervalWhenLoadProfileTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ObisCodeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.UnsupportedIntervalException;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LoadProfileTypeImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-13 (14:43)
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileTypeImplTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.MINUTES);
    private static final ObisCode OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final long PHENOMENON_ID = 151;

    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    private ReadingType readingType;
    private InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();
    private Phenomenon phenomenon;

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase("LoadProfileTypeImplTest.mdc.device.config");
        this.initializeMocks();
    }

    private void initializeMocks() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testCreateWithoutSave () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testCreateWithoutViolations";

        // Business method
        TimeDuration interval = INTERVAL_15_MINUTES;
        LoadProfileType loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getName()).isEqualTo(loadProfileTypeName);
        assertThat(loadProfileType.getDescription()).isNotEmpty();
        assertThat(loadProfileType.getObisCode()).isEqualTo(OBIS_CODE);
        assertThat(loadProfileType.getInterval()).isEqualTo(interval);
    }

    @Test
    public void testCreateWithoutViolations () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testCreateWithoutViolations";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.save();
            ctx.commit();
        }

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getName()).isEqualTo(loadProfileTypeName);
        assertThat(loadProfileType.getDescription()).isNotEmpty();
        assertThat(loadProfileType.getObisCode()).isEqualTo(OBIS_CODE);
        assertThat(loadProfileType.getInterval()).isEqualTo(interval);
    }

    @Test(expected = DuplicateNameException.class)
    public void testDuplicateName () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testDuplicateName";
        TimeDuration interval = INTERVAL_15_MINUTES;

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Setup first LoadProfileType
            LoadProfileType loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            LoadProfileType loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.save();
            ctx.commit();
        }
        catch (DuplicateNameException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_ALREADY_EXISTS);
            throw e;
        }
    }

    @Test(expected = NameIsRequiredException.class)
    public void testCreateWithoutName () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType(null, OBIS_CODE, interval);
        }
        catch (NameIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_NAME_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = NameIsRequiredException.class)
    public void testCreateWithEmptyName () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("", OBIS_CODE, interval);
        }
        catch (NameIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_NAME_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = ObisCodeIsRequiredException.class)
    public void testCreateWithoutObisCode () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithoutObisCode", null, interval);
        }
        catch (ObisCodeIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = IntervalIsRequiredException.class)
    public void testCreateWithoutInterval () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithoutInterval", OBIS_CODE, null);
        }
        catch (IntervalIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = IntervalIsRequiredException.class)
    public void testCreateWithEmptyInterval () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(0, TimeDuration.MINUTES);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithEmptyInterval", OBIS_CODE, interval);
        }
        catch (IntervalIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    public void testCreateWithIntervalInWeeks () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(1, TimeDuration.WEEKS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithIntervalInWeeks", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    public void testCreateWithNegativeIntervalSeconds () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(-1, TimeDuration.SECONDS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithNegativeIntervalSeconds", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_MUST_BE_STRICTLY_POSITIVE);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    public void testCreateWithMultipleDays () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(2, TimeDuration.DAYS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithMultipleDays", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_IN_DAYS_MUST_BE_ONE);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    public void testCreateWithMultipleMonths () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(2, TimeDuration.MONTHS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithMultipleMonths", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_IN_MONTHS_MUST_BE_ONE);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    public void testCreateWithMultipleYears () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(2, TimeDuration.YEARS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithMultipleYears", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_IN_YEARS_MUST_BE_ONE);
            throw e;
        }
    }

    @Test
    public void testUpdateInterval () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testUpdateInterval";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.save();
            ctx.commit();
        }

        // Business method
        TimeDuration updatedInterval = new TimeDuration(1, TimeDuration.HOURS);
        loadProfileType.setInterval(updatedInterval);

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getName()).isEqualTo(loadProfileTypeName);
        assertThat(loadProfileType.getDescription()).isNotEmpty();
        assertThat(loadProfileType.getObisCode()).isEqualTo(OBIS_CODE);
        assertThat(loadProfileType.getInterval()).isEqualTo(updatedInterval);
    }

    @Test(expected = CannotUpdateIntervalWhenLoadProfileTypeIsInUseException.class)
    public void testUpdateIntervalWhileInUse () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testUpdateIntervalWhileInUse";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Setup LoadProfileType
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.save();

            // Setup DeviceType with a DeviceConfiguration and a LoadProfileSpec that uses the LoadProfileType
            DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
            deviceType.addLoadProfileType(loadProfileType);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            configurationBuilder.newLoadProfileSpec(loadProfileType);
            configurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }

        // Business method
        TimeDuration updatedInterval = new TimeDuration(1, TimeDuration.HOURS);
        loadProfileType.setInterval(updatedInterval);

        // Asserts: expecting CannotUpdateIntervalWhenLoadProfileTypeIsInUseException
    }

    @Test
    public void testCreateWithRegisterMapping () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testCreateWithRegisterMapping";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        long registerMappingId;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupReadingTypeInExistingTransaction();

            // Setup ProductSpec
            ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
            productSpec.save();

            // Setup RegisterMapping
            RegisterMapping registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
            registerMapping.save();

            // Setup LoadProfileType with RegisterMapping
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.addRegisterMapping(registerMapping);

            // Business method
            loadProfileType.save();
            registerMappingId = registerMapping.getId();
            ctx.commit();
        }

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getRegisterMappings()).hasSize(1);
        RegisterMapping registerMapping = loadProfileType.getRegisterMappings().get(0);
        assertThat(registerMapping.getId()).isEqualTo(registerMappingId);
    }

    @Test
    public void testAddRegisterMapping () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testAddRegisterMapping";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterMapping registerMapping;
        LoadProfileType loadProfileType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupReadingTypeInExistingTransaction();

            // Setup ProductSpec
            ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
            productSpec.save();

            // Setup RegisterMapping
            registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
            registerMapping.save();

            // Setup LoadProfileType without RegisterMapping
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            loadProfileType.addRegisterMapping(registerMapping);
            ctx.commit();
        }

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getRegisterMappings()).containsOnly(registerMapping);
    }

    @Test
    public void testRemoveRegisterMapping () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testRemoveRegisterMapping";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterMapping registerMapping;
        LoadProfileType loadProfileType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupReadingTypeInExistingTransaction();

            // Setup ProductSpec
            ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
            productSpec.save();

            // Setup RegisterMapping
            registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
            registerMapping.save();

            // Setup LoadProfileType with RegisterMapping
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.addRegisterMapping(registerMapping);
            loadProfileType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            loadProfileType.removeRegisterMapping(registerMapping);
            ctx.commit();
        }


        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getRegisterMappings()).isEmpty();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testRemoveRegisterMappingWhileInUse () {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testRemoveRegisterMappingWhileInUse";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterMapping registerMapping;
        LoadProfileType loadProfileType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupPhenomenaInExistingTransaction();
            this.setupReadingTypeInExistingTransaction();

            // Setup ProductSpec
            ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
            productSpec.save();

            // Setup RegisterMapping
            registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
            registerMapping.save();

            // Setup LoadProfileType with RegisterMapping
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.addRegisterMapping(registerMapping);
            loadProfileType.save();

            // Setup DeviceType with a DeviceConfiguration and LoadProfileSpec and ChannelSpec that uses the LoadProfileType
            DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
            deviceType.addLoadProfileType(loadProfileType);
            deviceType.addRegisterMapping(registerMapping);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(loadProfileType);
            configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder);
            configurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            loadProfileType.removeRegisterMapping(registerMapping);
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_CHANNEL_SPEC);
            throw e;
        }
    }

    @Test
    public void testSimpleDelete () throws SQLException {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testSimpleDelete";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.save();
            ctx.commit();
        }

        // Business method
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            loadProfileType.delete();
            ctx.commit();
        }

        // Asserts
        this.assertLoadProfileTypeDoesNotExist(loadProfileType);
    }

    @Test
    public void testSimpleDeleteWithRegisterMappings () throws SQLException {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testSimpleDeleteWithRegisterMapping";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterMapping registerMapping;
        LoadProfileType loadProfileType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupReadingTypeInExistingTransaction();

            // Setup ProductSpec
            ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
            productSpec.save();

            // Setup RegisterMapping
            registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
            registerMapping.save();

            // Setup LoadProfileType with RegisterMapping
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.addRegisterMapping(registerMapping);
            loadProfileType.save();
            ctx.commit();
        }

        // Business method
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            loadProfileType.delete();
            ctx.commit();
        }

        // Asserts
        this.assertLoadProfileTypeDoesNotExist(loadProfileType);
        this.assertRegisterMappingsDoNotExist(loadProfileType);
    }

    @Test
    public void testDeleteWhenInUseByDeviceType () throws SQLException {
        DeviceConfigurationServiceImpl deviceConfigurationService = this.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testDeleteWhenInUseByDeviceType";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupPhenomenaInExistingTransaction();
            this.setupReadingTypeInExistingTransaction();

            // Setup LoadProfileType
            loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType.setDescription("For testing purposes only");
            loadProfileType.save();

            // Setup ProductSpec
            ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
            productSpec.save();

            // Setup RegisterMapping
            RegisterMapping registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
            registerMapping.save();

            // Setup DeviceType with a DeviceConfiguration and LoadProfileSpec and ChannelSpec that uses the LoadProfileType
            DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
            deviceType.addLoadProfileType(loadProfileType);
            DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(loadProfileType);
            configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder);
            configurationBuilder.add();
            deviceType.save();

            ctx.commit();
        }

        // Business method
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            loadProfileType.delete();
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_DEVICE_TYPES);
            throw e;
        }

        this.assertLoadProfileTypeDoesNotExist(loadProfileType);
    }

    private void assertLoadProfileTypeDoesNotExist(LoadProfileType loadProfileType) throws SQLException {
        SqlBuilder builder = new SqlBuilder("select * from EISLOADPROFILETYPE where id = ?");
        builder.bindLong(loadProfileType.getId());
        try (Connection connection = Environment.DEFAULT.get().getConnection()) {
            try (PreparedStatement preparedStatement = builder.getStatement(connection)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    assertThat(resultSet.next()).as("Was not expecting to find LoadProfileType " + loadProfileType.getName() + " after deletion").isFalse();
                }
            }
        }
    }

    private void assertRegisterMappingsDoNotExist(LoadProfileType loadProfileType) throws SQLException {
        SqlBuilder builder = new SqlBuilder("select * from EISREGMAPPINGINLOADPROFILETYPE where LOADPROFILETYPEID = ?");
        builder.bindLong(loadProfileType.getId());
        try (Connection connection = Environment.DEFAULT.get().getConnection()) {
            try (PreparedStatement preparedStatement = builder.getStatement(connection)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    assertThat(resultSet.next()).as("Was not expecting to find register mappings for LoadProfileType " + loadProfileType.getName() + " after deletion").isFalse();
                }
            }
        }
    }

    private void setupPhenomenaInExistingTransaction () {
        this.phenomenon = this.inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(DeviceTypeImplTest.class.getSimpleName(), Unit.get("kWh"));
        this.phenomenon.save();
    }

    private void setupReadingTypeInExistingTransaction () {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType = this.inMemoryPersistence.getMeteringService().getReadingType(code).get();
    }

}