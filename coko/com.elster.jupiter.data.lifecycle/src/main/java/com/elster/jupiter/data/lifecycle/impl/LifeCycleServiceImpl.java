package com.elster.jupiter.data.lifecycle.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskExecutor;
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
	private volatile Clock clock;
	
	public LifeCycleServiceImpl() {	
	}

	@Inject
	public LifeCycleServiceImpl(OrmService ormService, NlsService nlsService, MessageService messageService, TaskService taskService, Clock clock) {
		setOrmService(ormService);
		setNlsService(nlsService);
		setMessageService(messageService);
		setTaskService(taskService);
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
	public void runNow() {
		TaskExecutor executor = new LifeCycleTaskExecutor(this);
		TaskOccurrence occurrence = getTask().createTaskOccurrence();
		executor.execute(occurrence);
	}

	public void execute(Logger logger) {
		LifeCycleCategory journal = getCategories().stream().filter(cat -> cat.getKind() == LifeCycleCategoryKind.JOURNAL).findFirst().get();
		Instant instant = clock.instant().minus(journal.getRetention()).truncatedTo(ChronoUnit.DAYS);
		logger.info("Removing journals up to " + instant);
		ormService.dropJournal(instant,logger);
	}
	
}
