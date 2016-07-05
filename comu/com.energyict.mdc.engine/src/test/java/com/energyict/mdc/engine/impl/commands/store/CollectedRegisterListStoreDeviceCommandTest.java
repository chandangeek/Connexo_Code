package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.engine.impl.meterdata.DeviceRegisterList;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.metering.impl.ObisCodeToReadingTypeFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Copyrights EnergyICT
 * Date: 15/01/14
 * Time: 16:08
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedRegisterListStoreDeviceCommandTest extends AbstractCollectedDataIntegrationTest {

    private final String registerObisCode1 = "1.0.1.8.0.255";
    private final Unit kiloWattHours = Unit.get("kWh");
    private final int firstReadingTypeOfChannel = 0;

    private Date justBeforeRegisterReadEventTime1 = new DateTime(2013, 11, 30, 23, 50, 0, 0).toDate();
    private Instant registerEventTime1 = Instant.ofEpochMilli(1388530800000L);  // Dec 31st, 2014 23:00:00 (UTC)
    private Quantity register1Quantity = new Quantity(123, kiloWattHours);
    private Date registerEventTime2 = new DateTime(2014, 1, 1, 0, 23, 12, 2).toDate();

    private DeviceCreator deviceCreator;

    @Mock
    private TopologyService topologyService;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ComServerDAOImpl.ServiceProvider serviceProvider;
    private RegisterType registerType;

    @Before
    public void setUp() {
        when(this.serviceProvider.identificationService()).thenReturn(this.identificationService);
        ReadingType readingType = getMeteringService().getReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        registerType = getMasterDataService().findRegisterTypeByReadingType(readingType).orElseGet(() -> {
            RegisterType registerType = getMasterDataService().newRegisterType(readingType, ObisCode.fromString(registerObisCode1));
            registerType.save();
            return registerType;
        });
        this.deviceCreator = new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)
        );
    }

    @Test
    @Transactional
    public void successfulStoreOfSingleRegisterTest() {
        Device device = this.deviceCreator.name("successfulStoreOfSingleRegisterTest").mRDI("successfulStoreOfSingleRegisterTest").registerType(registerType)
                .create(Instant.ofEpochMilli(justBeforeRegisterReadEventTime1.getTime()));
        long deviceId = device.getId();

        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(deviceId, getInjector().getInstance(DeviceService.class));

        when(identificationService.createDeviceIdentifierForAlreadyKnownDevice(device)).thenReturn(deviceIdentifier);

        Register register = createMockedRegister(ObisCode.fromString(registerObisCode1));
        when(register.getDevice()).thenReturn(device);

        RegisterIdentifier registerIdentifier = mock(RegisterIdentifier.class);
        when(registerIdentifier.findRegister()).thenReturn(register);
        when(registerIdentifier.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(registerIdentifier.getObisCode()).thenReturn(ObisCode.fromString(registerObisCode1));

        CollectedRegister collectedRegister = createCollectedRegister(registerIdentifier);

        DeviceRegisterList collectedRegisterList = new DeviceRegisterList(deviceIdentifier);
        collectedRegisterList.addCollectedRegister(collectedRegister);
        MdcReadingTypeUtilServiceAndClock serviceProvider = new MdcReadingTypeUtilServiceAndClock();

        MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommandImpl(null, serviceProvider);
        CollectedRegisterListDeviceCommand collectedRegisterListDeviceCommand = new CollectedRegisterListDeviceCommand(collectedRegisterList, null, meterDataStoreCommand, serviceProvider);

        ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        // Business method
        collectedRegisterListDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        Optional<AmrSystem> amrSystem = getInjector().getInstance(MeteringService.class).findAmrSystem(1);
        MeterActivation currentMeterActivation = getCurrentMeterActivation(deviceId, amrSystem.get());
        assertThat(currentMeterActivation).isNotNull();
        ReadingType registerReadingType = getReadingType(currentMeterActivation, registerObisCode1, kiloWattHours);
        assertThat(registerReadingType).isNotNull();
        List<? extends BaseReadingRecord> readings =
                currentMeterActivation.getReadings(
                        Ranges.closedOpen(
                                justBeforeRegisterReadEventTime1.toInstant(),
                                registerEventTime2.toInstant()),
                        registerReadingType);
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getQuantity(firstReadingTypeOfChannel).getValue()).isEqualTo(new BigDecimal(123));
    }

    @Test
    @Transactional
    public void successfulStoreOfSingleRegisterTestForUnlinkedDataLogger() {
        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("unLinkedDataLogger")
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .registerType(registerType)
                .dataLoggerEnabled(true)
                .create(justBeforeRegisterReadEventTime1.toInstant());

        long deviceId = dataLogger.getId();

        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(deviceId, getInjector().getInstance(DeviceService.class));

        when(identificationService.createDeviceIdentifierForAlreadyKnownDevice(dataLogger)).thenReturn(deviceIdentifier);
        when(serviceProvider.topologyService()).thenReturn(topologyService);


        Register register = createMockedRegister(ObisCode.fromString(registerObisCode1));
        when(register.getDevice()).thenReturn(dataLogger);

        // DataLogger is not linked
        when(topologyService.getSlaveRegister(register, registerEventTime1)).thenReturn(Optional.empty());

        RegisterIdentifier registerIdentifier = mock(RegisterIdentifier.class);
        when(registerIdentifier.findRegister()).thenReturn(register);
        when(registerIdentifier.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(registerIdentifier.getObisCode()).thenReturn(ObisCode.fromString(registerObisCode1));

        CollectedRegister collectedRegister = createCollectedRegister(registerIdentifier);

        DeviceRegisterList collectedRegisterList = new DeviceRegisterList(deviceIdentifier);
        collectedRegisterList.addCollectedRegister(collectedRegister);
        MdcReadingTypeUtilServiceAndClock serviceProvider = new MdcReadingTypeUtilServiceAndClock();

        MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommandImpl(null, serviceProvider);
        CollectedRegisterListDeviceCommand collectedRegisterListDeviceCommand = new CollectedRegisterListDeviceCommand(collectedRegisterList, null, meterDataStoreCommand, serviceProvider);

        ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        // Business method
        collectedRegisterListDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        Optional<AmrSystem> amrSystem = getInjector().getInstance(MeteringService.class).findAmrSystem(1);
        MeterActivation currentMeterActivation = getCurrentMeterActivation(deviceId, amrSystem.get());
        assertThat(currentMeterActivation).isNotNull();
        ReadingType registerReadingType = getReadingType(currentMeterActivation, registerObisCode1, kiloWattHours);
        assertThat(registerReadingType).isNotNull();
        List<? extends BaseReadingRecord> readings =
                currentMeterActivation.getReadings(
                        Ranges.closedOpen(
                                justBeforeRegisterReadEventTime1.toInstant(),
                                registerEventTime2.toInstant()),
                        registerReadingType);
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getQuantity(firstReadingTypeOfChannel).getValue()).isEqualTo(new BigDecimal(123));
    }

    @Test
    @Transactional
    public void successfulStoreOfSingleRegisterTestForLinkedDataLogger() {
        DeviceCreator slaveDeviceCreator = (DeviceCreator) new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)
        ).dataLoggerSlaveDevice();

        Device dataLogger = this.deviceCreator
                .name("DataLogger")
                .mRDI("unLinkedDataLogger")
                .deviceTypeName(DeviceCreator.DATA_LOGGER_DEVICE_TYPE_NAME)
                .deviceConfigName(DeviceCreator.DATA_LOGGER_DEVICE_CONFIGURATION_NAME)
                .registerType(registerType)
                .dataLoggerEnabled(true)
                .create(justBeforeRegisterReadEventTime1.toInstant());

        long dataLoggerId = dataLogger.getId();

        DeviceIdentifier dataLoggerIdentifier = new DeviceIdentifierById(dataLoggerId, getInjector().getInstance(DeviceService.class));

        when(identificationService.createDeviceIdentifierForAlreadyKnownDevice(dataLogger)).thenReturn(dataLoggerIdentifier);
        when(serviceProvider.topologyService()).thenReturn(topologyService);

        Register dataLoggerRegister = createMockedRegister(ObisCode.fromString(registerObisCode1));
        when(dataLoggerRegister.getDevice()).thenReturn(dataLogger);

        Device slave = slaveDeviceCreator
                .name("slave")
                .mRDI("simplePreStoreWithDataInFutureTest")
                .registerType(registerType)
                .create(justBeforeRegisterReadEventTime1.toInstant());
        long slaveId = dataLogger.getId();
        DeviceIdentifier slaveIdentifier = new DeviceIdentifierById(slaveId, getInjector().getInstance(DeviceService.class));
        when(identificationService.createDeviceIdentifierForAlreadyKnownDevice(slave)).thenReturn(slaveIdentifier);

        // Linked slave register
        Register slaveRegister = createMockedRegister(ObisCode.fromString(registerObisCode1));
        when(slaveRegister.getDevice()).thenReturn(slave);

        // DataLogger is not linked
        when(topologyService.getSlaveRegister(eq(dataLoggerRegister), any(Instant.class))).thenReturn(Optional.of(slaveRegister));


        RegisterIdentifier registerIdentifier = mock(RegisterIdentifier.class);
        when(registerIdentifier.findRegister()).thenReturn(dataLoggerRegister);
        when(registerIdentifier.getDeviceIdentifier()).thenReturn(dataLoggerIdentifier);
        when(registerIdentifier.getObisCode()).thenReturn(ObisCode.fromString(registerObisCode1));

        CollectedRegister collectedRegister = createCollectedRegister(registerIdentifier);

        DeviceRegisterList collectedRegisterList = new DeviceRegisterList(dataLoggerIdentifier);
        collectedRegisterList.addCollectedRegister(collectedRegister);
        MdcReadingTypeUtilServiceAndClock serviceProvider = new MdcReadingTypeUtilServiceAndClock();

        MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommandImpl(null, serviceProvider);
        CollectedRegisterListDeviceCommand collectedRegisterListDeviceCommand = new CollectedRegisterListDeviceCommand(collectedRegisterList, null, meterDataStoreCommand, serviceProvider);

        ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        // Business method
        collectedRegisterListDeviceCommand.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // Asserts
        Optional<AmrSystem> amrSystem = getInjector().getInstance(MeteringService.class).findAmrSystem(1);
        MeterActivation currentMeterActivation = getCurrentMeterActivation(slaveId, amrSystem.get());
        assertThat(currentMeterActivation).isNotNull();
        ReadingType registerReadingType = getReadingType(currentMeterActivation, registerObisCode1, kiloWattHours);
        assertThat(registerReadingType).isNotNull();
        List<? extends BaseReadingRecord> readings =
                currentMeterActivation.getReadings(
                        Ranges.closedOpen(
                                justBeforeRegisterReadEventTime1.toInstant(),
                                registerEventTime2.toInstant()),
                        registerReadingType);
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getQuantity(firstReadingTypeOfChannel).getValue()).isEqualTo(new BigDecimal(123));
    }

    private ComServerDAOImpl mockComServerDAOButCallRealMethodForMeterReadingStoring() {
        final ComServerDAOImpl comServerDAO = spy(new ComServerDAOImpl(this.serviceProvider));
        doCallRealMethod().when(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), any(MeterReading.class));
        doCallRealMethod().when(comServerDAO).findOfflineRegister(any(RegisterIdentifier.class), any(Instant.class));
        doAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform()).when(comServerDAO).executeTransaction(any());
        return comServerDAO;
    }

    private ReadingType getReadingType(MeterActivation currentMeterActivation, String obisCode1, Unit unit) {
        String mridFromObisCodeAndUnit = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(ObisCode.fromString(obisCode1), unit);
        for (ReadingType readingType : currentMeterActivation.getReadingTypes()) {
            if (readingType.getMRID().equals(mridFromObisCodeAndUnit)) {
                return readingType;
            }
        }
        return null;
    }

    private MeterActivation getCurrentMeterActivation(long deviceId, AmrSystem amrSystem) {
        for (MeterActivation meterActivation : amrSystem.findMeter(String.valueOf(deviceId)).get().getMeterActivations()) {
            if (meterActivation.isCurrent()) {
                return meterActivation;
            }
        }
        return null;
    }

    private CollectedRegister createCollectedRegister(RegisterIdentifier registerIdentifier) {
        CollectedRegister collectedRegister = new DefaultDeviceRegister(registerIdentifier, getMdcReadingTypeUtilService().getReadingTypeFrom(registerIdentifier.getObisCode(), kiloWattHours));
        collectedRegister.setReadTime(registerEventTime1);
        collectedRegister.setCollectedData(register1Quantity);
        return collectedRegister;
    }

    private Register createMockedRegister(final ObisCode obisCode) {
        final String serialNumber = "MeterSerialNumber";
        RegisterSpec registerSpec = mock(RegisterSpec.class, withSettings().extraInterfaces(NumericalRegisterSpec.class));
        when(((NumericalRegisterSpec) registerSpec).getOverflowValue()).thenReturn(Optional.empty());
        RegisterGroup registerGroup = mock(RegisterGroup.class);
        when(registerGroup.getId()).thenReturn(1L);
        Register register = mock(Register.class);
        when(register.getDeviceObisCode()).thenReturn(obisCode);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        return register;
    }
}
