/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.check;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.cim.EndDeviceEventType;
import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.factory.AbstractMerlinFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.MerlinCollectedDataFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.CellInfoMapping;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.RegisterMapping;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class CheckFrameDataFactory implements MerlinCollectedDataFactory {

    private final CheckFrameParser parser;
    private final InboundContext inboundContext;

    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private CollectedRegisterList collectedRegisterList;
    private List<CollectedData> collectedDataList;
    private CollectedLoadProfile dailyLoadProfile;
    private List<CollectedLogBook> collectedLogBooks;
    private CollectedDataFactory factory;

    private CollectedLogBook eventLog;

    public CheckFrameDataFactory(CheckFrameParser checkFrameParser, InboundContext inboundContext) {
        this.parser = checkFrameParser;
        this.inboundContext = inboundContext;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        if (this.deviceIdentifier == null) {
            this.deviceIdentifier = new DeviceIdentifierBySerialNumber(parser.getDeviceId());
        }
        return this.deviceIdentifier;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        if (this.collectedDataList != null) {
            return collectedDataList;
        }

        createContainers();

        collectedRegisterList.addCollectedRegister(
                createNumericalRegister(RegisterMapping.INSTANTANEOUS_VOLUME.getObisCode(), parser.getMeterIndex(), Unit.get("m3", -3)));

        collectedRegisterList.addCollectedRegister(
                createNumericalRegister(CellInfoMapping.CELL_ID.getObisCode(), parser.getCellId(), Unit.getUndefined()));

        collectedRegisterList.addCollectedRegister(
                createNumericalRegister(CellInfoMapping.SIGNAL_STRENGTH.getObisCode(), parser.getSignalStrength(), Unit.getUndefined()));

        collectedRegisterList.addCollectedRegister(
                createNumericalRegister(CellInfoMapping.SIGNAL_QUALITY.getObisCode(), parser.getSignalQuality(), Unit.getUndefined()));

        collectedRegisterList.addCollectedRegister(
                createNumericalRegister(CellInfoMapping.TRANSMISSION_POWER.getObisCode(), parser.getTxPower(), Unit.getUndefined()));

        collectedRegisterList.addCollectedRegister(
                createTextRegister(CellInfoMapping.PAIRED_METER_ID.getObisCode(), parser.getMechanicalId()));

        List<MeterProtocolEvent> allEvents = new ArrayList<>();

        allEvents.add(createEvent(MeterEvent.HEART_BEAT, parser.toString()));

        eventLog.addCollectedMeterEvents(allEvents);

        collectedDataList.add(eventLog);
        collectedDataList.add(collectedRegisterList);

        return collectedDataList;
    }

    private void createContainers() {
        collectedDataList = new ArrayList<>();

        collectedLogBooks = new ArrayList<>();
        factory = inboundContext.getInboundDiscoveryContext().getCollectedDataFactory();
        collectedRegisterList = factory.createCollectedRegisterList(getDeviceIdentifier());


        LogBookIdentifierByDeviceAndObisCode logBookIdentifier = new LogBookIdentifierByDeviceAndObisCode(getDeviceIdentifier(), ObisCode.fromString(AbstractMerlinFactory.STANDARD_LOGBOOK));
        eventLog = factory.createCollectedLogBook(logBookIdentifier);


    }


    private CollectedRegister createNumericalRegister(ObisCode obisCode, long cellId, Unit unit) {
        RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier());

        BigDecimal valueNumeric = BigDecimal.valueOf(parser.getMeterIndex());

        Quantity quantity = new Quantity(valueNumeric, unit);

        CollectedRegister register = factory.createDefaultCollectedRegister(registerIdentifier);
        register.setCollectedData(quantity);
        register.setReadTime(Date.from(parser.getDateTimeUtc()));

        return register;
    }

    private CollectedRegister createTextRegister(ObisCode obisCode, String text) {
        RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier());
        CollectedRegister register = factory.createDefaultCollectedRegister(registerIdentifier);
        register.setCollectedData(text);
        register.setReadTime(Date.from(parser.getDateTimeUtc()));

        return register;
    }


    protected MeterProtocolEvent createEvent(int eiCode, String message) {
        java.util.Date eventTime = Date.from(parser.getDateTimeUtc());
        int protocolCode = 0;
        EndDeviceEventType eventType = EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(eiCode);
        int meterEventLogId = 0;
        int deviceEventId = 0;

        return new MeterProtocolEvent(eventTime, eiCode, protocolCode, eventType, message, meterEventLogId, deviceEventId);

    }
}
