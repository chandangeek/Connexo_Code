/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import java.time.Instant;
import java.util.logging.Level;

public class DataValidationTaskMinimalInfo {

    public long id;
    public String name;
    public int logLevel = Level.WARNING.intValue();
    public PeriodicalExpressionInfo schedule;
    public Instant nextRun;
}