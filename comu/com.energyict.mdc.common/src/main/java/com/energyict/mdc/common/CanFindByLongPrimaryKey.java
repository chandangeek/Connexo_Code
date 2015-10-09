package com.energyict.mdc.common;

import com.elster.jupiter.util.HasId;
import java.util.Optional;

/**
 * Models the behavior of a component that is capable of finding objects
 * from the primary key that is known to be of type long.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-19 (17:21)
 */
public interface CanFindByLongPrimaryKey<T extends HasId> {

    /**
     * Returns the key that will be used to identifiy
     * this component to a ReferencePropertySpecFinderProvider.
     *
     * @return The registration key
     */
    public FactoryIds factoryId();

    /**
     * Returns the domain of objects that are returned by the this component.
     *
     * @return The domain
     */
    public Class<T> valueDomain ();

    public Optional<T> findByPrimaryKey (long id);

}