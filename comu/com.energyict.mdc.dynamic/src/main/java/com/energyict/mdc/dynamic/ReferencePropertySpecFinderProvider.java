package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;

import java.util.List;

/**
 * Models the behavior of an OSGi component that is capable of providing
 * the {@link CanFindByLongPrimaryKey} for {@link PropertySpec reference properties}.
 * Every OSGi bundle that contains such component(s) will register these
 * component(s) automatically with the {@link PropertySpecService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-15 (17:01)
 */
public interface ReferencePropertySpecFinderProvider {

    /**
     * Returns all the finders that are known to this component.
     *
     * @return The knonwn finders
     */
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders ();

}