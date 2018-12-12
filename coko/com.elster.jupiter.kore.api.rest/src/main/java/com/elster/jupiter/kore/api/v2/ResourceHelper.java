/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.usagepoint.lifecycle.RequiredMicroActionPropertiesException;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceHelper {

    private final Provider<MetrologyConfigurationInfoFactory> metrologyConfigurationInfoFactory;
    private final Provider<MetrologyConfigurationPurposeInfoFactory> metrologyConfigurationPurposeInfoFactory;
    private final Provider<UsagePointInfoFactory> usagePointInfoFactory;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final UsagePointLifeCycleService usagePointLifeCycleService;
    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(Provider<MetrologyConfigurationInfoFactory> metrologyConfigurationInfoFactory,
                          Provider<MetrologyConfigurationPurposeInfoFactory> metrologyConfigurationPurposeInfoFactory,
                          Provider<UsagePointInfoFactory> usagePointInfoFactory,
                          UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService, UsagePointLifeCycleService usagePointLifeCycleService, MeteringService meteringService,
                          MetrologyConfigurationService metrologyConfigurationService,
                          ExceptionFactory exceptionFactory) {
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
        this.metrologyConfigurationPurposeInfoFactory = metrologyConfigurationPurposeInfoFactory;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.usagePointLifeCycleService = usagePointLifeCycleService;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.exceptionFactory = exceptionFactory;
    }

    public UsagePointMetrologyConfiguration findUsagePointMetrologyConfigurationById(long id) {
        return metrologyConfigurationService
                .findMetrologyConfiguration(id)
                .filter(config -> config instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(() -> exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION));
    }

    public Optional<MetrologyConfiguration> findMetrologyConfiguration(long id) {
        return metrologyConfigurationService.findMetrologyConfiguration(id);
    }

    public Optional<MeterActivation> findMeterActivation(UsagePoint usagePoint, MeterRole meterRole, Instant effectiveAt){
        return usagePoint.getMeterActivations(meterRole).stream().filter(ma -> ma.isEffectiveAt(effectiveAt)).findFirst();
    }

    public Optional<UsagePoint> findUsagePointByMrid(String mrid) {
        return meteringService.findUsagePointByMRID(mrid);
    }

    public Optional<UsagePoint> findAndLockUsagePoint(String mrid, long version) {
        return meteringService.findAndLockUsagePointByMRIDAndVersion(mrid, version);
    }

    public UsagePointTransition getTransitionByIdOrThrowException(long id) {
        return this.usagePointLifeCycleConfigurationService.findUsagePointTransition(id)
                .orElseThrow(this.exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_LIFE_CYCLE_TRANSITION, id));
    }

    public void applyMetrologyConfigurationToUsagePoint(UsagePoint usagePoint, EffectiveMetrologyConfigurationInfo info) {
        RestValidationBuilder validationBuilder = new RestValidationBuilder()
                .notEmpty(info.id, "id");
        validationBuilder.validate();

        UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = (UsagePointMetrologyConfiguration) this.findMetrologyConfiguration(info.metrologyConfiguration.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION));

        if (info.purposes != null) {
            usagePoint.apply(usagePointMetrologyConfiguration, Instant.ofEpochMilli(info.id), usagePointMetrologyConfiguration.getContracts()
                    .stream()
                    .filter(metrologyContract -> !metrologyContract.getDeliverables().isEmpty())
                    .filter(metrologyContract -> info.purposes.stream()
                            .anyMatch(purpose -> metrologyContract.getId() == purpose.id))
                    .filter(metrologyContract -> !metrologyContract.isMandatory())
                    .distinct()
                    .collect(Collectors.toSet()));
        } else {
            usagePoint.apply(usagePointMetrologyConfiguration, Instant.ofEpochMilli(info.id));
        }
        usagePoint.update();
    }

    public void performUsagePointTransition(UsagePoint usagePoint, UsagePointTransitionInfo info) {
        if(info.transitionNow==null && info.effectiveTimestamp==null){
            return;
        }
        UsagePointTransition transition = getTransitionByIdOrThrowException(info.id);
        UsagePointStateChangeRequest changeRequest;

        if ((info.transitionNow!= null && info.transitionNow) || info.effectiveTimestamp == null) {
            changeRequest = this.usagePointLifeCycleService.performTransition(usagePoint, transition, "INS", Collections.emptyMap());
        } else {
            changeRequest = this.usagePointLifeCycleService.scheduleTransition(usagePoint, transition, info.effectiveTimestamp, "INS", Collections.emptyMap());
        }
        if (changeRequest.getStatus().equals(UsagePointStateChangeRequest.Status.FAILED)) {
            throw exceptionFactory.newException(MessageSeeds.TRANSITION_FAILED, changeRequest.getGeneralFailReason());
        }
    }
}
