package com.elster.jupiter.validation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.MessageSeeds;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.security.Privileges;

public class InstallerImpl {
    public static final String DESTINATION_NAME = ValidationServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = ValidationServiceImpl.SUBSCRIBER_NAME;
    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;
    private volatile Thesaurus thesaurus;
    private final MessageService messageService;
    private DestinationSpec destinationSpec;

    public InstallerImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, UserService userService,MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.userService = userService;
    }

    public DestinationSpec getDestinationSpec() {
        return destinationSpec;
    }

    public void install(boolean executeDdl, boolean updateOrm) {
        try {
            dataModel.install(executeDdl, updateOrm);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not install datamodel : " + ex.getMessage(), ex);
        }
        setTranslations();
        createEventTypes();
        createDestinationAndSubscriber();
    }


    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Could not install eventType '" + eventType.name() + "': " + ex.getMessage(), ex);
            }
        }
    }

    private void setTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(ValidationService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        for (MessageSeeds.Labels label : MessageSeeds.Labels.values()) {
            translations.add(label.toDefaultTransation());
        }
        try {
            thesaurus.addTranslations(translations);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not install translations : " + ex.getMessage(), ex);
        }
    }

    private void createDestinationAndSubscriber() {
        try
        {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
            destinationSpec.save();
            destinationSpec.activate();
            destinationSpec.subscribe(SUBSCRIBER_NAME);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
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
}
