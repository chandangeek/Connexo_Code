package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.BitMapCollection;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorConfiguration;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterReadingStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterRelayStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterSensorStatus;

import java.time.Clock;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ConcentratorStatusResponseStructure extends Data<ConcentratorStatusResponseStructure> {

    public static final FunctionCode FUNCTION_CODE = FunctionCode.CONCENTRATOR_STATUS_RESPONSE;
    private static final int NR_OF_METERS = 12;
    private static final int LENGTH_OF_METER_RELAY_STATUSES = 2;
    private static final int LENGTH_OF_METER_SENSOR_STATUSES = 1;
    private static final int LENGTH_OF_METER_READING_STATUSES = 1;

    private DateTime dateTime;
    private ConcentratorConfiguration concentratorConfiguration;
    private BitMapCollection<MeterReadingStatus> meterReadingStatusCollection;
    private BitMapCollection<MeterRelayStatus> meterRelayStatusCollection;
    private BitMapCollection<MeterSensorStatus> meterSensorStatusCollection;

    private final TimeZone timeZone;

    public ConcentratorStatusResponseStructure(Clock clock, TimeZone timeZone) {
        super(FUNCTION_CODE);
        this.timeZone = timeZone;
        this.dateTime = new DateTime(clock, timeZone);
        this.concentratorConfiguration = new ConcentratorConfiguration();
        this.meterReadingStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_READING_STATUSES, NR_OF_METERS, MeterReadingStatus.class);
        this.meterRelayStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_RELAY_STATUSES, NR_OF_METERS, MeterRelayStatus.class);
        this.meterSensorStatusCollection = new BitMapCollection<>(LENGTH_OF_METER_SENSOR_STATUSES, NR_OF_METERS, MeterSensorStatus.class);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                concentratorConfiguration.getBytes(),
                meterReadingStatusCollection.getBytes(),
                meterRelayStatusCollection.getBytes(),
                meterSensorStatusCollection.getBytes()
        );
    }

    @Override
    public ConcentratorStatusResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime.parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.concentratorConfiguration.parse(rawData, ptr);
        ptr += concentratorConfiguration.getLength();

        this.meterReadingStatusCollection.parse(rawData, ptr);
        ptr += meterReadingStatusCollection.getLength();

        this.meterRelayStatusCollection.parse(rawData, ptr);
        ptr += meterRelayStatusCollection.getLength();

        this.meterSensorStatusCollection.parse(rawData, ptr);
        ptr += meterSensorStatusCollection.getLength();

        return this;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public ConcentratorConfiguration getConcentratorConfiguration() {
        return concentratorConfiguration;
    }

    public BitMapCollection<MeterReadingStatus> getMeterReadingStatusCollection() {
        return meterReadingStatusCollection;
    }

    public BitMapCollection<MeterRelayStatus> getMeterRelayStatusCollection() {
        return meterRelayStatusCollection;
    }

    public BitMapCollection<MeterSensorStatus> getMeterSensorStatusCollection() {
        return meterSensorStatusCollection;
    }
}