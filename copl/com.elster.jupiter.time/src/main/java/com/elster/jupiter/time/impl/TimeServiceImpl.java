/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.impl.parser.CronExpressionDescriptorImpl;
import com.elster.jupiter.time.impl.parser.TranslationKeys;
import com.elster.jupiter.time.security.Privileges;
import com.elster.jupiter.time.spi.RelativePeriodCategoryTranslationProvider;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_2SimpleUpgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
        name = "com.elster.jupiter.time",
        service = {TimeService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = "name=" + TimeService.COMPONENT_NAME,
        immediate = true)
public final class TimeServiceImpl implements TimeService, TranslationKeyProvider, MessageSeedProvider {
    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile OrmService ormService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private Set<ComponentAndLayer> alreadyJoined = ConcurrentHashMap.newKeySet();
    private final Object thesaurusLock = new Object();
    private volatile UserService userService;
    private volatile EventService eventService;
    private volatile UpgradeService upgradeService;

    public TimeServiceImpl() {
    }

    @Inject
    public TimeServiceImpl(QueryService queryService, OrmService ormService, NlsService nlsService, UserService userService, EventService eventService, UpgradeService upgradeService) {
        this();
        this.setQueryService(queryService);
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setUserService(userService);
        this.setEventService(eventService);
        this.setUpgradeService(upgradeService);
        activate();
    }

    @Override
    public Optional<RelativePeriod> findRelativePeriod(long relativePeriodId) {
        return this.getDataModel().mapper(RelativePeriod.class).getUnique("id", relativePeriodId);
    }

