package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.security.Privileges;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.ValidationVersionStatus;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "UsagePointConfigurationServiceImpl",
        service = {UsagePointConfigurationService.class, TranslationKeyProvider.class},
        property = {"name=" + UsagePointConfigurationService.COMPONENTNAME},
        immediate = true)
public class UsagePointConfigurationServiceImpl implements UsagePointConfigurationService, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile Clock clock;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile EventService eventService;
    private volatile ValidationService validationService;
    private volatile UserService userService;
    private volatile Thesaurus thesaurus;
    private volatile UpgradeService upgradeService;

    // For OSGi purpose
    public UsagePointConfigurationServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public UsagePointConfigurationServiceImpl(Clock clock, OrmService ormService, EventService eventService, UserService userService,
                                              ValidationService validationService, NlsService nlsService, MetrologyConfigurationService metrologyConfigurationService, UpgradeService upgradeService) {
        this();
        setClock(clock);
        setOrmService(ormService);
        setMetrologyConfigurationService(metrologyConfigurationService);
        setEventService(eventService);
        setUserService(userService);
        setValidationService(validationService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        activate();
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Clock.class).toInstance(clock);
                bind(ValidationService.class).toInstance(validationService);
                bind(UserService.class).toInstance(userService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UsagePointConfigurationService.class).toInstance(UsagePointConfigurationServiceImpl.this);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());

        upgradeService.register(InstallIdentifier.identifier("Insight", UsagePointConfigurationService.COMPONENTNAME), dataModel, Installer.class, ImmutableMap.of(
                version(10, 2), UpgraderV10_2.class
        ));
    }


    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "Usage Point Configuration");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference(target = "(com.elster.jupiter.checklist=Insight)")
    public void setCheckList(UpgradeCheckList upgradeCheckList) {
        // just explicitly depend
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public String getComponentName() {
        return UsagePointConfigurationService.COMPONENTNAME;
    }

    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        Arrays.stream(DefaultTranslationKey.values()).forEach(translationKeys::add);
        Arrays.stream(Privileges.values()).forEach(translationKeys::add);
        return translationKeys;
    }

    @Override
    public void link(UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration) {
        usagePoint.apply(metrologyConfiguration, this.clock.instant());
    }

    @Override
    public boolean isInUse(MetrologyConfiguration metrologyConfiguration) {
        return this.metrologyConfigurationService.isInUse(metrologyConfiguration);
    }

    @Override
    public void addValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet) {
        this.dataModel
                .getInstance(MetrologyContractValidationRuleSetUsageImpl.class)
                .initAndSave(metrologyContract, validationRuleSet);
        metrologyContract.update();
    }

    @Override
    public void removeValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet) {
        this.dataModel
                .mapper(MetrologyContractValidationRuleSetUsage.class)
                .getUnique(MetrologyContractValidationRuleSetUsageImpl.Fields.METROLOGY_CONTRACT.fieldName(), metrologyContract,
                        MetrologyContractValidationRuleSetUsageImpl.Fields.VALIDATION_RULE_SET.fieldName(), validationRuleSet)
                .ifPresent(metrologyContractValidationRuleSetUsage -> dataModel.remove(metrologyContractValidationRuleSetUsage));
        metrologyContract.update();
    }

    @Override
    public List<ValidationRuleSet> getValidationRuleSets(MetrologyContract metrologyContract) {
        Condition condition = where(MetrologyContractValidationRuleSetUsageImpl.Fields.METROLOGY_CONTRACT.fieldName())
                .isEqualTo(metrologyContract);
        return this.dataModel
                .query(MetrologyContractValidationRuleSetUsage.class)
                .select(condition)
                .stream()
                .map(MetrologyContractValidationRuleSetUsage::getValidationRuleSet)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isLinkableValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet, List<ValidationRuleSet> linkedValidationRuleSets) {
        if (linkedValidationRuleSets.contains(validationRuleSet)) {
            return false;
        }
        if (!validationRuleSet.getRuleSetVersions().isEmpty()) {
            ValidationRuleSetVersion activeRuleSetVersion = validationRuleSet.getRuleSetVersions()
                    .stream()
                    .filter(validationRuleSetVersion -> validationRuleSetVersion.getStatus() == ValidationVersionStatus.CURRENT)
                    .findFirst()
                    .get();
            if (!activeRuleSetVersion.getRules().isEmpty()) {
                List<ReadingType> ruleSetReadingTypes = activeRuleSetVersion
                        .getRules()
                        .stream()
                        .flatMap(rule -> rule.getReadingTypes().stream())
                        .collect(Collectors.toList());
                List<String> ruleSetReadingTypeMRIDs = ruleSetReadingTypes
                        .stream()
                        .map(ReadingType::getMRID)
                        .collect(Collectors.toList());
                if (!metrologyContract.getDeliverables().isEmpty()) {
                    List<String> deliverableReadingTypeMRIDs = metrologyContract.getDeliverables()
                            .stream()
                            .map(readingTypeDeliverable -> readingTypeDeliverable.getReadingType().getMRID())
                            .collect(Collectors.toList());
                    if (deliverableReadingTypeMRIDs.stream().anyMatch(ruleSetReadingTypeMRIDs::contains)) {
                        return true;
                    } else {
                        ReadingTypeRequirementsCollector requirementChecker = new ReadingTypeRequirementsCollector();
                        metrologyContract.getDeliverables()
                                .stream()
                                .map(ReadingTypeDeliverable::getFormula)
                                .map(Formula::getExpressionNode)
                                .forEach(expressionNode -> expressionNode.accept(requirementChecker));
                        for (ReadingTypeRequirement readingTypeRequirement : requirementChecker.getReadingTypeRequirements()) {
                            if (readingTypeRequirement instanceof FullySpecifiedReadingTypeRequirement && ruleSetReadingTypes.contains(((FullySpecifiedReadingTypeRequirement) readingTypeRequirement).getReadingType())) {
                                return true;
                            } else if (readingTypeRequirement instanceof PartiallySpecifiedReadingTypeRequirement) {
                                ReadingTypeTemplate readingTypeTemplate = ((PartiallySpecifiedReadingTypeRequirement) readingTypeRequirement).getReadingTypeTemplate();
                                return ruleSetReadingTypes.stream().anyMatch(readingTypeTemplate::matches);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        Condition condition = where(MetrologyContractValidationRuleSetUsageImpl.Fields.VALIDATION_RULE_SET.fieldName())
                .isEqualTo(ruleset);
        return !this.dataModel
                .query(MetrologyContractValidationRuleSetUsage.class)
                .select(condition)
                .isEmpty();
    }
}