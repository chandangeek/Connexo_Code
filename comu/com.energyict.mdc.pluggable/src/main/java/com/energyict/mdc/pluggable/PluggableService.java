/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:48)
 */
@ProviderType
public interface PluggableService {

    String COMPONENTNAME = "CPC";

    /**
     * Returns a new {@link PluggableClass} who's properties can be completed
     * by calling the setProperty method.
     * Note that the name of the PluggableClass is unique in the context of the PluggableClassType,
     * i.e. the combination PluggableClassType/name must be unique.
     *
     * @param type The PluggableClassType
     * @param name The name of the PluggableClass
     * @param javaClassName The name of the java implementation class
     * @return The PluggableClass that is not yet saved
     * @see PluggableClass#setProperty(PropertySpec, Object)
     * @see PluggableClass#save()
     */
    PluggableClass newPluggableClass(PluggableClassType type, String name, String javaClassName);

    List<PluggableClass> findByTypeAndClassName(PluggableClassType type, String javaClassName);

    Optional<PluggableClass> findByTypeAndName(PluggableClassType type, String name);

    Optional<PluggableClass> findByTypeAndId (PluggableClassType type, long id);

    Optional<PluggableClass> findAndLockPluggableClassByIdAndVersion(PluggableClassType type, long id, long version);

    Finder<PluggableClass> findAllByType(PluggableClassType type);

}