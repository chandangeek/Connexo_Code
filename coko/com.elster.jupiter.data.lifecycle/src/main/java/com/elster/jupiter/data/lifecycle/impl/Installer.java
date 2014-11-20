package com.elster.jupiter.data.lifecycle.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskService;

class Installer {

	private DataModel dataModel;
	private Thesaurus thesaurus;
	private MessageService messageService;
	private TaskService taskService;
	
	Installer (DataModel dataModel, Thesaurus thesaurus, MessageService messageService, TaskService taskService) {
		this.dataModel = dataModel;
		this.thesaurus = thesaurus;
		this.messageService = messageService;
		this.taskService = taskService;
	}
	
	void install() {
		List<LifeCycleCategory> categories = new ArrayList<>();
		for (LifeCycleCategoryKind category : LifeCycleCategoryKind.values()) {
			LifeCycleCategory newCategory = new LifeCycleCategoryImpl(dataModel).init(category);
			dataModel.persist(newCategory);
			categories.add(newCategory);
		}
		createTask();
		createTranslations(categories);
	}
	
	private void createTranslations(List<LifeCycleCategory> categories) {
	        List<Translation> translations = new ArrayList<>(categories.size());
	        for (LifeCycleCategory category : categories) {
	        	SimpleNlsKey nlsKey = SimpleNlsKey.key(LifeCycleService.COMPONENTNAME, Layer.DOMAIN, category.getTranslationKey()).defaultMessage(category.getName());
	            translations.add(toTranslation(nlsKey, Locale.ENGLISH, category.getName()));
	        }
	        thesaurus.addTranslations(translations);
	    }

	private Translation toTranslation(final SimpleNlsKey nlsKey, final Locale locale, final String translation) {
        return new Translation() {
            @Override
            public NlsKey getNlsKey() {
                return nlsKey;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getTranslation() {
                return translation;
            }
        };
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
}

