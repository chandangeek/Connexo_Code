package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;
import com.elster.jupiter.validation.impl.kpi.DataValidationKpiCalculatorHandlerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerImpl {
    public static final String DESTINATION_NAME = ValidationServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = ValidationServiceImpl.SUBSCRIBER_NAME;
    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private DestinationSpec destinationSpec;
    private final UserService userService;

    public InstallerImpl(DataModel dataModel, EventService eventService, MessageService messageService, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.eventService = eventService;
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
        ExceptionCatcher.executing(
                this::createEventTypes,
                this::createMessageHandlers,
                this::createValidationUser
        ).andHandleExceptionsWith(exception -> LOGGER.log(Level.SEVERE, exception.getMessage(), exception))
                .execute();
    }

    private void createValidationUser() {
        User validationUser = userService.createUser(ValidationServiceImpl.VALIDATION_USER, ValidationServiceImpl.VALIDATION_USER);
        Optional<Group> batchExecutorRole = userService.findGroup(UserService.BATCH_EXECUTOR_ROLE);
        if (batchExecutorRole.isPresent()) {
            validationUser.join(batchExecutorRole.get());
        } else {
            LOGGER.log(Level.SEVERE, "Could not add role to '" + ValidationServiceImpl.VALIDATION_USER + "' user because role '" + UserService.BATCH_EXECUTOR_ROLE + "' is not found");
        }
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
    private void createMessageHandlers() {
        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            this.createMessageHandler(defaultQueueTableSpec, DataValidationKpiCalculatorHandlerFactory.TASK_DESTINATION, DataValidationKpiCalculatorHandlerFactory.TASK_SUBSCRIBER);
            this.createMessageHandler(defaultQueueTableSpec, DESTINATION_NAME, SUBSCRIBER_NAME);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, String subscriberName) {
        try {
            Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
            if (!destinationSpecOptional.isPresent()) {
                DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
                queue.activate();
                queue.subscribe(subscriberName);
            } else {
                boolean notSubscribedYet = !destinationSpecOptional.get().getSubscribers().stream().anyMatch(spec -> spec.getName().equals(subscriberName));
                if (notSubscribedYet) {
                    destinationSpecOptional.get().activate();
                    destinationSpecOptional.get().subscribe(subscriberName);
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
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
