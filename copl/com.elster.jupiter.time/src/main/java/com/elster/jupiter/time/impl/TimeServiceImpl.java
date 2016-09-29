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

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
    private volatile Thesaurus thesaurus;
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
        this.setThesaurus(nlsService);
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
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
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
        String everyTranslation = thesaurus.getFormat(TranslationKeys.every).format();

        String unitTranslation = unit.getDescription();
        if (unit.equals(TimeDuration.TimeUnit.MINUTES)) {
            if (count == 1) {
                unitTranslation = thesaurus.getFormat(TranslationKeys.minute).format();
            } else {
                unitTranslation = thesaurus.getFormat(TranslationKeys.minutes).format();
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.HOURS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getFormat(TranslationKeys.hour).format();
            } else {
                unitTranslation = thesaurus.getFormat(TranslationKeys.hours).format();
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.DAYS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getFormat(TranslationKeys.day).format();
            } else {
                unitTranslation = thesaurus.getFormat(TranslationKeys.days).format();
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.WEEKS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getFormat(TranslationKeys.week).format();
            } else {
                unitTranslation = thesaurus.getFormat(TranslationKeys.weeks).format();
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.MONTHS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getFormat(TranslationKeys.month).format();
            } else {
                unitTranslation = thesaurus.getFormat(TranslationKeys.months).format();
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.YEARS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getFormat(TranslationKeys.year).format();
            } else {
                unitTranslation = thesaurus.getFormat(TranslationKeys.years).format();
            }
        }
        if (count == 1) {
            return everyTranslation + " " + unitTranslation;
        } else {
            return everyTranslation + " " + count + " " + unitTranslation;
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
    public void setThesaurus(NlsService nlsService) {
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

}
