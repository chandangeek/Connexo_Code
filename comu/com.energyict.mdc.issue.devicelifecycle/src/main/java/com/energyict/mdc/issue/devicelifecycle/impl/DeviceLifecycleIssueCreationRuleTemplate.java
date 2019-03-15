/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.DeviceLifeCycleTransitionPropertyFactory;
import com.elster.jupiter.properties.rest.RaiseEventUrgencyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.issue.devicelifecycle.impl.DeviceLifecycleIssueCreationRuleTemplate",
        property = {"name=" + DeviceLifecycleIssueCreationRuleTemplate.NAME},
        service = CreationRuleTemplate.class, immediate = true)
public class DeviceLifecycleIssueCreationRuleTemplate implements CreationRuleTemplate {
    private static final Logger LOG = Logger.getLogger(DeviceLifecycleIssueCreationRuleTemplate.class.getName());
    static final String NAME = "DeviceLifecycleIssueCreationRuleTemplate";
    public static final String LOG_ON_SAME_ISSUE = NAME + ".logOnSameIssue";
    public static final String AUTORESOLUTION = NAME + ".autoresolution";
    public static final String DEVICE_LIFECYCLE_TRANSITION_PROPS = NAME + ".deviceLifecycleTransitionProps";
    private static final String SEPARATOR = ":";
    private static final String DASH_SEPARATOR = "-";

    private volatile IssueDeviceLifecycleService issueDeviceLifecycleService;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    private List<DeviceLifeCycleTransitionPropsInfo> deviceLifeCycleProps = new ArrayList<>();

    //for OSGI
    public DeviceLifecycleIssueCreationRuleTemplate() {
    }

    @Inject
    public DeviceLifecycleIssueCreationRuleTemplate(IssueDeviceLifecycleService issueDeviceLifecycleIssueService, IssueService issueService,
                                                    NlsService nlsService, PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this();
        setIssueDeviceLifecycleService(issueDeviceLifecycleIssueService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setDeviceConfigurationService(deviceConfigurationService);
    }


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return TranslationKeys.DEVICE_LIFECYCLE_ISSUE_RULE_TEMPLATE_NAME.getTranslated(thesaurus);
    }

    @Override
    public String getDescription() {
        return TranslationKeys.DEVICE_LIFECYCLE_ISSUE_RULE_TEMPLATE_DESCRIPTION.getTranslated(thesaurus);
    }

    @Override
    public Optional<CreationRule> getCreationRuleWhichUsesDeviceType(Long deviceTypeId)
    {
        List<CreationRule> issueCreationRules = DeviceLifecycleIssueUtil.getIssueCreationRules(issueService);

        for (CreationRule issueCreationRule:issueCreationRules) {
            if(((List)(issueCreationRule.getProperties().get(DeviceLifecycleIssueCreationRuleTemplate.DEVICE_LIFECYCLE_TRANSITION_PROPS)))
                    .stream()
                    .filter(propertySpec -> ((DeviceLifeCycleTransitionPropsInfo)propertySpec).getDeviceType().getId() == deviceTypeId)
                    .findFirst().isPresent())
                return Optional.of(issueCreationRule);
        }
        return Optional.empty();
    }

    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.devicelifecycle\n" +
                "import com.energyict.mdc.issue.devicelifecycle.impl.event.TransitionFailureEvent;\n" +
                "import com.energyict.mdc.issue.devicelifecycle.impl.event.TransitionRemovedEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Device lifecycle issue rule @{ruleId} with log on same issue\"\n" +
                "when\n" +
                "\tevent : TransitionFailureEvent(resolveEvent == false)\n" +
                "\teval( event.logOnSameIssue(\"@{" + LOG_ON_SAME_ISSUE + "}\") == true )\n" +
                "\teval( event.checkConditions(@{ruleId}, \"@{" + DEVICE_LIFECYCLE_TRANSITION_PROPS + "}\") == true )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by devicelifecycle rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processAlarmCreationEvent(@{ruleId}, event, true);\n" +
                "end\n" +

                "rule \"Device lifecycle issue rule @{ruleId} without log on same issue\"\n" +
                "when\n" +
                "\tevent : TransitionFailureEvent(resolveEvent == false)\n" +
                "\teval( event.logOnSameIssue(\"@{" + LOG_ON_SAME_ISSUE + "}\") == false )\n" +
                "\teval( event.checkConditions(@{ruleId}, \"@{" + DEVICE_LIFECYCLE_TRANSITION_PROPS + "}\") == true )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by devicelifecycle rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processAlarmCreationEvent(@{ruleId}, event, false);\n" +
                "end\n" +

