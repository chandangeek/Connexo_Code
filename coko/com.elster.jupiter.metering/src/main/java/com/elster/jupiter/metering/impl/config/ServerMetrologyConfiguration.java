/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.MetrologyConfiguration;

/**
 * Adds behavior to {@link MetrologyConfiguration} that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-30 (10:00)
 */
interface ServerMetrologyConfiguration extends MetrologyConfiguration {
    void contractUpdated(MetrologyContractImpl contract);
}