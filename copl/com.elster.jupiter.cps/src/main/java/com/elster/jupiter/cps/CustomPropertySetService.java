package com.elster.jupiter.cps;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Provides support for adding custom properties to
 * entities that are provided by all Connexo bundles.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (15:37)
 */
@ProviderType
public interface CustomPropertySetService {

    static String COMPONENT_NAME = "CPS";

    /**
     * Registers the specified {@link CustomPropertySet} on this service's whiteboard.
     * This will enables the custom properties on the set's domain class,
     * providing that the domain class is enabled for extension.
     *
     * @param customPropertySet The CustomPropertySet
     */
    void addCustomPropertySet(CustomPropertySet customPropertySet);

    /**
     * Removes the specified {@link CustomPropertySet} from this service's whiteboard.
     * This will disable the custom properties on the set's domain class.
     *
     * @param customPropertySet The CustomPropertySet
     */
    void removeCustomPropertySet(CustomPropertySet customPropertySet);

    /**
     * Finds all {@link RegisteredCustomPropertySet}s.
     *
     * @return The List of RegisteredCustomPropertySet
     */
    List<RegisteredCustomPropertySet> findRegisteredCustomPropertySets();

    /**
     * Finds all {@link RegisteredCustomPropertySet}s that provide
     * custom properties for the specified domain class.
     *
     * @param domainClass The domain class
     * @return The List of RegisteredCustomPropertySet
     */
    List<RegisteredCustomPropertySet> findRegisteredCustomPropertySets(Class domainClass);


}