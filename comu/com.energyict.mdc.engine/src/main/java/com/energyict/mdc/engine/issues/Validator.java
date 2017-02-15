/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.issues;

import com.energyict.mdc.issues.Issue;

import java.util.Set;

/**
 * Interface for classes that inspect the validity of an Object of type T.
 *
 * Typically Validator implementation check only one aspect of validity and are then bundled in a CompositeValidator.
 *
 * @param <T> The type of object to be validated
 *
 * @author Tom De Greyt (tgr)
 */
public interface Validator<T> {

    /**
     * Checks whether the specified target object is valid in some sense.
     * If there are {@link Issue}s they can be reported,
     * it is up to the client to interpret how to handle the Issues.
     * In any case if an empty Set is returned, the target is valid.
     *
     * @param target the object to validate
     * @return a Set of Issues, never null.
     */
    public Set<Issue> validate (T target);

}