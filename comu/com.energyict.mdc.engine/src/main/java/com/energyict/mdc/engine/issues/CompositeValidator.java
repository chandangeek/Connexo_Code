/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.issues;

import com.energyict.mdc.issues.Issue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This Validator implementation is a composite of several Validators.
 * It runs each validator it is composed of in order.
 * Depending on the collectAll setting it either runs all Validators (collectAll = true, this is default behaviour),
 * or stops as soon as one Validator returns Issues.
 *
 * @author Tom De Greyt (tgr)
 */
public class CompositeValidator<T> implements Validator<T> {
    private List<Validator<T>> validators = new ArrayList<>();

    private boolean collectAll = true;

    public CompositeValidator(Validator<T>... validators) {
        Collections.addAll(this.validators, validators);
    }

    public CompositeValidator(List<Validator<T>> validators) {
        this.validators.addAll(validators);
    }

    public boolean isCollectAll() {
        return collectAll;
    }

    public void setCollectAll(boolean collectAll) {
        this.collectAll = collectAll;
    }

    public Set<Issue> validate(T target) {
        Set<Issue> issues = new LinkedHashSet<>();
        for (Validator<T> validator : validators) {
            issues.addAll(validator.validate(target));
            if (!collectAll && !issues.isEmpty()) {
                return issues;
            }
        }
        return issues;
    }

}