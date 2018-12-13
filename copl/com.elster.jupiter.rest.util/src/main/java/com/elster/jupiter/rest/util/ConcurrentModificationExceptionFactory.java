/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

public class ConcurrentModificationExceptionFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ConcurrentModificationExceptionFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ConcurrentModificationExceptionBuilder conflict() {
        return new ConcurrentModificationExceptionBuilder(this.thesaurus);
    }

    /**
     * Use this method when you want to leave an error message and detailed description to context
     * @param objectName name of object, will be used as a part of error message
     * @return
     */
    public ConcurrentModificationExceptionBuilder contextDependentConflictOn(String objectName) {
        return new ConcurrentModificationExceptionBuilder(this.thesaurus, objectName);
    }
}
