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
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.kpi.DataValidationKpiCalculatorHandlerFactory;
import com.elster.jupiter.validation.security.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class InstallerImpl implements FullInstaller, PrivilegesProvider {
    public static final String DESTINATION_NAME = ValidationServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = ValidationServiceImpl.SUBSCRIBER_NAME;
    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

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
                this::createMessageHandlers,
                logger
        );
        doTry(
                "Create validation user",
                this::createValidationUser,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return ValidationService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(ValidationService.COMPONENTNAME, Privileges.RESOURCE_VALIDATION.getKey(), Privileges.RESOURCE_VALIDATION_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                        Privileges.Constants.VALIDATE_MANUAL, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,
                        Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION)));
        return resources;
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
    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, DataValidationKpiCalculatorHandlerFactory.TASK_DESTINATION, DataValidationKpiCalculatorHandlerFactory.TASK_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, DESTINATION_NAME, SUBSCRIBER_NAME);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, String subscriberName) {
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
