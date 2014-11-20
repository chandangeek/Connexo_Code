package com.elster.jupiter.data.lifecycle.impl;

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

import sun.security.action.GetLongAction;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.google.inject.AbstractModule;

@Component(name="com.elster.jupiter.data.lifecycle")
public class LifeCycleServiceImpl implements LifeCycleService, InstallService {
	
	private volatile OrmService ormService;
	private volatile DataModel dataModel;
	private volatile MessageService messageService;
	private volatile TaskService taskService;
	
	public LifeCycleServiceImpl() {	
	}

	@Inject
	public LifeCycleServiceImpl(OrmService ormService, MessageService messageService, TaskService taskService) {
		setOrmService(ormService);
		setMessageService(messageService);
		setTaskService(taskService);
		if (!dataModel.isInstalled()) {
			install();
		}
	}
	
	@Override
	public void install() {		
		dataModel.install(true, true);
		for (LifeCycleCategoryKind category : LifeCycleCategoryKind.values()) {
			LifeCycleCategory newCategory = new LifeCycleCategoryImpl(dataModel).init(category);
			dataModel.persist(newCategory);
		}
		createTask();
	}

	private DestinationSpec getDestination() {
		return messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get().createDestinationSpec("DataLifeCycle", 10);
	}
	
	private void createTask() {
		taskService.newBuilder()
			.setName("Data Lifecycle")
			.setScheduleExpressionString("0 0 18 ? * 1L") // last sunday of the month at 18:00		
			.setDestination(getDestination())
			.setPayLoad("Data Lifecycle")
			.scheduleImmediately()
			.build().save();
	}
	
	@Override
	public List<String> getPrerequisiteModules() {
		return Arrays.asList(OrmService.COMPONENTNAME, MessageService.COMPONENTNAME, TaskService.COMPONENTNAME);
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
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	@Reference
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
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
	public void execute(Logger logger) {
		LifeCycleCategory journal = getCategories().stream().filter(cat -> cat.getKind() == LifeCycleCategoryKind.JOURNAL).findFirst().get();
		Instant instant = Instant.now().minus(journal.getRetention()).truncatedTo(ChronoUnit.DAYS);
		logger.info("Removing journals up to " + instant);
		ormService.dropJournal(instant,logger);
	}
	
}
