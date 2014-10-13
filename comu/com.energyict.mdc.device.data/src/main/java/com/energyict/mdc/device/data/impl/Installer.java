package com.energyict.mdc.device.data.impl;

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
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.security.Privileges;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Installer for the Device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer {

    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_NAME = "COMSCHED_RECALCULATOR";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME = "COMSCHED_BATCH_OBSOLETE";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final int KPI_CALCULATOR_TASK_RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;
    private final MessageService messageService;
    private final UserService userService;
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    public Installer(DataModel dataModel, EventService eventService, Thesaurus thesaurus, MessageService messageService, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.messageService = messageService;
        this.userService = userService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        try {
            this.createPrivileges();
            this.assignPrivilegesToDefaultRoles();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        this.createEventTypes();
        this.createTranslations();
        this.createMessageHandlers();
        this.createMasterData();
        this.createKpiCalculatorDestination();
    }

    private void createKpiCalculatorDestination() {
        DestinationSpec destination =
                this.messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get().
                        createDestinationSpec(
                                DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION,
                                KPI_CALCULATOR_TASK_RETRY_DELAY);
        destination.activate();
        destination.subscribe(DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER);
    }

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("MDC", "device.devices", "device.devices.description", new String[]{Privileges.ADMINISTRATE_DEVICE, Privileges.VIEW_DEVICE, Privileges.VALIDATE_DEVICE, Privileges.SCHEDULE_DEVICE});
        this.userService.createResourceWithPrivileges("MDC", "inventoryManagement.inventoryManagements", "inventoryManagement.inventoryManagements.description", new String[]{Privileges.IMPORT_INVENTORY_MANAGEMENT, Privileges.REVOKE_INVENTORY_MANAGEMENT, Privileges.CREATE_INVENTORY_MANAGEMENT});
        this.userService.createResourceWithPrivileges("MDC", "deviceSecurity.deviceSecurities", "deviceSecurity.deviceSecurities.description", new String[]{Privileges.ADMINISTRATE_DEVICE_SECURITY, Privileges.VIEW_DEVICE_SECURITY});
    }

    private void assignPrivilegesToDefaultRoles() {
        this.userService.grantGroupWithPrivilege(UserService.DEFAULT_METER_EXPERT_ROLE, new String[]{
                Privileges.ADMINISTRATE_DEVICE, Privileges.VIEW_DEVICE, Privileges.VALIDATE_DEVICE, Privileges.SCHEDULE_DEVICE,
                Privileges.IMPORT_INVENTORY_MANAGEMENT, Privileges.REVOKE_INVENTORY_MANAGEMENT, Privileges.CREATE_INVENTORY_MANAGEMENT,
                Privileges.ADMINISTRATE_DEVICE_SECURITY, Privileges.VIEW_DEVICE_SECURITY
        });
        this.userService.grantGroupWithPrivilege(UserService.DEFAULT_METER_OPERATOR_ROLE, new String[]{Privileges.VIEW_DEVICE});
    }

    private void createMessageHandlers() {
        try {
            this.createMessageHandler(COMSCHEDULE_RECALCULATOR_MESSAGING_NAME);
            this.createMessageHandler(COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void createMessageHandler(String messagingName) {
        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(messagingName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
            destinationSpec.subscribe(messagingName);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            try {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
            } catch (Exception e) {
                this.logger.severe(e.getMessage());
            }
        }
        this.thesaurus.addTranslations(translations);
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
    }

    private void createMasterData() {
        // No master data so far
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.createIfNotExists(this.eventService);
            } catch (Exception e) {
                this.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private static class SimpleTranslation implements Translation {

        private final SimpleNlsKey nlsKey;
        private final Locale locale;
        private final String translation;

        private SimpleTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
            this.nlsKey = nlsKey;
            this.locale = locale;
            this.translation = translation;
        }

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
    }

}