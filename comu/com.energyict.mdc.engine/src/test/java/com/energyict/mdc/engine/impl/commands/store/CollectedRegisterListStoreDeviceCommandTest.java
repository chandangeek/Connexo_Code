package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.engine.impl.meterdata.DeviceRegisterList;
import com.energyict.mdc.engine.impl.meterdata.identifiers.RegisterDataIdentifier;
import com.energyict.mdc.engine.impl.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.metering.impl.ObisCodeToReadingTypeFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 15/01/14
 * Time: 16:08
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedRegisterListStoreDeviceCommandTest extends AbstractCollectedDataIntegrationTest{

    private final String registerObisCode1 = "1.0.1.8.0.255";
    private final String registerObisCode2 = "1.0.2.8.0.255";
    private final Unit kiloWattHours = Unit.get("kWh");
    private final int firstReadingTypeOfChannel = 0;

    private Date justBeforeRegisterReadEventTime1 = new DateTime(2013, 11, 30, 23, 50, 0, 0).toDate();
    private Date registerEventTime1 = new DateTime(2014, 1, 1, 0, 0, 0, 0).toDate();
    private Quantity register1Quantity = new Quantity(123, kiloWattHours);
    private Date registerEventTime2 = new DateTime(2014, 1, 1, 0, 23, 12, 2).toDate();
    private Date registerEventTime3 = new DateTime(2014, 1, 1, 0, 23, 12, 12).toDate();
    
    private DeviceCreator deviceCreator;

    @Before
    public void setUp() {
        this.deviceCreator = new DeviceCreator(
                getInjector().getInstance(DeviceConfigurationService.class),
                getInjector().getInstance(DeviceDataService.class),
                getInjector().getInstance(TransactionService.class));
        initializeEnvironment();
    }

    private static void initializeEnvironment() {
        Environment mockedEnvironment = mock(Environment.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(mockedEnvironment.getApplicationContext()).thenReturn(applicationContext);
        Environment.DEFAULT.set(mockedEnvironment);
    }

    @After
    public void cleanup(){
        this.deviceCreator.destroy();
    }

    @Test
    public void successfulStoreOfSingleRegisterTest() {
        Device device = this.deviceCreator.name("successfulStoreOfSingleRegisterTest").mRDI("successfulStoreOfSingleRegisterTest").create();
        long deviceId = device.getId();

        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(deviceId, getInjector().getInstance(DeviceDataService.class));
        RegisterIdentifier registerIdentifier = new RegisterDataIdentifier(ObisCode.fromString(registerObisCode1), ObisCode.fromString(registerObisCode1), deviceIdentifier);
        CollectedRegister collectedRegister = createCollectedRegister(registerIdentifier);

        DeviceRegisterList collectedRegisterList = new DeviceRegisterList(deviceIdentifier);
        collectedRegisterList.addCollectedRegister(collectedRegister);
        final CollectedRegisterListDeviceCommand collectedRegisterListDeviceCommand = new CollectedRegisterListDeviceCommand(collectedRegisterList);

        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        collectedRegisterListDeviceCommand.execute(comServerDAO);

        Optional<AmrSystem> amrSystem = getInjector().getInstance(MeteringService.class).findAmrSystem(1);
        MeterActivation currentMeterActivation = getCurrentMeterActivation(deviceId, amrSystem.get());
        assertThat(currentMeterActivation).isNotNull();
        ReadingType registerReadingType = getReadingType(currentMeterActivation, registerObisCode1, kiloWattHours);
        assertThat(registerReadingType).isNotNull();
        List<? extends BaseReadingRecord> readings = currentMeterActivation.getReadings(new Interval(justBeforeRegisterReadEventTime1, registerEventTime2), registerReadingType);
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getQuantity(firstReadingTypeOfChannel).getValue()).isEqualTo(new BigDecimal(123));
    }

    private ReadingType getReadingType(MeterActivation currentMeterActivation, String obisCode1, Unit unit) {
        String mridFromObisCodeAndUnit = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(ObisCode.fromString(obisCode1), unit);
        for (ReadingType readingType : currentMeterActivation.getReadingTypes()) {
            if(readingType.getMRID().equals(mridFromObisCodeAndUnit)){
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
        CollectedRegister collectedRegister = new DefaultDeviceRegister(registerIdentifier);
        collectedRegister.setReadTime(registerEventTime1);
        collectedRegister.setCollectedData(register1Quantity);
        return collectedRegister;
    }
}
