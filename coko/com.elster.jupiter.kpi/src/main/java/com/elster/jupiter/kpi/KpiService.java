package com.elster.jupiter.kpi;

import com.google.common.base.Optional;

/**
 * Central Kpi Service. Allows for creating and getting Kpi instances.
 */
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
     * @return an Optional containg the Kpi instance for the given id, if any.
     */
    Optional<Kpi> getKpi(long id);
}
