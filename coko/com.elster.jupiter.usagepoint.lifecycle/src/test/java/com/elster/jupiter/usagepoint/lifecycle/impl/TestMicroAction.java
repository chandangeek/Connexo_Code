/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroActionException;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class TestMicroAction implements ExecutableMicroAction {
    private final BiConsumer<UsagePoint, Instant> onExecute;

    public TestMicroAction(BiConsumer<UsagePoint, Instant> onExecute) {
        this.onExecute = onExecute;
    }

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

    @Override
    public void execute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) throws ExecutableMicroActionException {
        if (onExecute != null) {
            this.onExecute.accept(usagePoint, transitionTime);
        }
    }

    public static class Factory implements UsagePointMicroActionFactory {
        private BiConsumer<UsagePoint, Instant> onExecute;

        @Override
        public Optional<MicroAction> from(String microActionKey) {
            return Optional.of(new TestMicroAction(this.onExecute));
        }

        @Override
        public Set<MicroAction> getAllActions() {
            return Collections.singleton(new TestMicroAction(this.onExecute));
        }

        public void setOnExecute(BiConsumer<UsagePoint, Instant> onExecute) {
            this.onExecute = onExecute;
        }
    }
}
