/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class IssueRuleBuilder extends com.elster.jupiter.demo.impl.builders.NamedBuilder<CreationRule, IssueRuleBuilder> {

    public static final String BASIC_DATA_COLLECTION_RULE_TEMPLATE = "BasicDataCollectionRuleTemplate";
    public static final String BASIC_DATA_VALIDATION_RULE_TEMPLATE = "DataValidationIssueCreationRuleTemplate";
    public static final String BASIC_DEVICE_ALARM_RULE_TEMPLATE = "BasicDeviceAlarmRuleTemplate";

    private static final String SEPARATOR = ":";
    private static final String WILDCARD = "*";
    private final IssueCreationService issueCreationService;
    private final IssueService issueService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final TimeService timeService;

    private String type;
    private String reason;
    private String ruleTemplate;
    private long dueInValue;
    private DueInType dueInType = null;
    private Priority priority;
    private boolean active;

    @Inject
    public IssueRuleBuilder(IssueCreationService issueCreationService, IssueService issueService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, TimeService timeService) {
        super(IssueRuleBuilder.class);
        this.issueCreationService = issueCreationService;
        this.issueService = issueService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.timeService = timeService;
    }

    public IssueRuleBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public IssueRuleBuilder withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public IssueRuleBuilder withRuleTemplate(String ruleTemplate) {
        this.ruleTemplate = ruleTemplate;
        return this;
    }

    public IssueRuleBuilder withDueInType(DueInType dueInType) {
        this.dueInType = dueInType;
        return this;
    }

    public IssueRuleBuilder withDueInValue(long dueInValue) {
        this.dueInValue = dueInValue;
        return this;
    }

    public IssueRuleBuilder withPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public IssueRuleBuilder withStatus(boolean status) {
        this.active = status;
        return this;
    }

    @Override
    public Optional<CreationRule> find() {
        return issueCreationService.getCreationRuleQuery().select(where("name").isEqualTo(getName())).stream().findFirst();
    }

    @Override
    public CreationRule create() {
        IssueType issueType = getReasonForRule().getIssueType();
        Log.write(this);
        CreationRuleBuilder builder = issueCreationService.newCreationRule();
        builder.setName(getName());
        builder = this.active ? builder.activate() : builder.deactivate();
        builder.setIssueType(issueType);
        builder.setReason(getReasonForRule());
        builder.setDueInTime(this.dueInType, this.dueInValue);
        builder.setPriority(this.priority);
        CreationRuleTemplate template = getCreationRuleTemplate();
        builder.setTemplate(template.getName());
        builder.setProperties(getProperties(template));
        CreationRule rule = builder.complete();

        if (issueType.getPrefix().equals("ALM")) {
            IssueCreationService.CreationRuleActionBuilder actionBuilder = builder.newCreationRuleAction();
            actionBuilder.setPhase(CreationRuleActionPhase.fromString("CREATE"));
            Optional<IssueActionType> actionType = issueService.getIssueActionService().findActionType(3);
            //TODO figure out why 3 is not present
            actionType.ifPresent(issueActionType -> {
                actionBuilder.setActionType(actionType.get());
                for (PropertySpec propertySpec : actionType.get().createIssueAction().get().setReasonName(issueType.getName()).getPropertySpecs()) {
                    actionBuilder.addProperty(propertySpec.getName(), propertySpec
                            .getValueFactory()
                            .fromStringValue("{\"workgroupId\":2,\"comment\":\"\",\"userId\":-1}"));
                    actionBuilder.complete();
                    rule.update();
                }
            });
        }
        return rule;
    }

    private com.elster.jupiter.issue.share.entity.IssueReason getReasonForRule() {
        return issueService.findReason(reason).isPresent() ? issueService.findReason(reason).get() :
                issueService.createReason(reason, getCreationRuleTemplate().getIssueType(), new TranslationKey() {
                    @Override
                    public String getKey() {
                        return reason;
                    }

                    @Override
                    public String getDefaultFormat() {
                        return "Alarm reason";
                    }
                }, new TranslationKey() {
                    @Override
                    public String getKey() {
                        return reason;
                    }

                    @Override
                    public String getDefaultFormat() {
                        return "Alarm reason";
                    }
                });
    }

    private CreationRuleTemplate getCreationRuleTemplate() {
        String templateName = this.ruleTemplate;
        CreationRuleTemplate template = issueCreationService.findCreationRuleTemplate(templateName).orElse(null);
        if (template == null) {
            throw new UnableToCreate("Unable to find creation rule template = " + templateName);
        }
        return template;
    }

    private Map<String, Object> getProperties(CreationRuleTemplate template) {
        Map<String, Object> properties = new HashMap<>();
        if (template.getName().equals(BASIC_DATA_COLLECTION_RULE_TEMPLATE)) {
            properties.put(
                    BasicDataCollectionRuleTemplate.EVENTTYPE,
                    template.getPropertySpec(BasicDataCollectionRuleTemplate.EVENTTYPE).get().getValueFactory().fromStringValue(type));
            properties.put(
                    BasicDataCollectionRuleTemplate.AUTORESOLUTION,
                    template.getPropertySpec(BasicDataCollectionRuleTemplate.AUTORESOLUTION).get().getValueFactory().fromStringValue("1"));

        } else if (template.getName().equals(BASIC_DATA_VALIDATION_RULE_TEMPLATE)) {
            List<HasIdAndName> deviceConfigurations = new ArrayList<>();
            deviceConfigurationService.findDeviceTypeByName("Elster A1800").get().getConfigurations()
                    .forEach(deviceConfiguration -> deviceConfigurations.add(new HasIdAndName() {
                        @Override
                        public Object getId() {
                            return deviceConfiguration.getId();
                        }

                        @Override
                        public String getName() {
                            return deviceConfiguration.getName();
                        }
                    }));

            if (!deviceConfigurations.isEmpty()) {
                properties.put(BASIC_DATA_VALIDATION_RULE_TEMPLATE + ".deviceConfigurations", deviceConfigurations);
            }
        } else if (template.getName().equals(BASIC_DEVICE_ALARM_RULE_TEMPLATE)) {
            properties.put(BasicDeviceAlarmRuleTemplate.TRIGGERING_EVENTS, getTamperingCode(BasicDeviceAlarmRuleTemplate.TRIGGERING_EVENTS));
            properties.put(BasicDeviceAlarmRuleTemplate.CLEARING_EVENTS, getTamperingCode(BasicDeviceAlarmRuleTemplate.CLEARING_EVENTS));
            properties.put(
                    BasicDeviceAlarmRuleTemplate.RAISE_EVENT_PROPS,
                    template.getPropertySpec(BasicDeviceAlarmRuleTemplate.RAISE_EVENT_PROPS).get().getValueFactory().fromStringValue("0:0:0"));
            properties.put(BasicDeviceAlarmRuleTemplate.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, getAllDeviceStatesInAllDeviceTypes());
            properties.put(
                    BasicDeviceAlarmRuleTemplate.THRESHOLD, getRelativePeriodWithCount());
        }
        return properties;
    }

    private List<HasIdAndName> getTamperingCode(String eventType) {
        String raisedOnEvent = "*.12.*.257";
        String clearingEvent = "*.12.*.291";
        if (eventType.equals(BasicDeviceAlarmRuleTemplate.TRIGGERING_EVENTS)) {
            return Collections.singletonList(new HasIdAndName() {
                @Override
                public Object getId() {
                    return raisedOnEvent + SEPARATOR + WILDCARD;
                }

                @Override
                public String getName() {
                    return "raised on event: " + raisedOnEvent;
                }
            });

        } else {
            return Collections.singletonList(new HasIdAndName() {
                @Override
                public Object getId() {
                    return clearingEvent + SEPARATOR + WILDCARD;
                }

                @Override
                public String getName() {
                    return "clearing event: " + clearingEvent;
                }
            });
        }
    }

    private List<HasIdAndName> getAllDeviceStatesInAllDeviceTypes() {
        List<HasIdAndName> list = new ArrayList<>();
        deviceConfigurationService.findAllDeviceTypes()
                .find().stream()
                .sorted(Comparator.comparing(DeviceType::getId))
                .forEach(deviceType ->
                        list.add(new HasIdAndName() {
                                     @Override
                                     public String getId() {
                                         return deviceType.getId() + SEPARATOR + deviceType.getDeviceLifeCycle().getId() + SEPARATOR + deviceType.getDeviceLifeCycle()
                                                 .getFiniteStateMachine()
                                                 .getStates()
                                                 .stream()
                                                 .sorted(Comparator.comparing(State::getId))
                                                 .map(HasId::getId)
                                                 .map(String::valueOf)
                                                 .collect(Collectors.joining(","));
                                     }

                                     @Override
                                     public String getName() {
                                         try {
                                             JSONObject jsonObj = new JSONObject();
                                             jsonObj.put("deviceTypeName", deviceType.getName());
                                             jsonObj.put("lifeCycleStateName", deviceType.getDeviceLifeCycle().getFiniteStateMachine().getStates().stream()
                                                     .sorted(Comparator.comparing(State::getId)).collect(Collectors.collectingAndThen(Collectors.toList(), Collection::stream))
                                                     .map(state -> getStateName(state) + " (" + deviceType.getDeviceLifeCycle().getName() + ")").collect(Collectors.toList()));
                                             return jsonObj.toString();
                                         } catch (JSONException e) {
                                             e.printStackTrace();
                                         }
                                         return "";
                                     }
                                 }
                        ));
        return list;
    }

    private HasIdAndName getRelativePeriodWithCount() {
        RelativePeriod relativePeriod = timeService.findRelativePeriodByName("Last 7 days").isPresent() ? timeService.findRelativePeriodByName("Last 7 days")
                .get() : timeService.getAllRelativePeriod();
        int occurrenceCount = new Random().nextInt(5);

        return new HasIdAndName() {
            @Override
            public String getId() {
                return occurrenceCount + SEPARATOR + relativePeriod.getId();
            }

            @Override
            public String getName() {
                try {
                    JSONObject jsonId = new JSONObject();
                    jsonId.put("occurrenceCount", occurrenceCount);
                    jsonId.put("relativePeriod", relativePeriod.getName());
                    return jsonId.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return "";
            }
        };
    }

    private String getStateName(State state) {
        return DefaultState
                .from(state)
                .map(deviceLifeCycleConfigurationService::getDisplayName)
                .orElseGet(state::getName);
    }
}
