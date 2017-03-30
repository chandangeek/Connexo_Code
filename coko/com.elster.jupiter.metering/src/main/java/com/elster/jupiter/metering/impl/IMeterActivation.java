/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeterActivation;

import java.time.Instant;

public interface IMeterActivation extends MeterActivation {

    void doEndAt(Instant end);
}
