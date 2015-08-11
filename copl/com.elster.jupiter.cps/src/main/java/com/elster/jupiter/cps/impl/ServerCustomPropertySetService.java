package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;

import java.util.Optional;

/**
 * Adds behavior to the {@link CustomPropertySetService} that is
 * specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (15:30)
 */
public interface ServerCustomPropertySetService extends CustomPropertySetService {

    /**
     * Finds the {@link CustomPropertySet} that registered on the whiteboard
     * with the specified id.
     *
     * @param id The id
     * @return The CustomPropertySet
     * @see CustomPropertySet#getId()
     */
    Optional<CustomPropertySet> findActiveCustomPropertySet(String id);

    /**
     * Cleansup any {@link RegisteredCustomPropertySet}s that are no longer active.
     * Where active is defined as: correctly deployed in the OSGi container
     * and having registered with this whiteboard of this CustomPropertySetService.
     *
     * @see #addCustomPropertySet(CustomPropertySet)
     */
    void cleanupRegisteredButNotActiveCustomPropertySets();

}