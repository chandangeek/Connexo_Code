package com.energyict.mdc.device.alarms.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.database.CreateDeviceAlarmViewOperation;
import com.energyict.mdc.device.alarms.impl.event.DeviceAlarmEventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

public class Installer implements FullInstaller, PrivilegesProvider {
    private static final Logger LOG = Logger.getLogger("DeviceAlarmIssueInstaller");

    private final MessageService messageService;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;

    @Inject
    public Installer(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, MessageService messageService, EventService eventService, UserService userService) {
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        userService.addModulePrivileges(this);
        run(() -> new CreateDeviceAlarmViewOperation(dataModel).execute(), "database schema. Execute command 'ddl " + DeviceAlarmService.COMPONENT_NAME + "' and apply the sql script manually", logger);
        run(this::setAQSubscriber, "aq subscribers", logger);
        run(() -> {
            IssueType issueType = setSupportedIssueType();
            setDeviceAlarmReasons(issueType);
        }, "issue reasons and action types", logger);
        run(this::publishEvents, "event publishing", logger);
    }

    @Override
    public String getModuleName() {
        return DeviceAlarmService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(DeviceAlarmService.COMPONENT_NAME, Privileges.RESOURCE_ALARMS.getKey(), Privileges.RESOURCE_ALARMS_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_ALARM, Privileges.Constants.COMMENT_ALARM,
                        Privileges.Constants.CLOSE_ALARM, Privileges.Constants.ASSIGN_ALARM,
                        Privileges.Constants.ACTION_ALARM
                )));
        resources.add(userService.createModuleResourceWithPrivileges(DeviceAlarmService.COMPONENT_NAME, Privileges.RESOURCE_ALARMS_CONFIGURATION.getKey(), Privileges.RESOURCE_ALARMS_CONFIGURATION_DESCRIPTION
                        .getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_ALARM_CREATION_RULE,
                        Privileges.Constants.ADMINISTRATE_ALARM_CREATION_RULE, Privileges.Constants.VIEW_ALARM_ASSIGNMENT_RULE
                )));
        return resources;
    }

    private void publishEvents() {
        Set<EventType> eventTypesToPublish = new HashSet<>();
        eventService.getEventType("com/elster/jupiter/metering/enddeviceevent/CREATED")
                .ifPresent(eventTypesToPublish::add);
        for (DeviceAlarmEventDescription deviceAlarmEventDescription : DeviceAlarmEventDescription.values()) {
            eventService.getEventType(deviceAlarmEventDescription.getTopic()).ifPresent(eventTypesToPublish::add);
        }
        for (EventType eventType : eventTypesToPublish) {
            eventType.setPublish(true);
            eventType.update();
        }
    }

    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(DeviceAlarmService.DEVICE_ALARM, TranslationKeys.ISSUE_TYPE_DEVICE_ALARM, DeviceAlarmService.DEVICE_ALARM_PREFIX);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            destinationSpec.subscribe(
                    TranslationKeys.AQ_DEVICE_ALARM_EVENT_SUBSC,
                    DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN,
                    whereCorrelationId().isEqualTo("com/energyict/mdc/device/data/device/CREATED"));

        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
    }

    private void setDeviceAlarmReasons(IssueType issueType) {
        //TODO - reasons to be input by hand by user in UI
       /* issueService.createReason(ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE, issueType,
                TranslationKeys.ISSUE_REASON_UNKNOWN_INBOUND_DEVICE, TranslationKeys.ISSUE_REASON_DESCRIPTION_UNKNOWN_INBOUND_DEVICE); */

    }

    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }

}