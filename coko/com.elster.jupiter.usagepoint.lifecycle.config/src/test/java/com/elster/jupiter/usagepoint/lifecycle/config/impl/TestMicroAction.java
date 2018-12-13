/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class TestMicroAction implements MicroAction {
    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        return this.getClass().getName();
    }

    @Override
    public String getCategory() {
        return this.getClass().getPackage().getName();
    }

    @Override
    public String getCategoryName() {
        return this.getClass().getPackage().getName();
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestMicroAction that = (TestMicroAction) o;
        return getKey() != null ? getKey().equals(that.getKey()) : that.getKey() == null;
    }

    public static class Factory implements UsagePointMicroActionFactory {

        @Override
        public Optional<MicroAction> from(String microActionKey) {
            return Optional.of(new TestMicroAction());
        }

        @Override
        public Set<MicroAction> getAllActions() {
            return Collections.singleton(new TestMicroAction());
        }
    }
}
