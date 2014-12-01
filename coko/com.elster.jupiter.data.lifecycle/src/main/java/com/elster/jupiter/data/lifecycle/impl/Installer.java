package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.tasks.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class Installer {

	public static final String DATA_LIFE_CYCLE_DESTINATION_NAME = "DataLifeCycle";
	public static final String DATA_LIFECYCLE_RECCURENT_TASK_NAME = "Data Lifecycle";
	private DataModel dataModel;
	private MessageService messageService;
	private TaskService taskService;
	
	Installer (DataModel dataModel, MessageService messageService, TaskService taskService) {
		this.dataModel = dataModel;
		this.messageService = messageService;
		this.taskService = taskService;
	}
	
	void install() {
		List<LifeCycleCategory> categories = new ArrayList<>();
		for (LifeCycleCategoryKind category : LifeCycleCategoryKind.values()) {
			LifeCycleCategory newCategory = new LifeCycleCategoryImpl(dataModel).init(category);
			try {
				dataModel.persist(newCategory);
			} catch (UnderlyingSQLFailedException ex){
				Logger.getLogger(this.getClass().getName()).warning("The LifeCycleCategory '" + newCategory.getName() + "' already exists");
			}
			categories.add(newCategory);
		}
		createTask();
	}

	private DestinationSpec getDestination() {
		return messageService.getDestinationSpec(DATA_LIFE_CYCLE_DESTINATION_NAME).orElse(
				messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get().createDestinationSpec(DATA_LIFE_CYCLE_DESTINATION_NAME, 10));
	}
	
	private void createTask() {
		if (!taskService.getRecurrentTask(DATA_LIFECYCLE_RECCURENT_TASK_NAME).isPresent()) {
			taskService.newBuilder()
					.setName(DATA_LIFECYCLE_RECCURENT_TASK_NAME)
					.setScheduleExpressionString("0 0 18 ? * 1L") // last sunday of the month at 18:00
					.setDestination(getDestination())
					.setPayLoad("Data Lifecycle")
					.scheduleImmediately()
					.build().save();
		}
	}
}

