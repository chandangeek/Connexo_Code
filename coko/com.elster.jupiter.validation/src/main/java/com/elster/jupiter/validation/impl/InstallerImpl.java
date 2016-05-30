package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class InstallerImpl implements FullInstaller {
    public static final String DESTINATION_NAME = ValidationServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = ValidationServiceImpl.SUBSCRIBER_NAME;

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private DestinationSpec destinationSpec;
    private final UserService userService;

    @Inject
    public InstallerImpl(DataModel dataModel, EventService eventService, MessageService messageService, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.eventService = eventService;
        this.userService = userService;
    }

    public DestinationSpec getDestinationSpec() {
        return destinationSpec;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

        doTry(
                "Create event types for VAL",
                this::createEventTypes,
                logger
        );
        doTry(
                "Create validation queue",
                this::createDestinationAndSubscriber,
                logger
        );
        doTry(
                "Create validation user",
                this::createValidationUser,
                logger
        );

    }

    private void createValidationUser() {
        User validationUser = userService.createUser(ValidationServiceImpl.VALIDATION_USER, ValidationServiceImpl.VALIDATION_USER);
        Optional<Group> batchExecutorRole = userService.findGroup(UserService.BATCH_EXECUTOR_ROLE);
        if (batchExecutorRole.isPresent()) {
            validationUser.join(batchExecutorRole.get());
        }
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(SUBSCRIBER_NAME);
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
