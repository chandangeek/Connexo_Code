/*
 * FactoryFinder.java
 *
 * Created on 5 december 2003, 16:22
 */

package com.energyict.mdc.common;

import java.util.List;

/**
 * @author Karel
 */
public interface FactoryFinder {

    /**
     * finds the factory for a given type
     *
     * @param name the type name
     * @return a factory
     */
    BusinessObjectFactory findFactory(String name);

    /**
     * finds the factory for a given type
     *
     * @param factoryId the factory Id (as returned by BusinessObjectFactory.getId())
     * @return a factory
     * @see BusinessObjectFactory
     */
    BusinessObjectFactory findFactory(int factoryId);

    /**
     * Get a list of all factories that implement the
     * {@link ProtectableFactory ProtectableFactory} interface.
     *
     * @return A list of {@link ProtectableFactory ProtectableFactories}
     */
    List<ProtectableFactory> getProtectableFactories();

    /**
     * Get a list factories that can be used to create 'Reference Attribute' in a Relation Type
     * in other words: Returns a list with all factories modeling objects that can be referenced by a dynamic reference attribute
     *
     * @return A list of <Code>BusinessObjectFactory</Code>'s that can be used for creating a Reference Attribute Type referring the factory's object type
     */
    List<BusinessObjectFactory> getReferenceableFactories();
}
