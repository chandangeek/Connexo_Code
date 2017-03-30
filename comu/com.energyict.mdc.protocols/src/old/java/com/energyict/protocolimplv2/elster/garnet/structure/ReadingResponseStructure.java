/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.BitMapCollection;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorConfiguration;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterConsumption;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ReadingSelector;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterInstallationStatusBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterModel;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterReadingStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterRelayStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterSensorStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterTariffStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 27/05/2014 - 13:57
 */
public class ReadingResponseStructure extends Data<ReadingResponseStructure> {

    private static final int NR_OF_METERS = 6;
    private static final int LENGTH_OF_METER_TARIFF_STATUSES = 1;
    private static final int LENGTH_OF_METER_RELAY_STATUSES = 2;
    private static final int LENGTH_OF_METER_SENSOR_STATUSES = 1;
    private static final int LENGTH_OF_METER_READING_STATUSES = 1;
    private static final int LENGTH_OF_METER_MODELS = 2;
    private static final int LENGTH_OF_METER_INSTALLATION_STATUSES = 2;
    public static final FunctionCode FUNCTION_CODE = FunctionCode.DISCOVER_METERS_RESPONSE;
    private static final int TARIFF_START_INDEX = 1;

    private DateTime dateTime;
    private DateTime readingDateTime;
    private ReadingSelector readingSelector;
    private BitMapCollection<MeterTariffStatus> meterTariffCollection;
    private ConcentratorConfiguration concentratorConfiguration;
    private BitMapCollection<MeterRelayStatus> meterRelayStatusCollection;
    private BitMapCollection<MeterSensorStatus> meterSensorStatusCollection;
    private BitMapCollection<MeterReadingStatus> meterReadingStatusCollection;
    private BitMapCollection<MeterModel> meterModelCollection;
    private BitMapCollection<MeterInstallationStatusBitMaskField> meterInstallationStatusCollection;
    private List<MeterConsumption> consumptionList;

    private final Clock clock;
    private final TimeZone timeZone;

