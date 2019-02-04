/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.orm.JournalEntry;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.util.List;

@ProviderType
public interface MultiplierValue {
    BigDecimal getValue();

    void setValue(BigDecimal value);

    MultiplierType getType();

    List<JournalEntry<MultiplierValue>> getHistory();
}
