package com.elster.protocolimpl.lis100.objects;

import java.util.Date;

/**
 * class to store meterreadings (including date)
 *
 * User: heuckeg
 * Date: 18.03.11
 * Time: 09:25
 */
public class MeterReading {

    protected double reading;
    protected Date date;

    public MeterReading(double reading, Date date) {
        this.reading = reading;
        this.date = date;
    }

    public double getReading() {
        return reading;
    }

    public Date getDate() {
        return date;
    }
}
