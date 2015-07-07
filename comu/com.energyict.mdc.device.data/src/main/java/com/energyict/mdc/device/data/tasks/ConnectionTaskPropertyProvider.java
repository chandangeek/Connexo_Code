package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.TypedProperties;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Provides {@link ConnectionTaskProperty properties} of a {@link ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-09 (17:01)
 */
@ProviderType
public interface ConnectionTaskPropertyProvider {

    public List<ConnectionTaskProperty> getProperties ();

    /**
     * Provides the current properties ({@link #getProperties()} in the TypedProperties format.
     *
     * @return the TypedProperties
     */
    public TypedProperties getTypedProperties ();

}