    @Override
    public Optional<RelativePeriod> findAndLockRelativePeriodByIdAndVersion(long id, long version) {
        return this.getDataModel().mapper(RelativePeriod.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<RelativePeriod> findRelativePeriodByName(String name) {
        return this.getDataModel().mapper(RelativePeriod.class).getUnique("name", name);
    }

    @Override
    public List<RelativePeriod> getRelativePeriods() {
        return dataModel.mapper(RelativePeriod.class).find();
    }

    @Override
    public RelativePeriod createDefaultRelativePeriod(String name, RelativeDate from, RelativeDate to, List<RelativePeriodCategory> categories) {
        RelativePeriodImpl relativePeriod = dataModel.getInstance(RelativePeriodImpl.class);
        relativePeriod.setName(name);
        relativePeriod.setRelativeDateFrom(from);
        relativePeriod.setRelativeDateTo(to);
        relativePeriod.setIsCreatedByInstaller(true);
        categories.forEach(relativePeriod::addRelativePeriodCategory);
        relativePeriod.save();
        return relativePeriod;
    }


    @Override
    public RelativePeriod createRelativePeriod(String name, RelativeDate from, RelativeDate to, List<RelativePeriodCategory> categories) {
        RelativePeriodImpl relativePeriod = dataModel.getInstance(RelativePeriodImpl.class);
        relativePeriod.setName(name);
        relativePeriod.setRelativeDateFrom(from);
        relativePeriod.setRelativeDateTo(to);
        categories.forEach(relativePeriod::addRelativePeriodCategory);
        relativePeriod.save();
        return relativePeriod;
    }

    @Override
    public RelativePeriod updateRelativePeriod(Long id, String name, RelativeDate from, RelativeDate to, List<RelativePeriodCategory> categories) {
        RelativePeriodImpl relativePeriod = RelativePeriodImpl.class.cast(findRelativePeriod(id));
        relativePeriod.setName(name);
        relativePeriod.setRelativeDateFrom(from);
        relativePeriod.setRelativeDateTo(to);
        relativePeriod.setRelativePeriodCategoryUsages(categories);
        relativePeriod.save();
        return relativePeriod;
    }

    @Override
    public RelativePeriodCategory createRelativePeriodCategory(String name) {
        RelativePeriodCategoryImpl category = RelativePeriodCategoryImpl.from(getDataModel(), name);
        category.save();
        return category;
    }

    @Override
    public Optional<RelativePeriodCategory> findRelativePeriodCategory(long relativePeriodCategoryId) {
        return this.getDataModel().mapper((RelativePeriodCategory.class)).getUnique("id", relativePeriodCategoryId);
    }

    @Override
    public Optional<RelativePeriodCategory> findRelativePeriodCategoryByName(String name) {
        return this.getDataModel().mapper((RelativePeriodCategory.class)).getUnique("name", name);
    }

    @Override
    public List<RelativePeriodCategory> getRelativePeriodCategories() {
        return this.getDataModel().mapper((RelativePeriodCategory.class)).find();
    }

    @Override
    public Query<? extends RelativePeriod> getRelativePeriodQuery() {
        return queryService.wrap(dataModel.query(RelativePeriod.class, RelativePeriodCategoryUsageImpl.class, RelativePeriodCategory.class));
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(QueryService.class).toInstance(queryService);
                bind(OrmService.class).toInstance(ormService);
                bind(Thesaurus.class).toProvider(() -> thesaurus);
                bind(MessageInterpolator.class).toProvider(() -> thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(EventService.class).toInstance(eventService);
                bind(TimeService.class).toInstance(TimeServiceImpl.this);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
        upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, V10_2SimpleUpgrader.V10_2_UPGRADER);
    }

    @Override
    public String toLocalizedString(PeriodicalScheduleExpression expression) {
        return expression.toString(thesaurus);
    }

    @Override
    public String toLocalizedString(CronExpression expression, Locale locale) {
        return new CronExpressionDescriptorImpl(thesaurus).getDescription(expression.toString(), locale);
    }

    @Override
    public String toLocalizedString(TemporalExpression expression) {
        TimeDuration every = expression.getEvery();
        int count = every.getCount();
        TimeDuration.TimeUnit unit = every.getTimeUnit();

        if (unit.equals(TimeDuration.TimeUnit.MINUTES)) {
            if (count == 1) {
                return thesaurus.getFormat(Labels.EVERY_MINUTE).format();
            } else {
                return thesaurus.getFormat(Labels.EVERY_N_MINUTES).format(count);
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.HOURS)) {
            if (count == 1) {
                return thesaurus.getFormat(Labels.EVERY_HOUR).format();
            } else {
                return thesaurus.getFormat(Labels.EVERY_N_HOUR).format(count);
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.DAYS)) {
            if (count == 1) {
                return thesaurus.getFormat(Labels.EVERY_DAY).format();
            } else {
                return thesaurus.getFormat(Labels.EVERY_N_DAY).format(count);
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.WEEKS)) {
            if (count == 1) {
                return thesaurus.getFormat(Labels.EVERY_WEEK).format();
            } else {
                return thesaurus.getFormat(Labels.EVERY_N_WEEKS).format(count);
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.MONTHS)) {
            if (count == 1) {
                return thesaurus.getFormat(Labels.EVERY_MONTH).format();
            } else {
                return thesaurus.getFormat(Labels.EVERY_N_MONTHS).format(count);
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.YEARS)) {
            if (count == 1) {
                return thesaurus.getFormat(Labels.EVERY_YEAR).format();
            } else {
                return thesaurus.getFormat(Labels.EVERY_N_YEARS).format(count);
            }
        } else {
            throw new IllegalArgumentException("Unknown time unit " + unit);
        }
    }

    @Override
    public RelativePeriod getAllRelativePeriod() {
        return AllRelativePeriod.INSTANCE;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel(COMPONENT_NAME, "Time Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(TimeService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @SuppressWarnings("unused") // Called by OSGi framework when RelativePeriodCategoryTranslationProvider component activates
    public void addIssueGroupTranslationProvider(RelativePeriodCategoryTranslationProvider provider) {
        synchronized (this.thesaurusLock) {
            ComponentAndLayer componentAndLayer = ComponentAndLayer.from(provider);
            if (!this.alreadyJoined.contains(componentAndLayer)) {
                Thesaurus providerThesaurus = this.nlsService.getThesaurus(provider.getComponentName(), provider.getLayer());
                this.thesaurus = this.thesaurus.join(providerThesaurus);
                this.alreadyJoined.add(componentAndLayer);
            }
        }
    }

    @SuppressWarnings("unused") // Called by OSGi framework when RelativePeriodCategoryTranslationProvider component deactivates
    public void removeIssueGroupTranslationProvider(RelativePeriodCategoryTranslationProvider obsolete) {
        // Don't bother unjoining the provider's thesaurus
    }

    DataModel getDataModel() {
        return dataModel;
    }

    Thesaurus getThesaurus() {
        return thesaurus;
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
        return Stream.of(
                Arrays.stream(Labels.values()),
                Arrays.stream(DefaultRelativePeriodDefinition.RelativePeriodTranslationKey.values()),
                Arrays.stream(TranslationKeys.values()),
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    private static final class ComponentAndLayer {
        private final String componentName;
        private final Layer layer;

        private ComponentAndLayer(String componentName, Layer layer) {
            this.componentName = componentName;
            this.layer = layer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ComponentAndLayer that = (ComponentAndLayer) o;
            return Objects.equals(componentName, that.componentName) &&
                    layer == that.layer;
        }

        @Override
        public int hashCode() {
            return Objects.hash(componentName, layer);
        }

        public static ComponentAndLayer from(RelativePeriodCategoryTranslationProvider provider) {
            return new ComponentAndLayer(provider.getComponentName(), provider.getLayer());
        }
    }

}