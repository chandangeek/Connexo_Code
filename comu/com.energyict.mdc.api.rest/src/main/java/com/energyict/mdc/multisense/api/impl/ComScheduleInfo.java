package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.time.TemporalExpression;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;

import java.time.Instant;

public class ComScheduleInfo extends LinkInfo {

    public long id;
    public String name;
    public TemporalExpression temporalExpression;
    @JsonDeserialize(using = NullifyingDeserializer.class)
    public Instant plannedDate;
    public boolean isInUse;
    public LinkInfo comTask;
    public Instant startDate;
    public String mRID;

}