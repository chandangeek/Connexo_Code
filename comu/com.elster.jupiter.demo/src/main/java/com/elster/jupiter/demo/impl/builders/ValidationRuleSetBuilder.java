/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ValidationRuleSetBuilder extends NamedBuilder<ValidationRuleSet, ValidationRuleSetBuilder> {
    private final ValidationService validationService;

    private String description;
    private List<Consumer<ValidationRuleSetVersion>> postBuilders = new ArrayList<>();

    @Inject
    public ValidationRuleSetBuilder(ValidationService validationService) {
        super(ValidationRuleSetBuilder.class);
        this.validationService = validationService;
    }

    public ValidationRuleSetBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ValidationRuleSetBuilder withVersionPostBuilder(Consumer<ValidationRuleSetVersion> postBuilder) {
        this.postBuilders.add(postBuilder);
        return this;
    }

    @Override
    public Optional<ValidationRuleSet> find() {
        return validationService.getValidationRuleSet(getName());
    }

    @Override
    public ValidationRuleSet create() {
        ValidationRuleSet ruleSet = validationService.createValidationRuleSet(getName(), QualityCodeSystem.MDC, this.description);
        ValidationRuleSetVersion ruleSetVersion = ruleSet.addRuleSetVersion("Demo Default Version", Instant.EPOCH);
        ruleSet.save();
        applyPostBuilders(ruleSet);
        this.postBuilders.forEach(postBuilder -> postBuilder.accept(ruleSetVersion));
        return ruleSet;
    }
}