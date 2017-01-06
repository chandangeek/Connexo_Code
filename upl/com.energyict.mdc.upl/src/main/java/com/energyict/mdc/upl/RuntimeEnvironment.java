package com.energyict.mdc.upl;

import java.util.Optional;

/**
 * Models the environment that provides the runtime parameters to this platform.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-06 (14:46)
 */
public interface RuntimeEnvironment {
    /**
     * Gets the value of the runtime property with the specified name.
     *
     * @param name The name of the runtime property
     * @return The value or <code>Optional.empty()</code> if that property does not exist
     */
    Optional<String> getProperty(String name);

    /**
     * Gets the value of the runtime property with the specified name
     * or the specified default value if no value was specified in the runtime.
     *
     * @param name The name of the runtime property
     * @return The value or the default value if that property does not exist
     */
    String getProperty(String name, String defaultValue);
}