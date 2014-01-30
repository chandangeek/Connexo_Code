package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings_.MeterReadings;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;

public interface Sender {

    void send(CreatedMeterReadings createdMeterReadings, MeterReadings meterReadings);
}
