/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroCheckFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UsagePointMicroCheckFactoryImpl implements UsagePointMicroCheckFactory {

    private DataModel dataModel;

    private final Map<String, Class<? extends MicroCheck>> microCheckMapping = new HashMap<>();

    public UsagePointMicroCheckFactoryImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        addMicroCheckMappings();
    }

    private void addMicroCheckMappings() {
        addMicroCheckMapping(MetrologyConfigurationIsDefinedCheck.class);
        addMicroCheckMapping(MeterRolesAreSpecifiedCheck.class);
    }

    private void addMicroCheckMapping(Class<? extends ExecutableMicroCheck> clazz) {
        this.microCheckMapping.put(clazz.getSimpleName(), clazz);
    }

    @Override
    public Optional<MicroCheck> from(String microCheckKey) {
        return Optional.ofNullable(this.microCheckMapping.get(microCheckKey))
                .map(this.dataModel::getInstance);
    }

    @Override
    public Set<MicroCheck> getAllChecks() {
        return this.microCheckMapping.values().stream()
                .map(this.dataModel::getInstance)
                .collect(Collectors.toSet());
    }
}
