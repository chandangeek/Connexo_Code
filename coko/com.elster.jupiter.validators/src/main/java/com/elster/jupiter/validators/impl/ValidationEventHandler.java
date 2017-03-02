/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.properties.ReadingQualityPropertyValue;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationScope;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.validators.impl.ValidationEventHandler", service = Subscriber.class, immediate = true)
public class ValidationEventHandler extends EventHandler<LocalEvent> {
    private static final String CREATED_TOPIC = com.elster.jupiter.metering.EventType.READINGS_CREATED.topic();
    private static final String VALIDATED_TOPIC = com.elster.jupiter.validation.EventType.VALIDATION_PERFORMED.topic();
    private static final String INVALIDATED_TOPIC = com.elster.jupiter.validation.EventType.VALIDATION_RESET.topic();
    private static final List<String> CHECKED_QUALITY_CODE_ENDINGS = Arrays.asList(
            Joiner.on('.').join("", QualityCodeCategory.VALIDATION.ordinal(), ReadingQualityPropertyValue.WILDCARD),
            Joiner.on('.').join("", QualityCodeIndex.SUSPECT.category().ordinal(), QualityCodeIndex.SUSPECT.index()),
            Joiner.on('.').join("", QualityCodeIndex.KNOWNMISSINGREAD.category().ordinal(), QualityCodeIndex.KNOWNMISSINGREAD.index())
    );

    private volatile ValidationService validationService;

    public ValidationEventHandler() {
        super(LocalEvent.class);
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        Map<ChannelsContainer, Map<Channel, Range<Instant>>> scopePerChannelPerChannelsContainer =
                determineScopePerChannelPerChannelsContainer(event);
        if (!scopePerChannelPerChannelsContainer.isEmpty()) {
            QualityCodeSystem underlyingSystem = getUnderlyingSystem(scopePerChannelPerChannelsContainer.keySet());
            List<ReadingQualityPropertyValue> checkedReadingQualities = getCheckedReadingQualityValues(underlyingSystem);
            Map<Channel, Range<Instant>> dependentScope = scopePerChannelPerChannelsContainer.entrySet().stream()
                    .flatMap(containerAndScopeByChannel -> containerAndScopeByChannel.getKey()
                            .findDependentChannelScope(containerAndScopeByChannel.getValue())
                            .entrySet()
                            .stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Range::span));
            dependentScope.entrySet().stream()
                    .collect(Collectors.groupingBy(channelAndScope -> channelAndScope.getKey().getChannelsContainer()))
                    .entrySet()
                    .forEach(containerAndChannelsWithScopes -> {
                        Set<ValidationRule> readingQualitiesBasedRules = validationService.activeRuleSets(containerAndChannelsWithScopes.getKey())
                                .stream()
                                .map(ValidationRuleSet::getRules)
                                .flatMap(List::stream)
                                .filter(ValidationRule::isActive)
                                .filter(rule -> rule.getImplementation().equals(DefaultValidatorFactory.READING_QUALITIES_VALIDATOR))
                                .filter(rule -> ReadingQualitiesValidator.getSelectedReadingQualities(rule.getProps()).stream()
                                        .anyMatch(checkedReadingQualities::contains))
                                .collect(Collectors.toSet());
                        Map<Channel, Range<Instant>> impactedScope = containerAndChannelsWithScopes.getValue().stream()
                                .filter(channelAndScope -> readingQualitiesBasedRules.stream()
                                        .anyMatch(rule -> rule.appliesTo(channelAndScope.getKey().getMainReadingType())))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                        validationService.validate(containerAndChannelsWithScopes.getKey(), impactedScope);
                    });
        }
    }

    private static Map<ChannelsContainer, Map<Channel, Range<Instant>>> determineScopePerChannelPerChannelsContainer(LocalEvent event) {
        String eventTopic = event.getType().getTopic();
        if (eventTopic.equals(CREATED_TOPIC)) {
            ReadingStorer storer = (ReadingStorer) event.getSource();
            if (StorerProcess.CONFIRM == storer.getStorerProcess()) {
                return storer.getScope().entrySet().stream()
                        .collect(Collectors.groupingBy(entry -> entry.getKey().getChannelContainer(),
                                Collectors.toMap(entry -> entry.getKey().getChannel(), Map.Entry::getValue, Range::span)));
            }
        } else if (eventTopic.equals(VALIDATED_TOPIC) || eventTopic.equals(INVALIDATED_TOPIC)) {
            ValidationScope scope = (ValidationScope) event.getSource();
            return ImmutableMap.of(scope.getChannelsContainer(), scope.getValidationScope());
        }
        return Collections.emptyMap();
    }

    private static QualityCodeSystem getUnderlyingSystem(Collection<ChannelsContainer> containers) {
        return containers.stream()
                .map(container -> container instanceof MetrologyContractChannelsContainer ?
                                QualityCodeSystem.MDM : QualityCodeSystem.MDC)
                .distinct()
                .reduce((system1, system2) -> {
                    throw new IllegalArgumentException("ReadingStorer is not designed to store readings for several systems at once");
                })
                .orElseThrow(() -> new IllegalArgumentException("No channels container found from ReadingStorer scope"));
    }

    private static List<ReadingQualityPropertyValue> getCheckedReadingQualityValues(QualityCodeSystem system) {
        return CHECKED_QUALITY_CODE_ENDINGS.stream()
                .map(ending -> system.ordinal() + ending)
                .map(ReadingQualityPropertyValue::new)
                .collect(Collectors.toList());
    }
}
