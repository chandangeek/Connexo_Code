package com.elster.jupiter.validation.rest;

import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import java.time.Instant;

public class DataValidationTaskMinimalInfo {

    public long id;
    public String name;
    public String logLevelId;
    public PeriodicalExpressionInfo schedule;
    public Instant nextRun;
}