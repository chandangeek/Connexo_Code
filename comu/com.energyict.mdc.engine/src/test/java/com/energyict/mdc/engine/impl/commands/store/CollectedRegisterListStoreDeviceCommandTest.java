package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.engine.impl.meterdata.DeviceRegisterList;
import com.energyict.mdc.engine.impl.meterdata.identifiers.RegisterDataIdentifier;
import com.energyict.mdc.metering.impl.ObisCodeToReadingTypeFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.Ranges;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 15/01/14
 * Time: 16:08
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedRegisterListStoreDeviceCommandTest extends AbstractCollectedDataIntegrationTest {

    private final String registerObisCode1 = "1.0.1.8.0.255";
    private final String registerObisCode2 = "1.0.2.8.0.255";
    private final Unit kiloWattHours = Unit.get("kWh");
    private final int firstReadingTypeOfChannel = 0;

    private Date justBeforeRegisterReadEventTime1 = new DateTime(2013, 11, 30, 23, 50, 0, 0).toDate();
    private Instant registerEventTime1 = Instant.ofEpochMilli(1388530800000L);  // Dec 31st, 2014 23:00:00 (UTC)
    private Quantity register1Quantity = new Quantity(123, kiloWattHours);
    private Date registerEventTime2 = new DateTime(2014, 1, 1, 0, 23, 12, 2).toDate();
    private Date registerEventTime3 = new DateTime(2014, 1, 1, 0, 23, 12, 12).toDate();

    private DeviceCreator deviceCreator;

    @Before
    public void setUp() {
        this.deviceCreator = new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceService.class)
        );
    }

    @Test
    @Transactional
    public void successfulStoreOfSingleRegisterTest() {
        Device device = this.deviceCreator.name("successfulStoreOfSingleRegisterTest").mRDI("successfulStoreOfSingleRegisterTest").create();
        long deviceId = device.getId();

        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(deviceId, getInjector().getInstance(DeviceService.class));
        RegisterIdentifier registerIdentifier = new RegisterDataIdentifier(ObisCode.fromString(registerObisCode1), ObisCode.fromString(registerObisCode1), deviceIdentifier);
        CollectedRegister collectedRegister = createCollectedRegister(registerIdentifier);

        DeviceRegisterList collectedRegisterList = new DeviceRegisterList(deviceIdentifier);
        collectedRegisterList.addCollectedRegister(collectedRegister);
        MdcReadingTypeUtilServiceAndClock serviceProvider = new MdcReadingTypeUtilServiceAndClock();
        MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommandImpl(serviceProvider);
        CollectedRegisterListDeviceCommand collectedRegisterListDeviceCommand = new CollectedRegisterListDeviceCommand(collectedRegisterList, null, meterDataStoreCommand, serviceProvider);

        OfflineRegister offlineRegister = mock(OfflineRegister.class);
        when(offlineRegister.getOverFlowValue()).thenReturn(new BigDecimal(Double.MAX_VALUE));
        ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();
        when(comServerDAO.findOfflineRegister(registerIdentifier)).thenReturn(offlineRegister);

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

    private ComServerDAOImpl mockComServerDAOButCallRealMethodForMeterReadingStoring() {
        final ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        doCallRealMethod().when(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), any(MeterReading.class));
        when(comServerDAO.executeTransaction(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return getTransactionService().execute((Transaction<?>) invocation.getArguments()[0]);
            }
        });
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
}
