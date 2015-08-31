package com.elster.jupiter.metering.cim;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;

public interface Sender {

    void send(CreatedMeterReadings createdMeterReadings, MeterReadings meterReadings);
}
