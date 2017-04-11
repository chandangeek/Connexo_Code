/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

/**
 * Adds behavior to {@link MetrologyContract} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-11 (13:24)
 */
public interface ServerMetrologyContract extends MetrologyContract {

    ReadingTypeDeliverable addDeliverable(String name, DeliverableType deliverableType, ReadingType readingType, Formula formula);

    void delete();

}