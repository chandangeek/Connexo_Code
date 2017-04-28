/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfoFactory;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceHelper {
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final CustomPropertySetService customPropertySetService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final UsagePointLifeCycleStateInfoFactory usagePointLifeCycleStateInfoFactory;

    @Inject
    public ResourceHelper(MetrologyConfigurationService metrologyConfigurationService, ConcurrentModificationExceptionFactory conflictFactory, CustomPropertySetService customPropertySetService, UsagePointConfigurationService usagePointConfigurationService, UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService, UsagePointLifeCycleStateInfoFactory usagePointLifeCycleStateInfoFactory) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.conflictFactory = conflictFactory;
        this.customPropertySetService = customPropertySetService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.usagePointLifeCycleStateInfoFactory = usagePointLifeCycleStateInfoFactory;
    }

    UsagePointMetrologyConfiguration getMetrologyConfigOrThrowException(long metrologyConfigId) {
        MetrologyConfiguration mc = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (mc instanceof UsagePointMetrologyConfiguration) {
            return (UsagePointMetrologyConfiguration) mc;
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private Optional<UsagePointMetrologyConfiguration> getLockedMetrologyConfiguration(long id, long version) {
        return metrologyConfigurationService
                .findAndLockMetrologyConfiguration(id, version)
                .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast);
    }

    private Long getCurrentMetrologyConfigurationVersion(long id) {
        return metrologyConfigurationService.findMetrologyConfiguration(id)
                .map(MetrologyConfiguration::getVersion)
                .orElse(null);
    }

    UsagePointMetrologyConfiguration findAndLockMetrologyConfiguration(MetrologyConfigurationInfo info) {
        return getLockedMetrologyConfiguration(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentMetrologyConfigurationVersion(info.id))
                        .supplier());
    }

    RegisteredCustomPropertySet getRegisteredCustomPropertySetOrThrowException(String id) {
        return customPropertySetService.findActiveCustomPropertySet(id)
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    // In fact the CPS values are considered part of the MetrologyConfiguration, so we need to rely on metrology version
    UsagePointMetrologyConfiguration findAndLockCPSOnMetrologyConfiguration(CustomPropertySetInfo<MetrologyConfigurationInfo> info) {
        return getLockedMetrologyConfiguration(info.parent.id, info.parent.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualParent(() -> getCurrentMetrologyConfigurationVersion(info.parent.id), info.parent.id)
                        .withActualVersion(() -> getCurrentMetrologyConfigurationVersion(info.parent.id))
                        .supplier());
    }

    MetrologyContract findAndLockContractOnMetrologyConfiguration(MetrologyContractInfo metrologyContractInfo) {
        return findAndLockContractOnMetrologyConfiguration(metrologyContractInfo.id, metrologyContractInfo.version, metrologyContractInfo.name);
    }

    MetrologyContract findAndLockContractOnMetrologyConfiguration(LinkableMetrologyContractInfo metrologyContractInfo) {
        return findAndLockContractOnMetrologyConfiguration(metrologyContractInfo.getId(), metrologyContractInfo.getVersion(), metrologyContractInfo.getName());
    }

    MetrologyContract findAndLockContractOnMetrologyConfiguration(long id, long version, String name) {
        return metrologyConfigurationService.findAndLockMetrologyContract(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(name)
                        .withActualVersion(() -> getCurrentMetrologyContractVersion(id))
                        .supplier());
    }

    MetrologyContract findContractByIdOrThrowException(long contractId) {
        return metrologyConfigurationService.findMetrologyContract(contractId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    LinkableMetrologyContractInfo getLinkableMetrologyContractInfo(MetrologyContract contract, List<OutputMatchesInfo> matchedOutputs) {
        return getLinkableMetrologyContractInfo(contract, matchedOutputs, null);
    }

    LinkableMetrologyContractInfo getLinkableMetrologyContractInfo(MetrologyContract contract, List<OutputMatchesInfo> matchedOutputs, List<UsagePointLifeCycleStateInfo> states) {
        LinkableMetrologyContractInfo info = new LinkableMetrologyContractInfo();
        info.setMetrologyConfigurationInfo(new IdWithNameInfo(contract.getMetrologyConfiguration().getId(),
                contract.getMetrologyConfiguration().getName()));
        info.setActive(contract.getMetrologyConfiguration().isActive());
        info.setOutputs(matchedOutputs);
        info.setVersion(contract.getVersion());
        info.setId(contract.getId());
        info.setName(contract.getMetrologyPurpose().getName());
        info.setLifeCycleStates(states);

        return info;
    }

    List<UsagePointLifeCycleStateInfo> getUsagePointLifeCycleStateInfos(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet){
        return  usagePointConfigurationService.getStatesLinkedToValidationRuleSetAndMetrologyContract(validationRuleSet, metrologyContract).stream()
                .flatMap(state -> usagePointLifeCycleConfigurationService.getUsagePointLifeCycles().stream()
                        .filter(usagePointLifeCycle -> usagePointLifeCycle.getStates().contains(state))
                        .map(usagePointLifeCycle -> usagePointLifeCycleStateInfoFactory.from(usagePointLifeCycle, state))
                        .findAny()
                        .map(Stream::of)
                        .orElse(Stream.empty()))
                .collect(Collectors.toList());
    }

    List<State> getStates (List<UsagePointLifeCycleStateInfo> lifeCycleStates){
        return lifeCycleStates.stream()
                .map(usagePointLifeCycleStateInfo -> usagePointLifeCycleConfigurationService.findUsagePointState(usagePointLifeCycleStateInfo.id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Long getCurrentMetrologyContractVersion(long id) {
        return metrologyConfigurationService.findMetrologyContract(id)
                .map(MetrologyContract::getVersion)
                .orElse(null);
    }

}