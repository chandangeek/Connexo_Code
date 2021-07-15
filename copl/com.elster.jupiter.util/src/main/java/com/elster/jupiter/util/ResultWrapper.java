/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.Set;
import java.util.TreeSet;

public class ResultWrapper<T> {

    private Set<T> failedObjects = new TreeSet<>();

    public void addFailedObject(T failedObject) {
        this.failedObjects.add(failedObject);
    }

    public Set<T> getFailedObjects() {
        return this.failedObjects;
    }

    public ResultWrapper<T> adopt(ResultWrapper<T> result) {
        this.failedObjects.addAll(result.getFailedObjects());
        return this;
    }

}
