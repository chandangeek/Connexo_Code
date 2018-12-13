/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;

/**
 * Updater for Kpi and its KpiMembers
 */
@ProviderType
public interface KpiUpdater {

    /**
     * Starts building a new member for the Kpi
     *
     * @return the KpiMemberBuilder that will allow specifying member details.
     */
    KpiMemberBuilder member();

    /**
     * Finalizes the update process and returns the resulting Kpi, and its members.
     *
     * @return the resulting Kpi
     */
    Kpi update();

    /**
     * Intermediate builder for KpiMembers.
     * Copied from {@link KpiBuilder.KpiMemberBuilder} because the refactoring will break a backward compatibility
     */
    @ProviderType
    interface KpiMemberBuilder {

        /**
         * @param target the static target for this KpiMember, this will overrule previous calls to this method or to withDynamicTarget()
         * @return the builder to chain on
         */
        KpiMemberBuilder withTargetSetAt(BigDecimal target);

        /**
         * Sets this KpiMember to have a dynamic target. This will overrule previous calls to this method or to withTargetSetAt()
         *
         * @return the builder to chain on
         */
        KpiMemberBuilder withDynamicTarget();

        /**
         * Indicates the target is a minimum value, i.e. scores are expected to be equal to or higher than the target.
         *
         * @return the builder to chain on
         */
        KpiMemberBuilder asMinimum();

        /**
         * Indicates the target is a maximum value, i.e. scores are expected to be equal to or lower than the target.
         *
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
         *
         * @return the updater for the parent Kpi to chain on.
         */
        KpiUpdater add();
    }
}
