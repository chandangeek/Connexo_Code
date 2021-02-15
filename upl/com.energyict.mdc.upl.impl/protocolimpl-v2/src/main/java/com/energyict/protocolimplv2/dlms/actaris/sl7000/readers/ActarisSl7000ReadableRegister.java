package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.custom.ComposedMeterInfo;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.obis.matchers.ChannelValueNotMatching;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.BillingCounterReader;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.BillingReader;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.DSTSwitchingTimeReader;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.mappers.BatterExpireDate;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.mappers.DSTWorkingModeMapper;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.BatteryVoltage;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.FatalOperationStatus;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.MaxDemandRegister;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.NonFatalOperationStatus;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.ProgrammingId;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers.RegisterProfile;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.DlmsClassIdMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.GenricMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisChannel;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisCodeMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.AttributeReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.common.MeterFirmwareVersion;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.common.MeterSerialNumber;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.PerTypeMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultDemandRegister;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultExtendedRegister;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultRegister;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReaderFinder;
import com.energyict.protocolimplv2.dlms.common.readers.ReaderRegistry;

import java.util.ArrayList;
import java.util.List;

public class ActarisSl7000ReadableRegister {

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final ActarisSl7000 protocol;

    public ActarisSl7000ReadableRegister(CollectedRegisterBuilder collectedRegisterBuilder, ActarisSl7000 protocol) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.protocol = protocol;
    }

    public CollectedRegisterReader<ActarisSl7000> getRegistryReader(ActarisSl7000 dlmsProtocol) {
        List<ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000>> readers = new ArrayList<>();
        readers.add(billing());
        readers.add(billingCounter1());
        readers.add(billingCounter2());
        readers.add(meterFirmwareVersion());
        readers.add(meterSerialNumber());
        readers.add(programmingId());
        readers.add(numberOfConfigurations());
        readers.add(dstWorkingMode());
        readers.add(dstSwitchingTime());
        readers.add(batteryExpireDate());
        readers.add(batteryVoltage());
        readers.add(fatalOperationStatus());
        readers.add(nonFatalOperationStatus());
        readers.add(maxDemandRegister1());
        readers.add(maxDemandRegister2());
        readers.add(maxDemandRegister3());
        readers.add(maxDemandRegister4());
        readers.add(allDemandsProfile());
        readers.add(allCumulativeProfile());
        readers.add(allEnergyProfile());
        readers.add(allTotalEnergyProfile());
        ReaderRegistry<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> readableRegisters = new ReaderRegistry<>(readers);
        List<ObisReader<CollectedRegister, OfflineRegister, DLMSClassId, ActarisSl7000>> dataClassReaders = new ArrayList<>();
        dataClassReaders.add(class1Readers());
        dataClassReaders.add(class3Readers());
        dataClassReaders.add(class4Readers());
        dataClassReaders.add(class5Readers());
        ReaderRegistry<CollectedRegister, OfflineRegister, DLMSClassId, ActarisSl7000> dataClassReadableRegisters = new ReaderRegistry<>(dataClassReaders);
        return new CollectedRegisterReader<>(new CollectedRegisterReaderFinder<>(readableRegisters, dataClassReadableRegisters), dlmsProtocol, collectedRegisterBuilder);
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> maxDemandRegister1() {
        return new MaxDemandRegister(collectedRegisterBuilder, ObisCode.fromString("0.0.98.133.61.255"));
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> maxDemandRegister2() {
        return new MaxDemandRegister(collectedRegisterBuilder, ObisCode.fromString("0.0.98.133.62.255"));
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> maxDemandRegister3() {
        return new MaxDemandRegister(collectedRegisterBuilder, ObisCode.fromString("0.0.98.133.63.255"));
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> maxDemandRegister4() {
        return new MaxDemandRegister(collectedRegisterBuilder, ObisCode.fromString("0.0.98.133.64.255"));
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> billing() {
        return new BillingReader(collectedRegisterBuilder, new ChannelValueNotMatching(ObisChannel.F, 255));
    }

    private BillingCounterReader billingCounter1() {
        return new BillingCounterReader(collectedRegisterBuilder, new ObisCodeMatcher(ObisCode.fromString("1.0.0.1.0.255")));
    }

    private BillingCounterReader billingCounter2() {
        return new BillingCounterReader(collectedRegisterBuilder, new ObisCodeMatcher(ObisCode.fromString("1.1.0.1.0.255")));
    }

    private MeterFirmwareVersion<ActarisSl7000> meterFirmwareVersion(){
        return new MeterFirmwareVersion<>(collectedRegisterBuilder, ComposedMeterInfo.FIRMWARE_VERSION.getObisCode());
    }

    private MeterSerialNumber<ActarisSl7000> meterSerialNumber(){
        return new MeterSerialNumber<>(collectedRegisterBuilder, ComposedMeterInfo.OBISCODE_SERIAL_NUMBER_REQ);
    }

    private ProgrammingId programmingId() {
        return  new ProgrammingId(collectedRegisterBuilder);
    }

    private DefaultExtendedRegister<ObisCode, ActarisSl7000> numberOfConfigurations(){
        return new DefaultExtendedRegister<>(new ObisCodeMatcher(ObisCode.fromString("0.0.96.1.4.255")), collectedRegisterBuilder, false);
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> dstWorkingMode() {
        return new AttributeReader<>(new ObisCodeMatcher(ObisCode.fromString("0.0.131.0.4.255")),collectedRegisterBuilder, new DSTWorkingModeMapper(), DataAttributes.VALUE.getAttributeNumber());
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> dstSwitchingTime() {
        return new DSTSwitchingTimeReader(collectedRegisterBuilder);
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> batteryExpireDate() {
        return new AttributeReader<>(new ObisCodeMatcher(ObisCode.fromString("0.0.96.6.2.255")), collectedRegisterBuilder, new BatterExpireDate(), DataAttributes.VALUE.getAttributeNumber());
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> batteryVoltage() {
        return new BatteryVoltage(new ObisCodeMatcher(ObisCode.fromString("0.0.96.6.3.255")), collectedRegisterBuilder);
    }
    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> fatalOperationStatus() {
        return new FatalOperationStatus(new ObisCodeMatcher(ObisCode.fromString("0.0.97.97.1.255")), collectedRegisterBuilder);
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> nonFatalOperationStatus() {
        return new NonFatalOperationStatus(new ObisCodeMatcher(ObisCode.fromString("0.0.97.97.2.255")), collectedRegisterBuilder);
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> allDemandsProfile() {
        return new RegisterProfile(collectedRegisterBuilder, ObisCode.fromString("0.0.98.133.5.255"),protocol);
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> allCumulativeProfile() {
        return new RegisterProfile(collectedRegisterBuilder, ObisCode.fromString("0.0.98.133.90.255"),protocol);
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> allEnergyProfile() {
        return new RegisterProfile(collectedRegisterBuilder, ObisCode.fromString("255.255.98.133.1.255"),protocol);
    }

    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> allTotalEnergyProfile() {
        return new RegisterProfile(collectedRegisterBuilder, ObisCode.fromString("255.255.98.133.2.255"),protocol);
    }

    private AttributeReader<DLMSClassId, ActarisSl7000> class1Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.DATA);
        return new AttributeReader<>(matcher, collectedRegisterBuilder, new PerTypeMapper(), DataAttributes.VALUE.getAttributeNumber());
    }

    private DefaultRegister<DLMSClassId, ActarisSl7000> class3Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.REGISTER);
        return new DefaultRegister<>(matcher, collectedRegisterBuilder);
    }

    private DefaultExtendedRegister<DLMSClassId, ActarisSl7000> class4Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.EXTENDED_REGISTER);
        return new DefaultExtendedRegister<>(matcher, collectedRegisterBuilder, false);
    }

    private DefaultDemandRegister<DLMSClassId, ActarisSl7000> class5Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.DEMAND_REGISTER);
        return new DefaultDemandRegister<>(matcher, collectedRegisterBuilder, false);
    }

}
