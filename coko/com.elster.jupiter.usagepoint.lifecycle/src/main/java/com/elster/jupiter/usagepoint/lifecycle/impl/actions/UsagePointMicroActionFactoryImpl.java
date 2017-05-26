/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;
import com.elster.jupiter.validation.ValidationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsagePointMicroActionFactoryImpl implements UsagePointMicroActionFactory {

    private DataModel dataModel;
    private Thesaurus thesaurus;
    private PropertySpecService propertySpecService;
    private volatile ValidationService validationService;

    private final Map<String, Class<? extends MicroAction>> microActionMapping = new HashMap<>();

    public UsagePointMicroActionFactoryImpl(DataModel dataModel,
                                            Thesaurus thesaurus,
                                            PropertySpecService propertySpecService,
                                            ValidationService validationService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.validationService = validationService;
        streamMicroActionClasses()
                .forEach(this::addMicroActionMapping);
    }

    private Stream<Class<? extends ExecutableMicroAction>> streamMicroActionClasses() {
        return Stream.of(SetConnectionStateAction.class,
                ResetValidationResultsAction.class);
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
