package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.dynamic.HasDynamicProperties;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.pluggable.PluggableClassUsageProperty;

/**
 * Executes {@link RelationTransaction}s
 * on behalf of a {@link HasDynamicProperties}
 * to save (or update) its {@link PluggableClassUsageProperty properties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-10 (16:26)
 */
public interface RelationTransactionExecutor<T extends HasDynamicProperties> {

    /**
     * Adds the {@link PluggableClassUsageProperty} to the
     * {@link RelationTransaction}.
     *
     * @param shadow The PluggableClassUsageProperty
     */
    public void add (PluggableClassUsageProperty<T> shadow);

    /**
     * Executes the {@link RelationTransaction}.
     */
    public void execute ();

}