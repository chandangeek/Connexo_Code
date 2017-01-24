package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.properties.HasIdAndName;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class IssueRuleBuilder extends com.elster.jupiter.demo.impl.builders.NamedBuilder<CreationRule, IssueRuleBuilder> {

    public static final String BASIC_DATA_COLLECTION_RULE_TEMPLATE = "BasicDataCollectionRuleTemplate";
    public static final String BASIC_DATA_VALIDATION_RULE_TEMPLATE = "DataValidationIssueCreationRuleTemplate";
    public static final String BASIC_DEVICE_ALARM_RULE_TEMPLATE = "BasicDeviceAlarmRuleTemplate";

    private final IssueCreationService issueCreationService;
    private final IssueService issueService;
    private final DeviceConfigurationService deviceConfigurationService;

    private String type;
    private String reason;
    private String ruleTemplate;
    private DueInType dueInType = null;
    private Priority priority;

    @Inject
    public IssueRuleBuilder(IssueCreationService issueCreationService, IssueService issueService, DeviceConfigurationService deviceConfigurationService) {
        super(IssueRuleBuilder.class);
        this.issueCreationService = issueCreationService;
        this.issueService = issueService;
        this.deviceConfigurationService = deviceConfigurationService;
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

    public IssueRuleBuilder withPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public Optional<CreationRule> find() {
        return issueCreationService.getCreationRuleQuery().select(where("name").isEqualTo(getName())).stream().findFirst();
    }

    @Override
    public CreationRule create() {
        Log.write(this);
        CreationRuleBuilder builder = issueCreationService.newCreationRule();
        builder.setName(getName());
        builder.setIssueType(getReasonForRule().getIssueType());
        builder.setReason(getReasonForRule());

        if (this.dueInType == null) {
            builder.setDueInTime(DueInType.WEEK, 1);
        } else {
            builder.setDueInTime(dueInType, 1);
        }
        if (this.priority == null) {
            builder.setPriority(Priority.DEFAULT);
        } else {
            builder.setPriority(priority);
        }

        CreationRuleTemplate template = getCreationRuleTemplate();
        builder.setTemplate(template.getName());
        builder.setProperties(getProperties(template));
        return builder.complete();
    }

    private com.elster.jupiter.issue.share.entity.IssueReason getReasonForRule() {
        Optional<com.elster.jupiter.issue.share.entity.IssueReason> reasonRef = issueService.findReason(this.reason);
        if (!reasonRef.isPresent()) {
            throw new UnableToCreate("Unable to find reason with key = " + this.reason);
        }
        return reasonRef.get();
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
            List<HasIdAndName> deviceConfigurations = getAllDefaultConfigurations();
            if (!deviceConfigurations.isEmpty()) {
                properties.put(BASIC_DATA_VALIDATION_RULE_TEMPLATE + ".deviceConfigurations", deviceConfigurations);
            }
        } else if (template.getName().equals(BASIC_DEVICE_ALARM_RULE_TEMPLATE)) {
            properties.put(
                    BasicDeviceAlarmRuleTemplate.EVENTTYPE,
                    template.getPropertySpec(BasicDeviceAlarmRuleTemplate.EVENTTYPE).get().getValueFactory().fromStringValue(type));
        }
        return properties;
    }

    private List<HasIdAndName> getAllDefaultConfigurations() {
        List<HasIdAndName> listValue = new ArrayList<>();
        for (DeviceType type : this.deviceConfigurationService.findAllDeviceTypes().find()) {
            for (DeviceConfiguration configuration : type.getConfigurations()) {
                if (configuration.getName().equals(DeviceConfigurationTpl.PROSUMERS.getName()) ||
                        configuration.getName().equals(DeviceConfigurationTpl.CONSUMERS.getName()) ||
                        configuration.getName().equals(DeviceConfigurationTpl.PROSUMERS_VALIDATION_STRICT.getName())) {
                    listValue.add(new HasIdAndName() {
                        @Override
                        public Object getId() {
                            return configuration.getId();
                        }

                        @Override
                        public String getName() {
                            return configuration.getName();
                        }
                    });
                }
            }
        }
        return listValue;
    }
}