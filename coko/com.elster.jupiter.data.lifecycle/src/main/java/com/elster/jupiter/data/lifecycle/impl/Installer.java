package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class Installer {

	public static final String DATA_LIFE_CYCLE_DESTINATION_NAME = "DataLifeCycle";
	public static final String DATA_LIFECYCLE_RECCURENT_TASK_NAME = "Data Lifecycle";
	private DataModel dataModel;
	private MessageService messageService;
	private TaskService taskService;
	private MeteringService meteringService;
    private UserService userService;
	
	Installer (DataModel dataModel, MessageService messageService, TaskService taskService, MeteringService meteringService, UserService userService) {
		this.dataModel = dataModel;
		this.messageService = messageService;
		this.taskService = taskService;
		this.meteringService = meteringService;
        this.userService = userService;
	}
	
	void install() {
		List<LifeCycleCategory> categories = new ArrayList<>();
		for (LifeCycleCategoryKind category : LifeCycleCategoryKind.values()) {
			LifeCycleCategory newCategory = new LifeCycleCategoryImpl(dataModel, meteringService).init(category);
			try {
				dataModel.persist(newCategory);
			} catch (UnderlyingSQLFailedException ex){
				Logger.getLogger(this.getClass().getName()).warning("The LifeCycleCategory '" + newCategory.getName() + "' already exists");
			}
			categories.add(newCategory);
		}
		createTask();
        createPrivileges();
	}

	private DestinationSpec getDestination() {
		return messageService.getDestinationSpec(DATA_LIFE_CYCLE_DESTINATION_NAME).orElseGet(() ->
				messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get().createDestinationSpec(DATA_LIFE_CYCLE_DESTINATION_NAME, 10));
	}
	
	private SubscriberSpec getSubscriberSpec() {
		return getDestination().getSubscribers().stream().findFirst().orElseGet(() -> getDestination().subscribe(DATA_LIFE_CYCLE_DESTINATION_NAME));
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
		DestinationSpec destination = getDestination();
		if (!destination.isActive()) {
			destination.activate();
		}
		getSubscriberSpec();		
	}

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("SYS", "dataPurge.dataPurge", "dataPurge.dataPurge.description", new String[]{Privileges.VIEW_DATA_PURGE, Privileges.ADMINISTRATE_DATA_PURGE});
    }
}

