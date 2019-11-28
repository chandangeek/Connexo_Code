package com.energyict.mdc.engine.offline.model;

import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.util.Date;

public class ValueForObis {

    private ObisCode obisCode;
    private BigDecimal value;
    private Date toDate;
    private Date eventDate;

    public ValueForObis(BigDecimal value, ObisCode obisCode) {
        this.value = value;
        this.obisCode = obisCode;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
}
