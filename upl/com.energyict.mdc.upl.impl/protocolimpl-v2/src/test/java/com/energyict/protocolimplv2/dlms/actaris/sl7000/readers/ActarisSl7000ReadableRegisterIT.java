package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.custom.ComposedMeterInfo;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.BillingCounterReader;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.BillingReader;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.DSTSwitchingTimeReader;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.MaxDemandRegister;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.BatteryVoltage;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.FatalOperationStatus;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.NonFatalOperationStatus;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.ProgrammingId;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.AttributeReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.common.MeterFirmwareVersion;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.common.MeterSerialNumber;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultExtendedRegister;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ActarisSl7000ReadableRegisterIT {

    @Mock
    private CollectedRegisterBuilder collectedRegisterBuilder;

    @Mock
    private ActarisSl7000 actarisSl7000;

    @Mock
    private OfflineRegister mockedRegister;

    private CollectedRegisterReader<ActarisSl7000> registryReader;

    @Before
    public void setUp() {
        ActarisSl7000ReadableRegister actarisSl7000ReadableRegister = new ActarisSl7000ReadableRegister(collectedRegisterBuilder, actarisSl7000);
        registryReader = actarisSl7000ReadableRegister.getRegistryReader(actarisSl7000);
    }

    @Test
    public void findBillingReader() {
        // since all that does not match f = 255 is a billing reader we can check only a few scenarios
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("1.1.1.1.1.1"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(BillingReader.class, obisReader.get().getClass());
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("1.2.3.4.5.6"));
        obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(BillingReader.class, obisReader.get().getClass());
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("255.255.255.255.255.0"));
        obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(BillingReader.class, obisReader.get().getClass());
    }

    @Test
    public void findBillingCounter1() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("1.0.0.1.0.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(BillingCounterReader.class, obisReader.get().getClass());
    }

    @Test
    public void findBillingCounter2() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("1.1.0.1.0.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(BillingCounterReader.class, obisReader.get().getClass());
    }

    @Test
    public void firmwareVersion() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.142.1.1.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(MeterFirmwareVersion.class, obisReader.get().getClass());
    }

    @Test
    public void serialNumber() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ComposedMeterInfo.OBISCODE_SERIAL_NUMBER_REQ);
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(MeterSerialNumber.class, obisReader.get().getClass());
    }

    @Test
    public void programmingId() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.96.2.0.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(ProgrammingId.class, obisReader.get().getClass());
    }

    @Test
    public void numberOfConfigurations() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.96.1.4.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(DefaultExtendedRegister.class, obisReader.get().getClass());
    }

    @Test
    public void dstWorkingMode() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.131.0.4.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(AttributeReader.class, obisReader.get().getClass());
    }

    @Test
    public void dstSwitchingTime() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.131.0.6.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(DSTSwitchingTimeReader.class, obisReader.get().getClass());
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.131.0.7.255"));
        obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(DSTSwitchingTimeReader.class, obisReader.get().getClass());
    }

    @Test
    public void batteryExpiry() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.96.6.2.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(AttributeReader.class, obisReader.get().getClass());
    }

    @Test
    public void batteryVoltage() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.96.6.3.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(BatteryVoltage.class, obisReader.get().getClass());
    }

    @Test
    public void fatalOpStatus() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.97.97.1.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(FatalOperationStatus.class, obisReader.get().getClass());
    }

    @Test
    public void nonFatalOpStatus() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.97.97.2.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(NonFatalOperationStatus.class, obisReader.get().getClass());
    }

    @Test
    public void maxDemandRegisters() {
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.98.133.61.255"));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, ActarisSl7000>> obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(MaxDemandRegister.class, obisReader.get().getClass());
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.98.133.62.255"));
        obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(MaxDemandRegister.class, obisReader.get().getClass());
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.98.133.63.255"));
        obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(MaxDemandRegister.class, obisReader.get().getClass());
        Mockito.when(mockedRegister.getObisCode()).thenReturn(ObisCode.fromString("0.0.98.133.64.255"));
        obisReader = registryReader.getCollectedRegisterReaderFinder().find(actarisSl7000, mockedRegister);
        Assert.assertTrue(obisReader.isPresent());
        Assert.assertEquals(MaxDemandRegister.class, obisReader.get().getClass());
    }


}