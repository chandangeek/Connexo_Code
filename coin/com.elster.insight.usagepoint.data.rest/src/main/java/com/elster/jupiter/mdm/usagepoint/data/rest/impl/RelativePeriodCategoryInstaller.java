/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.LAST_7_DAYS;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_YEAR;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.TODAY;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.YESTERDAY;
import static com.elster.jupiter.util.streams.Currying.perform;

@Component(name = "com.elster.jupiter.udr.relativeperiodcategory.installer", service = RelativePeriodCategoryInstaller.class, immediate = true, property = {"name=URP"})
public class RelativePeriodCategoryInstaller implements FullInstaller {

    private static final Logger LOGGER = Logger.getLogger(RelativePeriodCategoryInstaller.class.getName());
    private volatile UpgradeService upgradeService;
    private volatile TimeService timeService;
    private volatile MeteringService meteringService;

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(RelativePeriodCategoryInstaller.class).toInstance(RelativePeriodCategoryInstaller.this);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("Insight", "URP"), dataModel, RelativePeriodCategoryInstaller.class, Collections.emptyMap());
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create relative period category for URP",
                this::createRelativePeriodCategory,
                logger
        );
        doTry(
                "Assign default relative periods to URP category",
                this::createRelativePeriods,
                logger
        );
    }

    private void createRelativePeriodCategory() {
        try {
            timeService.createRelativePeriodCategory(DefaultTranslationKey.RELATIVE_PERIOD_CATEGORY_USAGE_POINT_VALIDATION_OVERVIEW.getKey());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void createRelativePeriods() {
        RelativePeriodCategory category = getCategory();

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, THIS_YEAR, TODAY, YESTERDAY)
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName())
                            .orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(category);
                });

        this.meteringService
                .getGasDayOptions()
                .ifPresent(perform(this::createGasRelativePeriods).with(category));
    }

    private void createGasRelativePeriods(GasDayOptions gasDayOptions, RelativePeriodCategory category) {
        gasDayOptions
                .getRelativePeriods()
                .forEach(relativePeriod -> relativePeriod.addRelativePeriodCategory(category));
    }


    private RelativePeriodCategory getCategory() {
        return timeService.findRelativePeriodCategoryByName(DefaultTranslationKey.RELATIVE_PERIOD_CATEGORY_USAGE_POINT_VALIDATION_OVERVIEW.getKey())
                .orElseThrow(IllegalArgumentException::new);
    }

}