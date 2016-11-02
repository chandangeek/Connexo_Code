package com.elster.jupiter.usagepoint.lifecycle.execution.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.execution.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.execution.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.execution.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.execution.impl.actions.MicroActionTranslationKeys;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component(name = "UsagePointLifeCycleExecutionServiceImpl",
        service = {UsagePointLifeCycleService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true)
public class UsagePointLifeCycleExecutionServiceImpl implements UsagePointLifeCycleService, MessageSeedProvider, TranslationKeyProvider {
    @SuppressWarnings("unused") // OSGI
    public UsagePointLifeCycleExecutionServiceImpl() {
    }

    @Override
    public void triggerTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties) {
        this.triggerMicroChecks(usagePoint, transition, transitionTime, properties);
        this.triggerMicroActions(usagePoint, transition, transitionTime, properties);
        this.performTransition(usagePoint, transition, transitionTime, properties);
    }

    private void triggerMicroChecks(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties) {
        transition.getChecks().stream()
                .map(ExecutableMicroCheck.class::cast)
                .forEach(action -> action.execute(usagePoint, properties, transitionTime));
    }

    private void triggerMicroActions(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties) {
        transition.getActions().stream()
                .map(ExecutableMicroAction.class::cast)
                .forEach(action -> action.execute(usagePoint, properties, transitionTime));
    }

    public void performTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties) {
        transition.doTransition(usagePoint.getMRID(), UsagePoint.class.getName(), transitionTime, properties);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(MicroCategoryTranslationKeys.values()));
        keys.addAll(Arrays.asList(MicroActionTranslationKeys.values()));
        keys.addAll(Arrays.asList(MicroCategoryTranslationKeys.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}
