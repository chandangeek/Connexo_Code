package com.energyict.protocolimplv2.umi.ei4.profile;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.ArrayList;
import java.util.List;

/**
 * Interval Data Table
 **/
public class EI4UmiProfileTable extends LittleEndianData {
    public static int SIZE = 32;
    private List<EI4UmiMeterReading> readings = new ArrayList<>();

    /**
     * Constructor for testing purposes.
     */
    public EI4UmiProfileTable(List<EI4UmiMeterReading> events) {
        super(events.size() * EI4UmiMeterReading.SIZE);
        this.readings = events;
        for (EI4UmiMeterReading event : events) {
            getRawBuffer().put(event.getRaw());
        }
    }

    public EI4UmiProfileTable(byte[] raw) {
        super(raw, EI4UmiMeterReading.SIZE, true);
        readings = new ArrayList<>();
        byte[] rawEvent = new byte[EI4UmiMeterReading.SIZE];

        while (getRawBuffer().position() < getRawBuffer().capacity()) {
            getRawBuffer().get(rawEvent);
            readings.add(new EI4UmiMeterReading(rawEvent));
        }
    }

    public List<EI4UmiMeterReading> getReadings() {
        return readings;
    }

    public void add(EI4UmiMeterReading reading) {
        readings.add(reading);
    }
}
