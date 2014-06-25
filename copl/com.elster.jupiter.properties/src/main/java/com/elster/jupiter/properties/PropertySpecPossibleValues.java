package com.elster.jupiter.properties;

import java.util.List;

/**
 * Models the possible values of a {@link PropertySpec}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-19 (15:41)
 */
public interface PropertySpecPossibleValues<T> {

    /**
     * Gets the possible values.
     *
     * @return The possible values
     */
    public List<? super T> getAllValues ();

    /**
     * Returns <code>true</code> if the possible values are an
     * exhaustive list, i.e. if no other values will be accepted.
     * Therefore, if this returns <code>false</code> then
     * the list of values returned by {@link #getAllValues()}
     * can be regarded as a example list of values.
     *
     * @return A flag that indicates if only values returned by the getAllValues method are accepted
     */
    public boolean isExhaustive ();

    /**
     * Gets the default value that will be used for the PropertySpec.
     *
     * @return The default value
     */
    public T getDefault ();

}