    public ReadingResponseStructure(Clock clock, TimeZone timeZone) {
        super(FUNCTION_CODE);
        this.clock = clock;
        this.timeZone = timeZone;
        this.dateTime = new DateTime(clock, timeZone);
        this.readingDateTime = new DateTime(clock, timeZone);
        this.readingSelector = new ReadingSelector();
        this.meterTariffCollection = new BitMapCollection<>(LENGTH_OF_METER_TARIFF_STATUSES, NR_OF_METERS, TARIFF_START_INDEX, MeterTariffStatus.class);
        this.concentratorConfiguration = new ConcentratorConfiguration();
        this.meterRelayStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_RELAY_STATUSES, NR_OF_METERS, MeterRelayStatus.class);
        this.meterSensorStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_SENSOR_STATUSES, NR_OF_METERS, MeterSensorStatus.class);
        this.meterReadingStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_READING_STATUSES, NR_OF_METERS, MeterReadingStatus.class);
        this.meterModelCollection = new BitMapCollection<>(LENGTH_OF_METER_MODELS, NR_OF_METERS, MeterModel.class);
        this.meterInstallationStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_INSTALLATION_STATUSES, NR_OF_METERS, MeterInstallationStatusBitMaskField.class);
        this.consumptionList = new ArrayList<>(NR_OF_METERS);
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream consumptionByteStream = new ByteArrayOutputStream(NR_OF_METERS * MeterConsumption.LENGTH);
        Iterator<MeterConsumption> it = consumptionList.iterator();
        try {
            while (it.hasNext()) {
                MeterConsumption consumption = it.next();
                consumptionByteStream.write(consumption.getBytes());
            }
        } catch (IOException e) {
            consumptionByteStream = new ByteArrayOutputStream(); // Clear the stream
        }
        byte[] readingSelectorBytes = new byte[]{(byte) (readingSelector.getBytes()[0] | meterTariffCollection.getBytes()[0])};

        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                readingSelectorBytes,
                readingDateTime.getBytes(),
                concentratorConfiguration.getBytes(),
                meterRelayStatusCollection.getBytes(),
                meterSensorStatusCollection.getBytes(),
                meterReadingStatusCollection.getBytes(),
                meterModelCollection.getBytes(),
                meterInstallationStatusCollection.getBytes(),
                consumptionByteStream.toByteArray()
        );
    }

    @Override
    public ReadingResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime = new DateTime(this.clock, getTimeZone()).parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.readingSelector = new ReadingSelector().parse(rawData, ptr);
        this.meterTariffCollection.parse(rawData, ptr);
        ptr += readingSelector.getLength();

        this.readingDateTime = new DateTime(this.clock, getTimeZone()).parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.concentratorConfiguration.parse(rawData, ptr);
        ptr += concentratorConfiguration.getLength();

        this.meterRelayStatusCollection.parse(rawData, ptr);
        ptr += meterRelayStatusCollection.getLength();

        this.meterSensorStatusCollection.parse(rawData, ptr);
        ptr += meterSensorStatusCollection.getLength();

        this.meterReadingStatusCollection.parse(rawData, ptr);
        ptr += meterReadingStatusCollection.getLength();

        this.meterModelCollection.parse(rawData, ptr);
        ptr += meterModelCollection.getLength();

        this.meterInstallationStatusCollection.parse(rawData, ptr);
        ptr += meterInstallationStatusCollection.getLength();

        this.consumptionList = new ArrayList<>(NR_OF_METERS);
        for (int i = 0; i < NR_OF_METERS; i++) {
            MeterConsumption meterConsumption = new MeterConsumption().parse(rawData, ptr);
            consumptionList.add(meterConsumption);
            ptr += meterConsumption.getLength();
        }
        return this;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public DateTime getReadingDateTime() {
        return readingDateTime;
    }

    public ReadingSelector getReadingSelector() {
        return readingSelector;
    }

    public ConcentratorConfiguration getConcentratorConfiguration() {
        return concentratorConfiguration;
    }

    public BitMapCollection<MeterTariffStatus> getMeterTariffCollection() {
        return meterTariffCollection;
    }

    public MeterTariffStatus getMeterTariff(String serialNumber) {
        Integer index = getMeterIndexOf(serialNumber);
        if (index != null) {
            return (getMeterTariffCollection().getAllBitMasks().size() >= index) ?
                    getMeterTariffCollection().getAllBitMasks().get(index)
                    : null;
        }
        return null;
    }

    public BitMapCollection<MeterRelayStatus> getMeterRelayStatusCollection() {
        return meterRelayStatusCollection;
    }

    public MeterRelayStatus getMeterRelayStatus(String serialNumber) {
        Integer index = getMeterIndexOf(serialNumber);
        if (index != null) {
            return (getMeterRelayStatusCollection().getAllBitMasks().size() >= index) ?
                    getMeterRelayStatusCollection().getAllBitMasks().get(index)
                    : null;
        }
        return null;
    }

    public BitMapCollection<MeterSensorStatus> getMeterSensorStatusCollection() {
        return meterSensorStatusCollection;
    }

    public MeterSensorStatus getMeterSensorStatus(String serialNumber) {
        Integer index = getMeterIndexOf(serialNumber);
        if (index != null) {
            return (getMeterSensorStatusCollection().getAllBitMasks().size() >= index) ?
                    getMeterSensorStatusCollection().getAllBitMasks().get(index)
                    : null;
        }
        return null;
    }

    public BitMapCollection<MeterReadingStatus> getMeterReadingStatusCollection() {
        return meterReadingStatusCollection;
    }

    public MeterReadingStatus getMeterReadingStatus(String serialNumber) {
        Integer index = getMeterIndexOf(serialNumber);
        if (index != null) {
            return (getMeterReadingStatusCollection().getAllBitMasks().size() >= index) ?
                    getMeterReadingStatusCollection().getAllBitMasks().get(index)
                    : null;
        }
        return null;
    }

    public BitMapCollection<MeterModel> getMeterModelCollection() {
        return meterModelCollection;
    }

    public MeterModel getMeterModel(String serialNumber) {
        Integer index = getMeterIndexOf(serialNumber);
        if (index != null) {
            return (getMeterModelCollection().getAllBitMasks().size() >= index) ?
                    getMeterModelCollection().getAllBitMasks().get(index)
                    : null;
        }
        return null;
    }

    public BitMapCollection<MeterInstallationStatusBitMaskField> getMeterInstallationStatusCollection() {
        return meterInstallationStatusCollection;
    }

    public MeterInstallationStatusBitMaskField getMeterInstallationStatus(String serialNumber) {
        Integer index = getMeterIndexOf(serialNumber);
        if (index != null) {
            return (getMeterInstallationStatusCollection().getAllBitMasks().size() >= index) ?
                    getMeterInstallationStatusCollection().getAllBitMasks().get(index)
                    : null;
        }
        return null;
    }

    public List<MeterConsumption> getConsumptionList() {
        return consumptionList;
    }

    public MeterConsumption getConsumption(String serialNumber) {
        Integer index = getMeterIndexOf(serialNumber);
        if (index != null) {
            return (getConsumptionList().size() >= index) ?
                    getConsumptionList().get(index)
                    : null;
        }
        return null;
    }

    private Integer getMeterIndexOf(String serialNumber) {
        int index = 0;
        for (MeterConsumption consumption : consumptionList) {
            if (consumption.getSerialNumber().getSerialNumber().equals(serialNumber)) {
                return index;
            } else {
                index++;
            }
        }
        return null;
    }

    public boolean containsMeter(String serialNumber) {
        return getMeterIndexOf(serialNumber) != null;
    }
}