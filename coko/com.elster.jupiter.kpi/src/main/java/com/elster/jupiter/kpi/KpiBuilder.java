/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.TimeZone;

/**
 * Builder for Kpi and its KpiMembers
 */
@ProviderType
public interface KpiBuilder {

    /**
     * Finalizes the build process and returns the resulting Kpi, and its members.
     * @return the resulting Kpi
     */
    Kpi create();

    /**
     * @param name the name to assign to the Kpi
     * @return the builder to chain on
     */
    KpiBuilder named(String name);

    /**
     * Starts building a new member for the Kpi under construction
     * @return the KpiMemberBuilder that will allow specifying member details.
     */
    KpiMemberBuilder member();

    /**
     * @param zone the TimeZone the Kpi will work against. If not specified, a Kpi will work against UTC by defautlt.
     * @return the builder to chain on
     */
    KpiBuilder timeZone(TimeZone zone);

    /**
     * @param zone the TimeZone the Kpi will work against. If not specified, a Kpi will work against UTC by defautlt.
     * @return the builder to chain on
     */
    KpiBuilder timeZone(ZoneId zone);

    /**
     * @param interval the IntervalLength this Kpi will need to register results
     * @return the builder to chain on
     */
    KpiBuilder interval(TemporalAmount interval);

    /**
     * Intermediate builder for KpiMembers.
     */
    interface KpiMemberBuilder {

        /**
         * @param target the static target for this KpiMember, this will overrule previous calls to this method or to withDynamicTarget()
         * @return the builder to chain on
         */
        KpiMemberBuilder withTargetSetAt(BigDecimal target);

        /**
         * Sets this KpiMember to have a dynamic target. This will overrule previous calls to this method or to withTargetSetAt()
         * @return the builder to chain on
         */
        KpiMemberBuilder withDynamicTarget();

        /**
         * Indicates the target is a minimum value, i.e. scores are expected to be equal to or higher than the target.
         * @return the builder to chain on
         */
        KpiMemberBuilder asMinimum();

        /**
         * Indicates the target is a maximum value, i.e. scores are expected to be equal to or lower than the target.
         * @return the builder to chain on
         */
        KpiMemberBuilder asMaximum();

        /**
         * @param name the name for this KpiMember
         * @return the builder to chain on
         */
        KpiMemberBuilder named(String name);

        /**
         * Finalizes specifying the KpiMember under construction.
         * @return the builder for the parent Kpi to chain on.
         */
        KpiBuilder add();
    }
}
