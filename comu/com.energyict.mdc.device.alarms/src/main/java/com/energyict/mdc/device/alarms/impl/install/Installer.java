/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
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
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmActionsFactory;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.actions.*;
import com.energyict.mdc.device.alarms.impl.database.CreateDeviceAlarmViewOperation;
import com.energyict.mdc.device.alarms.impl.event.DeviceAlarmEventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.LAST_7_DAYS;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_YEAR;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.TODAY;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.YESTERDAY;

public class Installer implements FullInstaller, PrivilegesProvider {

    private final MessageService messageService;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;
    private final TimeService timeService;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public Installer(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, MessageService messageService, EventService eventService, UserService userService, TimeService timeService, EndPointConfigurationService endPointConfigurationService) {
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
        this.timeService = timeService;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        userService.addModulePrivileges(this);
        run(() -> new CreateDeviceAlarmViewOperation(dataModel).execute(), "database schema. Execute command 'ddl " + DeviceAlarmService.COMPONENT_NAME + "' and apply the sql script manually", logger);
        run(this::setAQSubscribers, "aq subscribers", logger);
        run(() -> {
            IssueType issueType = setSupportedIssueType();
            setDefaultDeviceAlarmActions(issueType);
        }, "issue action types", logger);
        run(this::createRelativePeriodCategory, "create alarm relative period category", logger);
        run(this::createRelativePeriods, "Assign default relative periods to DAL category", logger);
        run(this::publishEvents, "publishing events", logger);
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
        Stream.of(DeviceAlarmEventDescription.values()).findFirst()
                .map(deviceAlarmEventDescription -> eventService.getEventType(deviceAlarmEventDescription.getTopic()))
                .ifPresent(eventType -> {
                    eventType.get().setPublish(true);
                    eventType.get().update();

                });
    }

    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(DeviceAlarmService.DEVICE_ALARM, TranslationKeys.ISSUE_TYPE_DEVICE_ALARM, DeviceAlarmService.DEVICE_ALARM_PREFIX);
    }

    private void setAQSubscribers() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            destinationSpec.subscribe(
                    TranslationKeys.AQ_DEVICE_ALARM_EVENT_SUBSC,
                    DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN,
                    whereCorrelationId().isEqualTo("com/elster/jupiter/metering/enddeviceevent/CREATED"));
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }

        try {
            destinationSpec.subscribe(
                    TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC,
                    DeviceAlarmService.COMPONENT_NAME,
                    Layer.DOMAIN,
                    whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/CREATED")
                            .or(whereCorrelationId().isEqualTo("com/elster/jupiter/metering/enddeviceevent/CREATED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/DELETED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/dlc/UPDATED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/lifecycle/config/dlc/update"))
                            .or(whereCorrelationId().isEqualTo("com/elster/jupiter/fsm/UPDATED")));
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
    }

    private void setDefaultDeviceAlarmActions(IssueType issueType) {
        IssueType deviceAlarmType = issueService.findIssueType(DeviceAlarmService.DEVICE_ALARM).get();
        issueActionService.createActionType(DeviceAlarmActionsFactory.ID, AssignDeviceAlarmAction.class.getName(), deviceAlarmType, null);
        issueActionService.createActionType(DeviceAlarmActionsFactory.ID, MailNotificationAlarmAction.class.getName(), deviceAlarmType,null);
        issueActionService.createActionType(DeviceAlarmActionsFactory.ID, StartProcessAlarmAction.class.getName(), deviceAlarmType, null);
        issueActionService.createActionType(DeviceAlarmActionsFactory.ID, CloseDeviceAlarmAction.class.getName(), deviceAlarmType, CreationRuleActionPhase.NOT_APPLICABLE);
        issueActionService.createActionType(DeviceAlarmActionsFactory.ID, WebServiceNotificationAlarmAction.class.getName(), deviceAlarmType, CreationRuleActionPhase.CREATE);
    }

    private void createRelativePeriodCategory() {
        timeService.createRelativePeriodCategory(ModuleConstants.ALARM_RELATIVE_PERIOD_CATEGORY);
    }

    private void createRelativePeriods() {
        RelativePeriodCategory category = getCategory();

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, THIS_YEAR, TODAY, YESTERDAY)
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName())
                            .orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(category);
                });
    }

    private RelativePeriodCategory getCategory() {
        return timeService.findRelativePeriodCategoryByName(ModuleConstants.ALARM_RELATIVE_PERIOD_CATEGORY)
                .orElseThrow(IllegalArgumentException::new);
    }

    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }

}