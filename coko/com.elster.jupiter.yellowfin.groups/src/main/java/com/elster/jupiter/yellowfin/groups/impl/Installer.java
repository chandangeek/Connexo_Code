package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Installer {
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final String CRON_STRING = "0 0 0 1/1 * ?";

    private DataModel dataModel;
    private MessageService messageService;
    private TaskService taskService;

    Installer(DataModel dataModel, MessageService messageService, TaskService taskService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.taskService = taskService;
    }

    void install() {
        createTask();
    }

    public DestinationSpec getDestination() {
        return messageService.getDestinationSpec(YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST).orElseGet(() ->
                messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get().createDestinationSpec(YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST, DEFAULT_RETRY_DELAY_IN_SECONDS));
    }

    private SubscriberSpec getSubscriberSpec() {
        return getDestination().getSubscribers().stream().findFirst().orElseGet(() -> getDestination().subscribe(YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST));
    }

    private void createTask() {
        if (!taskService.getRecurrentTask(YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_TASK).isPresent()) {
            taskService.newBuilder()
                    .setName(YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_TASK)
                    .setScheduleExpressionString(CRON_STRING) //
                    .setDestination(getDestination())
                    .setPayLoad(YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_TASK)
                    .scheduleImmediately()
                    .build().save();
        }
        DestinationSpec destination = getDestination();
        if (!destination.isActive()) {
            destination.activate();
        }
        getSubscriberSpec();
    }

}

