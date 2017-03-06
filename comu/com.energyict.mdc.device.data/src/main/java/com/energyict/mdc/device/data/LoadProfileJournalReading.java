/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Created by bvn on 8/1/14.
 */
@ProviderType
public interface LoadProfileJournalReading extends LoadProfileReading {

    Instant getJournalTime();

    String getUserName();

    boolean getActive();

    long getVersion();
}