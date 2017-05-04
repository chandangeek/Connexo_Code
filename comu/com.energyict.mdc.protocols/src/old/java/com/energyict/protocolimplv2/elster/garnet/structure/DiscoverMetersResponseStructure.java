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
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterSerialNumber;
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
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 27/05/2014 - 13:57
 */
public class DiscoverMetersResponseStructure extends Data<DiscoverMetersResponseStructure> {

    private static final int NR_OF_METERS = 12;
    private static final int LENGTH_OF_METER_MODELS = 3;
    private static final int LENGTH_OF_METER_READING_STATUSES = 2;
    private static final int LENGTH_OF_METER_RELAY_STATUSES = 3;
    private static final int LENGTH_OF_METER_SENSOR_STATUSES = 2;
    private static final int LENGTH_OF_METER_INSTALLATION_STATUSES = 3;
    private static final int LENGTH_OF_METER_TARIFF_STATUSES = 2;
    public static final FunctionCode FUNCTION_CODE = FunctionCode.DISCOVER_METERS_RESPONSE;

    private DateTime dateTime;
    private BitMapCollection<MeterModel> meterModelCollection;
    private BitMapCollection<MeterReadingStatus> meterReadingStatusCollection;
    private ConcentratorConfiguration concentratorConfiguration;
    private BitMapCollection<MeterRelayStatus> meterRelayStatusCollection;
    private BitMapCollection<MeterSensorStatus> meterSensorStatusCollection;
    private BitMapCollection<MeterInstallationStatusBitMaskField> meterInstallationStatusCollection;
    private BitMapCollection<MeterTariffStatus> meterTariffStatusCollection;
    private List<MeterSerialNumber> meterSerialNumberCollection;

    private final Clock clock;
    private final TimeZone timeZone;

    public DiscoverMetersResponseStructure(Clock clock, TimeZone timeZone) {
        super(FUNCTION_CODE);
        this.clock = clock;
        this.timeZone = timeZone;
        this.dateTime = new DateTime(clock, timeZone);
        this.meterModelCollection = new BitMapCollection<>(LENGTH_OF_METER_MODELS, NR_OF_METERS, MeterModel.class);
        this.meterReadingStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_READING_STATUSES, NR_OF_METERS, MeterReadingStatus.class);
        this.concentratorConfiguration = new ConcentratorConfiguration();
        this.meterRelayStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_RELAY_STATUSES, NR_OF_METERS, MeterRelayStatus.class);
        this.meterSensorStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_SENSOR_STATUSES, NR_OF_METERS, MeterSensorStatus.class);
        this.meterInstallationStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_INSTALLATION_STATUSES, NR_OF_METERS, MeterInstallationStatusBitMaskField.class);
        this.meterTariffStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_TARIFF_STATUSES, NR_OF_METERS, MeterTariffStatus.class);
        this.meterSerialNumberCollection = new ArrayList<>(NR_OF_METERS);
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream meterSerialsByteSteam = new ByteArrayOutputStream(NR_OF_METERS * MeterSerialNumber.LENGTH);
        try {
            for (int i = 0; i < NR_OF_METERS; i++) {
                meterSerialsByteSteam.write(meterSerialNumberCollection.get(i).getBytes());
            }
        } catch (IOException e) {
            meterSerialsByteSteam = new ByteArrayOutputStream(); // clear the stream;
        }

        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                meterModelCollection.getBytes(),
                meterReadingStatusCollection.getBytes(),
                concentratorConfiguration.getBytes(),
                meterRelayStatusCollection.getBytes(),
                meterSensorStatusCollection.getBytes(),
                meterInstallationStatusCollection.getBytes(),
                meterTariffStatusCollection.getBytes(),
                meterSerialsByteSteam.toByteArray()
        );
    }

    @Override
    public DiscoverMetersResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime = new DateTime(this.clock, getTimeZone()).parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.meterModelCollection.parse(rawData, ptr);
        ptr += meterModelCollection.getLength();

        this.meterReadingStatusCollection.parse(rawData, ptr);
        ptr += meterReadingStatusCollection.getLength();

        this.concentratorConfiguration.parse(rawData, ptr);
        ptr += concentratorConfiguration.getLength();

        this.meterRelayStatusCollection.parse(rawData, ptr);
        ptr += meterRelayStatusCollection.getLength();

        this.meterSensorStatusCollection.parse(rawData, ptr);
        ptr += meterSensorStatusCollection.getLength();

        this.meterInstallationStatusCollection.parse(rawData, ptr);
        ptr += meterInstallationStatusCollection.getLength();

        this.meterTariffStatusCollection.parse(rawData, ptr);
        ptr += meterTariffStatusCollection.getLength();

        this.meterSerialNumberCollection = new ArrayList<>(NR_OF_METERS);
        for (int i = 0; i < NR_OF_METERS; i++) {
            MeterSerialNumber meterSerialNumber = new MeterSerialNumber().parse(rawData, ptr);
            meterSerialNumberCollection.add(meterSerialNumber);
            ptr += meterSerialNumber.getLength();
        }
        return this;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public BitMapCollection<MeterModel> getMeterModelCollection() {
        return meterModelCollection;
    }

    public BitMapCollection<MeterReadingStatus> getMeterReadingStatusCollection() {
        return meterReadingStatusCollection;
    }

    public ConcentratorConfiguration getConcentratorConfiguration() {
        return concentratorConfiguration;
    }

    public BitMapCollection<MeterRelayStatus> getMeterRelayStatusCollection() {
        return meterRelayStatusCollection;
    }

    public BitMapCollection<MeterSensorStatus> getMeterSensorStatusCollection() {
        return meterSensorStatusCollection;
    }

    public BitMapCollection<MeterInstallationStatusBitMaskField> getMeterInstallationStatusCollection() {
        return meterInstallationStatusCollection;
    }

    public BitMapCollection<MeterTariffStatus> getMeterTariffStatusCollection() {
        return meterTariffStatusCollection;
    }

    public List<MeterSerialNumber> getMeterSerialNumberCollection() {
        return meterSerialNumberCollection;
    }
}