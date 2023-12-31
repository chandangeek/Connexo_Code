/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;

/**
 * Created by bvn on 8/1/14.
 */
@ConsumerType
public interface LoadProfileJournalReading extends LoadProfileReading {

    Instant getJournalTime();

    String getUserName();

    boolean getActive();

    long getVersion();
}