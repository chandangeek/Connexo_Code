/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.data.lifecycle.security.Privileges;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.PurgeConfiguration;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LifeCycleClass;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.streams.Functions.asStream;

@Component(
		name="com.elster.jupiter.data.lifecycle",
		property = "name=" + LifeCycleService.COMPONENTNAME,
		service = {LifeCycleService.class, TranslationKeyProvider.class},
		immediate = true)
public class LifeCycleServiceImpl implements LifeCycleService, TranslationKeyProvider {

	private volatile OrmService ormService;
	private volatile DataModel dataModel;
	private volatile Thesaurus thesaurus;
	private volatile MessageService messageService;
	private volatile TaskService taskService;
	private volatile IdsService idsService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;
	private volatile Clock clock;
	private volatile UpgradeService upgradeService;

	public LifeCycleServiceImpl() {
	}

	@Inject
	public LifeCycleServiceImpl(OrmService ormService, NlsService nlsService, MessageService messageService, TaskService taskService, IdsService idsService, MeteringService meteringService, UserService userService, Clock clock, UpgradeService upgradeService) {
		this();
		setOrmService(ormService);
		setNlsService(nlsService);
		setMessageService(messageService);
		setTaskService(taskService);
		setIdsService(idsService);
		setMeteringService(meteringService);
        setUserService(userService);
		setClock(clock);
        setUpgradeService(upgradeService);
		activate();
	}

	@Reference
	public void setOrmService(OrmService ormService) {
		this.ormService = ormService;
		dataModel = ormService.newDataModel("LFC", "Data Life Cycle Management");
		for (TableSpecs table : TableSpecs.values()) {
			table.addTo(dataModel);
		}
	}

	@Activate
	public void activate(){
		dataModel.register(new AbstractModule() {
			@Override
			protected void configure() {
				bind(DataModel.class).toInstance(dataModel);
				bind(Thesaurus.class).toInstance(thesaurus);
				bind(MeteringService.class).toInstance(meteringService);
                bind(MessageService.class).toInstance(messageService);
                bind(TaskService.class).toInstance(taskService);
				bind(UserService.class).toInstance(userService);
			}
		});
        upgradeService.register(
        		identifier("Pulse", COMPONENTNAME),
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class
                ));
	}

	@Reference
	public void setNlsService(NlsService nlsService) {
		this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
	}

	@Reference
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	@Reference
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	@Reference
	public void setIdsService(IdsService idsService) {
		this.idsService = idsService;
	}

	@Reference
	public void setMeteringService(MeteringService meteringService) {
		this.meteringService = meteringService;
	}

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

	@Reference
	public void setClock(Clock clock) {
		this.clock = clock;
	}

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
	public List<LifeCycleCategory> getCategories() {
		return dataModel.stream(LifeCycleCategory.class)
			.sorted(Comparator.comparing(LifeCycleCategory::getKind))
			.collect(Collectors.toList());
	}

	@Override
	public RecurrentTask getTask() {
		return taskService.getRecurrentTask("Data Lifecycle").get();
	}

	@Override
	public TaskOccurrence runNow() {
		return getTask().runNow(new LifeCycleTaskExecutor(this));
	}

	private Instant limit(Period period) {
		if (period.getDays() < 30) {
			throw new IllegalArgumentException();
		}
		return clock.instant().minus(period).truncatedTo(ChronoUnit.DAYS);
	}

	private LifeCycleCategory getCategory(LifeCycleCategoryKind kind) {
		return getCategories().stream().filter(cat -> cat.getKind() == kind).findFirst().get();
	}

	public void execute(Logger logger) {
		Instant instant = limit(getCategory(LifeCycleCategoryKind.JOURNAL).getRetention());
		logger.info("Removing journals up to " + instant);
		ormService.dropJournal(instant,logger);
		instant = limit(getCategory(LifeCycleCategoryKind.LOGGING).getRetention());
		logger.info("Removing logging up to " + instant);
		ormService.dropAuto(LifeCycleClass.LOGGING, instant, logger);
		PurgeConfiguration purgeConfiguration = PurgeConfiguration.builder()
				.registerRetention(getCategory(LifeCycleCategoryKind.REGISTER).getRetention())
				.intervalRetention(getCategory(LifeCycleCategoryKind.INTERVAL).getRetention())
				.dailyRetention(getCategory(LifeCycleCategoryKind.DAILY).getRetention())
				.eventRetention(getCategory(LifeCycleCategoryKind.ENDDEVICEEVENT).getRetention())
				.logger(logger)
				.build();
		//meteringService.configurePurge(purgeConfiguration);
		idsService.purge(logger);
		meteringService.purge(purgeConfiguration);
		instant = limit(getCategory(LifeCycleCategoryKind.WEBSERVICES).getRetention());
		ormService.dropAuto(LifeCycleClass.WEBSERVICES, instant, logger);
		instant = clock.instant().plus(360,ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
		logger.info("Adding partitions up to " + instant);
		ormService.createPartitions(instant, logger);
		idsService.extendTo(instant,logger);
	}

	@Override
	public List<LifeCycleCategory> getCategoriesAsOf(Instant instant) {
		return getCategories().stream()
			.map(LifeCycleCategoryImpl.class::cast)
			.map(lifeCycleCategory -> lifeCycleCategory.asOf(instant))
			.flatMap(asStream())
			.collect(Collectors.toList());
	}

	@Override
	public Optional<LifeCycleCategory> findAndLockCategoryByKeyAndVersion(LifeCycleCategoryKind key, long version) {
		return dataModel.mapper(LifeCycleCategory.class).lockObjectIfVersion(version, key);
	}

	@Override
	public String getComponentName() {
		return LifeCycleService.COMPONENTNAME;
	}

	@Override
	public Layer getLayer() {
		return Layer.DOMAIN;
	}

	@Override
	public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Stream.of(LifeCycleCategoryKindTranslationKeys.values()).forEach(keys::add);
        Stream.of(Privileges.values()).forEach(keys::add);
        Stream.of(TranslationKeys.values()).forEach(keys::add);
        return keys;
	}

}