/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsagePointMicroActionFactoryImpl implements UsagePointMicroActionFactory {

    private DataModel dataModel;

    private final Map<String, Class<? extends MicroAction>> microActionMapping = new HashMap<>();

    public UsagePointMicroActionFactoryImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        streamMicroActionClasses()
                .forEach(this::addMicroActionMapping);
    }

    private Stream<Class<? extends ExecutableMicroAction>> streamMicroActionClasses() {
        return Stream.of(SetConnectionStateAction.class,
                ResetValidationResultsAction.class,
                RemoveUsagePointFromStaticGroup.class,
                CancelAllServiceCalls.class);
    }

    private void addMicroActionMapping(Class<? extends ExecutableMicroAction> clazz) {
        this.microActionMapping.put(clazz.getSimpleName(), clazz);
    }

    @Override
    public Optional<MicroAction> from(String microActionKey) {
        return Optional.ofNullable(this.microActionMapping.get(microActionKey))
                .map(this.dataModel::getInstance);
    }

    @Override
    public Set<MicroAction> getAllActions() {
        return this.microActionMapping.values().stream()
                .map(this.dataModel::getInstance)
                .collect(Collectors.toSet());
    }
}