                "\n" +
                "rule \"Autoresolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent : TransitionRemovedEvent(resolveEvent == true, @{" + AUTORESOLUTION + "} == 1 )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve issue by devicelifecycle rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
                "end\n";
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueDeviceLifecycleService.COMPONENT_NAME, Layer.DOMAIN)
            .join(nlsService.getThesaurus(DeviceLifeCycleConfigurationService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueDeviceLifecycleService(IssueDeviceLifecycleService issueDeviceLifecycleService) {
        this.issueDeviceLifecycleService = issueDeviceLifecycleService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }




    public void clearAndRecalculateCache() {
        deviceLifeCycleProps.clear();
        deviceConfigurationService.findAllDeviceTypes()
                .find().stream()
                .sorted(Comparator.comparing(DeviceType::getId))
                .forEach(deviceType -> deviceType.getDeviceLifeCycle().getFiniteStateMachine().getTransitions().forEach(stateTransition ->
                        deviceLifeCycleProps.add(
                                new DeviceLifeCycleTransitionPropsInfo(deviceType,
                                        deviceType.getDeviceLifeCycle(),
                                        stateTransition,
                                        stateTransition.getFrom(),
                                        stateTransition.getTo(),
                                        deviceLifeCycleConfigurationService, thesaurus)))
                );

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        HashMap<Long, String> possibleActionValues = getPossibleValues();

        RecurrenceSelectionInfo[] possibleValues = possibleActionValues.entrySet().stream()
                .map(entry -> new RecurrenceSelectionInfo(entry.getKey(), entry.getValue()))
                .toArray(RecurrenceSelectionInfo[]::new);
        clearAndRecalculateCache();
        DeviceLifeCycleTransitionPropsInfo[] deviceLifeCyclePropsPossibleValues = deviceLifeCycleProps.stream().toArray(DeviceLifeCycleTransitionPropsInfo[]::new);

        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(propertySpecService
                .specForValuesOf(new RecurrenceSelectionInfoValueFactory())
                .named(LOG_ON_SAME_ISSUE, TranslationKeys.ISSUE_CREATION_SELECTION_ON_RECURRENCE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .setDefaultValue(new RecurrenceSelectionInfo(1L, thesaurus.getFormat(TranslationKeys.CREATE_NEW_DEVICELIFECYCLE_ISSUE).format()))
                .addValues(possibleValues)
                .finish());
        builder.add(propertySpecService
                .booleanSpec()
                .named(AUTORESOLUTION, TranslationKeys.PARAMETER_AUTO_RESOLUTION)
                .fromThesaurus(thesaurus)
                .setDefaultValue(true)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new DeviceLifeCycleTransitionPropsInfoValueFactory())
                .named(DEVICE_LIFECYCLE_TRANSITION_PROPS, TranslationKeys.DEVICE_LIFECYCLE_TRANSITION_PROPS)
                .fromThesaurus(thesaurus)
                .markRequired()
                .markMultiValued(";")
                .addValues(deviceLifeCyclePropsPossibleValues)
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        return builder.build();
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueDeviceLifecycleService.ISSUE_TYPE_NAME).get();
    }

    @Override
    public OpenIssueDeviceLifecycle createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        return issueDeviceLifecycleService.createIssue(baseIssue, issueEvent);
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssueDeviceLifecycle issueDeviceLifecycle = (OpenIssueDeviceLifecycle) issue.get();
            event.apply(issueDeviceLifecycle);
            if (issueDeviceLifecycle.getFailedTransitions().isEmpty()) {
                return Optional.of(issueDeviceLifecycle.close(issueService.findStatus(IssueStatus.RESOLVED).get()));
            } else {
                issueDeviceLifecycle.update();
                return Optional.of(issueDeviceLifecycle);
            }
        }
        return issue;
    }

    private HashMap<Long, String> getPossibleValues() {
        return new HashMap<Long, String>() {{
            put(0L, thesaurus.getFormat(TranslationKeys.CREATE_NEW_DEVICELIFECYCLE_ISSUE).format());
            put(1L, thesaurus.getFormat(TranslationKeys.LOG_ON_EXISTING_DEVICELIFECYCLE_ISSUE).format());
        }};
    }

    @XmlRootElement
    private class RecurrenceSelectionInfo extends HasIdAndName {

        private Long id;
        private String name;


        public RecurrenceSelectionInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }


        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        private boolean hasIncreaseUrgency() {
            return id == 1L;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            RecurrenceSelectionInfo that = (RecurrenceSelectionInfo) o;

            if (!id.equals(that.id)) {
                return false;
            }
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + id.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

    }

    private class RecurrenceSelectionInfoValueFactory implements ValueFactory<HasIdAndName>, RaiseEventUrgencyFactory {
        @Override
        public RecurrenceSelectionInfo fromStringValue(String stringValue) {
            return new RecurrenceSelectionInfo(Long.parseLong(stringValue), getPossibleValues().get(Long.parseLong(stringValue)));
        }

        @Override
        public String toStringValue(HasIdAndName object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<HasIdAndName> getValueType() {
            return HasIdAndName.class;
        }

        @Override
        public HasIdAndName valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(HasIdAndName object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, HasIdAndName value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, HasIdAndName value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    private class DeviceLifeCycleTransitionPropsInfoValueFactory implements ValueFactory<HasIdAndName>, DeviceLifeCycleTransitionPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            List<String> values = Arrays.asList(stringValue.split(SEPARATOR));
            if (values.size() != 4) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS,
                        "properties." + DEVICE_LIFECYCLE_TRANSITION_PROPS,
                        String.valueOf(4),
                        String.valueOf(values.size()));
            }
            long deviceTypeId = Long.parseLong(values.get(0));
            long deviceLifecycleId = Long.parseLong(values.get(1));
            long stateTransitionId = Long.parseLong(values.get(2));
            String[] stateSwitch = String.valueOf(values.get(3)).split(DASH_SEPARATOR);
            if (stateSwitch.length != 2) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS,
                        "properties." + DEVICE_LIFECYCLE_TRANSITION_PROPS,
                        String.valueOf(2),
                        String.valueOf(values.size()));
            }
            long fromStateId = Long.parseLong(stateSwitch[0]);
            long toStateId = Long.parseLong(stateSwitch[1]);


            DeviceType deviceType = deviceConfigurationService
                    .findDeviceType(deviceTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Devicetype with id " + deviceTypeId + " does not exist"));
            DeviceLifeCycle deviceLifecycle = deviceType.getDeviceLifeCycle();
            if (!(deviceType.getDeviceLifeCycle().getId() == deviceLifecycleId)) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT,
                        "properties." + DEVICE_LIFECYCLE_TRANSITION_PROPS,
                        values.get(1));
            }

            StateTransition stateTransition = deviceLifecycle.getFiniteStateMachine().getTransitions().stream().filter(t -> t.getId() == stateTransitionId).findFirst()
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT,
                            "properties." + DEVICE_LIFECYCLE_TRANSITION_PROPS,
                            values.get(2)));
            State fromState = stateTransition.getFrom();
            State toState = stateTransition.getTo();

            if (fromState.getId() != fromStateId || toState.getId() != toStateId) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT,
                        "properties." + DEVICE_LIFECYCLE_TRANSITION_PROPS,
                        values.get(4));
            }


            return new DeviceLifeCycleTransitionPropsInfo(deviceType, deviceLifecycle, stateTransition, fromState, toState, deviceLifeCycleConfigurationService, thesaurus);
        }

        @Override
        public String toStringValue(HasIdAndName object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<HasIdAndName> getValueType() {
            return HasIdAndName.class;
        }

        @Override
        public HasIdAndName valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(HasIdAndName object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, HasIdAndName value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, HasIdAndName value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    public static class DeviceLifeCycleTransitionPropsInfo extends HasIdAndName {

        private DeviceType deviceType;
        private DeviceLifeCycle deviceLifeCycle;
        private StateTransition stateTransition;
        private State fromState;
        private State toState;
        private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
        private Thesaurus thesaurus;

        DeviceLifeCycleTransitionPropsInfo(DeviceType deviceType, DeviceLifeCycle deviceLifeCycle, StateTransition stateTransition, State from, State to,
                                           DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, Thesaurus thesaurus) {
            this.deviceType = deviceType;
            this.deviceLifeCycle = deviceLifeCycle;
            this.stateTransition = stateTransition;
            this.fromState = from;
            this.toState = to;
            this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
            this.thesaurus = thesaurus;
        }


        @Override
        public String getId() {
            return deviceType.getId() + SEPARATOR + deviceLifeCycle.getId() + SEPARATOR + stateTransition.getId() + SEPARATOR + fromState.getId() + DASH_SEPARATOR + toState.getId();
        }

        public long getDeviceLifecycleId() {
            return deviceLifeCycle.getId();
        }

        public long getStateTransitionId() {
            return stateTransition.getId();
        }

        @Override
        public String getName() {
            try {

                JSONObject jsonObj = new JSONObject();
                jsonObj.put("deviceTypeName", deviceType.getName());
                jsonObj.put("deviceLifeCycleName", deviceLifeCycle.getName());
                jsonObj.put("stateTransitionName", stateTransition.getName(thesaurus));
                jsonObj.put("fromStateName", getStateName(fromState));
                jsonObj.put("toStateName", getStateName(toState));
                return jsonObj.toString();
            } catch (JSONException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
            return "";
        }

        protected DeviceType getDeviceType() {
            return deviceType;
        }

        private String getStateName(State state) {
            return DefaultState
                    .from(state)
                    .map(deviceLifeCycleConfigurationService::getDisplayName)
                    .orElseGet(state::getName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DeviceLifeCycleTransitionPropsInfo)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DeviceLifeCycleTransitionPropsInfo that = (DeviceLifeCycleTransitionPropsInfo) o;

            if (!getDeviceType().equals(that.getDeviceType())) {
                return false;
            }
            if (!deviceLifeCycle.equals(that.deviceLifeCycle)) {
                return false;
            }
            if (!stateTransition.equals(that.stateTransition)) {
                return false;
            }
            if (!fromState.equals(that.fromState)) {
                return false;
            }
            if (!toState.equals(that.toState)) {
                return false;
            }
            return deviceLifeCycleConfigurationService.equals(that.deviceLifeCycleConfigurationService);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + getDeviceType().hashCode();
            result = 31 * result + deviceLifeCycle.hashCode();
            result = 31 * result + stateTransition.hashCode();
            result = 31 * result + fromState.hashCode();
            result = 31 * result + toState.hashCode();
            result = 31 * result + deviceLifeCycleConfigurationService.hashCode();
            return result;
        }
    }
}
