/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

/**
 * Central Kpi Service. Allows for creating and getting Kpi instances.
 */
@ProviderType
public interface KpiService {

    /**
     * Component name for this bundle.
     */
    String COMPONENT_NAME = "KPI";

    /**
     * @return a KpiBuilder to build a new Kpi instance.
     */
    KpiBuilder newKpi();

    /**
     * @param id
     * @return an Optional containing the Kpi instance for the given id, if any.
     */
    Optional<Kpi> getKpi(long id);
}
