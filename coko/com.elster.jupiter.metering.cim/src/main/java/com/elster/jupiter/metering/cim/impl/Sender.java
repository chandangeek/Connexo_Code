package com.elster.jupiter.metering.cim.impl;

public interface Sender {

    void send(CreatedMeterReadings createdMeterReadings, MeterReadings meterReadings);
}
