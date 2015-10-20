package com.energyict.mdc.pluggable;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.properties.PropertySpec;

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

    public static String COMPONENTNAME = "CPC";

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
    public PluggableClass newPluggableClass (PluggableClassType type,  String name, String javaClassName);

    public List<PluggableClass> findByTypeAndClassName (PluggableClassType type, String javaClassName);

    public Optional<PluggableClass> findByTypeAndName (PluggableClassType type, String name);

    Optional<PluggableClass> findByTypeAndId (PluggableClassType type, long id);

    Optional<PluggableClass> findAndLockPluggableClassByIdAndVersion(PluggableClassType type, long id, long version);

    public Finder<PluggableClass> findAllByType(PluggableClassType type);

}