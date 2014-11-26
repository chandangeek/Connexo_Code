package com.elster.jupiter.data.lifecycle.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.PurgeConfiguration;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LifeCycleClass;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.google.inject.AbstractModule;

@Component(name="com.elster.jupiter.data.lifecycle", property = "name=" + LifeCycleService.COMPONENTNAME)
public class LifeCycleServiceImpl implements LifeCycleService, InstallService {
	
	private volatile OrmService ormService;
	private volatile DataModel dataModel;
	private volatile Thesaurus thesaurus;
	private volatile MessageService messageService;
	private volatile TaskService taskService;
	private volatile MeteringService meteringService;
	private volatile Clock clock;
	
	public LifeCycleServiceImpl() {	
	}

	@Inject
	public LifeCycleServiceImpl(OrmService ormService, NlsService nlsService, MessageService messageService, TaskService taskService, MeteringService meteringService, Clock clock) {
		setOrmService(ormService);
		setNlsService(nlsService);
		setMessageService(messageService);
		setTaskService(taskService);
		setMeteringService(meteringService);
		setClock(clock);
		if (!dataModel.isInstalled()) {
			install();
		}
	}
	
	@Override
	public void install() {		
		dataModel.install(true, true);
		new Installer(dataModel, thesaurus, messageService , taskService).install();
	}
	
	@Override
	public List<String> getPrerequisiteModules() {
		return Arrays.asList(OrmService.COMPONENTNAME, MessageService.COMPONENTNAME, TaskService.COMPONENTNAME, NlsService.COMPONENTNAME);
	}

	@Reference
	public void setOrmService(OrmService ormService) {
		this.ormService = ormService;
		dataModel = ormService.newDataModel("LFC", "Data Life Cycle Management");
		for (TableSpecs table : TableSpecs.values()) {
			table.addTo(dataModel);
		}
		dataModel.register(new AbstractModule() {			
			@Override
			protected void configure() {
				bind(DataModel.class).toInstance(dataModel);
			}
		});
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
	public void setMeteringService(MeteringService meteringService) {
		this.meteringService = meteringService;
	}
	
	@Reference
	public void setClock(Clock clock) {
		this.clock = clock;
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
				.registerLimit(limit(getCategory(LifeCycleCategoryKind.REGISTER).getRetention()))
				.intervalLimit(limit(getCategory(LifeCycleCategoryKind.INTERVAL).getRetention()))
				.dailyLimit(limit(getCategory(LifeCycleCategoryKind.DAILY).getRetention()))
				.eventLimit(limit(getCategory(LifeCycleCategoryKind.ENDDEVICEEVENT).getRetention()))
				.logger(logger)
				.build();
		meteringService.purge(purgeConfiguration);
	}
	
	@Override
	public List<LifeCycleCategory> getCategoriesAsOf(Instant instant) {
		return getCategories().stream()
			.map(LifeCycleCategoryImpl.class::cast)
			.map(lifeCycleCategory -> lifeCycleCategory.asOf(instant))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}
}
