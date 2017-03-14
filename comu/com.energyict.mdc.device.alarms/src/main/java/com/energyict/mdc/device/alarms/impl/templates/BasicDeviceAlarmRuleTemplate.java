/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.templates;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.DeviceLifeCycleInDeviceTypePropertyFactory;
import com.elster.jupiter.properties.rest.EndDeviceEventTypePropertyFactory;
import com.elster.jupiter.properties.rest.RaiseEventPropertyFactory;
import com.elster.jupiter.properties.rest.RelativePeriodWithCountPropertyFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmEvent;
import com.energyict.mdc.device.alarms.event.EndDeviceEventCreatedEvent;
import com.energyict.mdc.device.alarms.impl.event.DeviceAlarmEventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.device.alarms.BasicDeviceAlarmRuleTemplate",
        property = {"name=" + BasicDeviceAlarmRuleTemplate.NAME},
        service = CreationRuleTemplate.class,
        immediate = true)
public class BasicDeviceAlarmRuleTemplate extends AbstractDeviceAlarmTemplate {
    protected static final Logger LOG = Logger.getLogger(BasicDeviceAlarmRuleTemplate.class.getName());
    static final String NAME = "BasicDeviceAlarmRuleTemplate";
    public static final String RAISE_EVENT_PROPS = NAME + ".raiseEventProps";
    public static final String TRIGGERING_EVENTS = NAME + ".triggeringEvents";
    public static final String CLEARING_EVENTS = NAME + ".clearingEvents";
    public static final String THRESHOLD = NAME + ".threshold";
    public static final String DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES = NAME + ".deviceLifecyleInDeviceTypes";
    private static final String SEPARATOR = ":";
    private static final int DEFAULT_NUMERICAL_VALUE = 0;
    private static final String EMPTY_CODE = "-1";
    private static final String RAISE_EVENT_PROPS_DEFAULT_VALUE = "0:0:0";
    private static final List<String> RAISE_EVENT_PROPS_POSSIBLE_VALUES = Arrays.asList("0:0:0", "1:0:0", "1:0:1", "1:1:0", "1:1:1");

    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile TimeService timeService;

    //for OSGI
    public BasicDeviceAlarmRuleTemplate() {
    }

    @Inject
    public BasicDeviceAlarmRuleTemplate(DeviceAlarmService deviceAlarmService, NlsService nlsService, IssueService issueService, PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, TimeService timeService) {
        this();
        setDeviceAlarmService(deviceAlarmService);
        setNlsService(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setTimeService(timeService);
        activate();
    }

    @Activate
    public void activate() {
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        super.setDeviceAlarmService(deviceAlarmService);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        super.setIssueService(issueService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public String getName() {
        return BasicDeviceAlarmRuleTemplate.NAME;
    }

    @Override
    public String getDescription() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DEVICE_ALARM_DESCRIPTION).format();
    }

    //END_DEVICE_EVENT_CREATED
    @Override
    public String getContent() {
        return "package com.energyict.mdc.device.device.alarms\n" +
                "import com.energyict.mdc.device.alarms.event.DeviceAlarmEvent;\n" +

                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Basic clearing event device alarm rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DeviceAlarmEvent( eventType == \"" + DeviceAlarmEventDescription.END_DEVICE_EVENT_CREATED.getUniqueKey() + "\" )\n" +
                "\teval( event.isClearing(@{ruleId}, \"@{" + CLEARING_EVENTS + "}\") == true )\n" +
                "then\n" +
                "\tSystem.out.println(\"Processing clearing event device alarm based on rule template number @{ruleId}\");\n" +
                "\tissueCreationService.processAlarmCreationEvent(@{ruleId}, event, true);\n" +
                "end\n" +

                "rule \"Basic triggering event device alarm rule @{ruleId} logged on same alarm\"\n" +
                "when\n" +
                "\tevent : DeviceAlarmEvent( eventType == \"" + DeviceAlarmEventDescription.END_DEVICE_EVENT_CREATED.getUniqueKey() + "\" )\n" +
                "\teval( event.logOnSameAlarm(\"@{" + RAISE_EVENT_PROPS + "}\") == true )\n" +
                "\teval( event.checkOccurrenceConditions(@{ruleId}, \"@{" + THRESHOLD + "}\", \"@{" + TRIGGERING_EVENTS + "}\") == true )\n" +
                "\teval( event.hasAssociatedDeviceLifecycleStatesInDeviceTypes(\"@{" + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES + "}\") == true )\n" +
                "then\n" +
                "\tSystem.out.println(\"Processing triggering event device alarm based on rule template number @{ruleId} logged on same alarm\");\n" +
                "\tissueCreationService.processAlarmCreationEvent(@{ruleId}, event, true);\n" +
                "end\n" +

                "rule \"Basic triggering event device alarm rule @{ruleId} create new alarm\"\n" +
                "when\n" +
                "\tevent : DeviceAlarmEvent( eventType == \"" + DeviceAlarmEventDescription.END_DEVICE_EVENT_CREATED.getUniqueKey() + "\" )\n" +
                "\teval( event.logOnSameAlarm(\"@{" + RAISE_EVENT_PROPS + "}\") == false )\n" +
                "\teval( event.checkOccurrenceConditions(@{ruleId}, \"@{" + THRESHOLD + "}\", \"@{" + TRIGGERING_EVENTS + "}\") == true )\n" +
                "\teval( event.hasAssociatedDeviceLifecycleStatesInDeviceTypes(\"@{" + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES + "}\") == true )\n" +
                "then\n" +
                "\tSystem.out.println(\"Processing triggering event device alarm based on rule template number @{ruleId} create new alarm\");\n" +
                "\tissueCreationService.processAlarmCreationEvent(@{ruleId}, event, false);\n" +
                "end";
    }

    @Override
    public void updateIssue(OpenIssue openIssue, IssueEvent event) {

        if (IssueStatus.IN_PROGRESS.equals(openIssue.getStatus().getKey())) {
            openIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).get());
        }
        getAlarmForUpdate(openIssue, event).update();
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        //TODO - resolve all occurrences
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssue openIssue = (OpenIssue) issue.get();
            issue = Optional.of(getAlarmForClosure(openIssue, event).close(issueService.findStatus(IssueStatus.RESOLVED).get()));
        }
        return issue;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        List<DeviceLifeCycleInDeviceTypeInfo> list = new ArrayList<>();
        deviceConfigurationService.findAllDeviceTypes()
                .find().stream()
                .sorted(Comparator.comparing(DeviceType::getId))
                .forEach(deviceType -> list.add(new DeviceLifeCycleInDeviceTypeInfo(deviceType, deviceType.getDeviceLifeCycle().getFiniteStateMachine().getStates().stream()
                        .sorted(Comparator.comparing(State::getId)).collect(Collectors.toList()), deviceLifeCycleConfigurationService)));
        DeviceLifeCycleInDeviceTypeInfo[] deviceLifeCycleInDeviceTypepossibleValues = list.stream().toArray(DeviceLifeCycleInDeviceTypeInfo[]::new);
        RaiseEventPropsInfo[] raiseEventPropsPossibleValues = RAISE_EVENT_PROPS_POSSIBLE_VALUES.stream()
                .map(RaiseEventPropsInfo::new)
                .toArray(RaiseEventPropsInfo[]::new);

        builder.add(propertySpecService
                .specForValuesOf(new EventTypeInfoValueFactory())
                .named(TRIGGERING_EVENTS, TranslationKeys.TRIGGERING_EVENTS)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markMultiValued(",")
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new EventTypeInfoValueFactory())
                .named(CLEARING_EVENTS, TranslationKeys.CLEARING_EVENTS)
                .fromThesaurus(this.getThesaurus())
                .markMultiValued(",")
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new DeviceLifeCycleInDeviceTypeInfoValueFactory())
                .named(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, TranslationKeys.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markMultiValued(";")
                .addValues(deviceLifeCycleInDeviceTypepossibleValues)
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new RaiseEventPropsInfoValueFactory())
                .named(RAISE_EVENT_PROPS, TranslationKeys.RAISE_EVENT_PROPS)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(new RaiseEventPropsInfo(RAISE_EVENT_PROPS_DEFAULT_VALUE))
                .addValues(raiseEventPropsPossibleValues)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new RelativePeriodWithCountInfoValueFactory())
                .named(THRESHOLD, TranslationKeys.EVENT_TEMPORAL_THRESHOLD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(new RelativePeriodWithCountInfo(DEFAULT_NUMERICAL_VALUE, timeService.getAllRelativePeriod()))
                .finish());
        return builder.build();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DEVICE_ALARM_NAME).format();
    }

    @SuppressWarnings("unchecked")
    private OpenIssue getAlarmForUpdate(OpenIssue openIssue, IssueEvent event) {
        if (openIssue instanceof OpenDeviceAlarm && event instanceof DeviceAlarmEvent) {
            OpenDeviceAlarm alarm = OpenDeviceAlarm.class.cast(openIssue);
            List<String> clearingEvents = new ArrayList<>();
            alarm.getRule().getProperties().entrySet().stream().filter(entry -> entry.getKey().equals(CLEARING_EVENTS))
                    .findFirst().ifPresent(element ->
                    ((ArrayList<EventTypeInfo>) (element.getValue())).forEach(value -> clearingEvents.add(value.getName())));
            Optional<RaiseEventPropsInfo> newEventProps = alarm.getRule().getProperties().entrySet().stream().filter(entry -> entry.getKey().equals(RAISE_EVENT_PROPS))
                    .findFirst().map(found -> (RaiseEventPropsInfo) found.getValue());

            if (!clearingEvents.isEmpty() &&
                    ((DeviceAlarmEvent) event).isClearing(clearingEvents)) {
                if (!alarm.isStatusCleared()) {
                    alarm.toggleClearedStatus();
                }
                if (newEventProps.isPresent() &&
                        newEventProps.get().hasDecreaseUrgency()) {
                    alarm.setPriority(Priority.get(alarm.getPriority().lowerUrgency(), alarm.getPriority().getImpact()));
                }
            } else {
                if (alarm.isStatusCleared()) {
                    alarm.toggleClearedStatus();
                }
                if (newEventProps.isPresent() &&
                        newEventProps.get().hasIncreaseUrgency()) {
                    alarm.setPriority(Priority.get(alarm.getPriority().increaseUrgency(), alarm.getPriority().getImpact()));
                }
            }
            alarm.addRelatedAlarmEvent(alarm.getDevice().getId(), ((EndDeviceEventCreatedEvent) event).getEventTypeMrid(), ((EndDeviceEventCreatedEvent) event).getTimestamp());
            return alarm;
        }
        return openIssue;
    }

    private OpenIssue getAlarmForClosure(OpenIssue openIssue, IssueEvent event) {
        if (openIssue instanceof OpenDeviceAlarm && event instanceof DeviceAlarmEvent) {
            return OpenDeviceAlarm.class.cast(openIssue);
        }
        return openIssue;
    }

    private class EventTypeInfoValueFactory implements ValueFactory<HasIdAndName>, EndDeviceEventTypePropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            if (stringValue.equals(EMPTY_CODE.concat(SEPARATOR).concat(EMPTY_CODE))) {
                return new EventTypeInfo(EMPTY_CODE, EMPTY_CODE);
            }
            List<String> splitEventTypeAndDeviceCode = Arrays.asList(stringValue.split(SEPARATOR));
            if (splitEventTypeAndDeviceCode.size() == 2) {
                return new EventTypeInfo(splitEventTypeAndDeviceCode.get(0), splitEventTypeAndDeviceCode.get(1));
            } else {
                return null;
            }
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

    @XmlRootElement
    static class EventTypeInfo extends HasIdAndName {

        private String eventType;
        private String deviceCode;

        EventTypeInfo(String eventType, String deviceCode) {
            this.eventType = eventType;
            this.deviceCode = deviceCode;
        }

        @Override
        public String getId() {
            return eventType.concat(SEPARATOR).concat(deviceCode);
        }

        @Override
        public String getName() {
            return eventType;
        }
    }


    private class DeviceLifeCycleInDeviceTypeInfoValueFactory implements ValueFactory<HasIdAndName>, DeviceLifeCycleInDeviceTypePropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            List<String> values = Arrays.asList(stringValue.split(SEPARATOR));
            if (values.size() != 3) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS, String.valueOf(3), String.valueOf(values.size()));
            }
            DeviceType deviceType = deviceConfigurationService
                    .findDeviceType(Long.parseLong(values.get(0)))
                    .orElse(null);
            if (!(deviceType.getDeviceLifeCycle().getId() == Long.parseLong(values.get(1)))) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT, values.get(1));
            }

            List<Long> stateIds = Arrays.stream(values.get(2)
                    .split(","))
                    .map(String::trim)
                    .mapToLong(Long::parseLong).boxed().collect(Collectors.toList());

            List<State> states = deviceLifeCycleConfigurationService
                    .findAllDeviceLifeCycles().find()
                    .stream().map(lifecycle -> lifecycle.getFiniteStateMachine().getStates())
                    .flatMap(Collection::stream)
                    .filter(stateValue -> stateIds.contains(stateValue.getId())).collect(Collectors.toList());
            return new DeviceLifeCycleInDeviceTypeInfo(deviceType, states, deviceLifeCycleConfigurationService);
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

    static class DeviceLifeCycleInDeviceTypeInfo extends HasIdAndName {

        private DeviceType deviceType;
        private List<State> states;
        private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

        DeviceLifeCycleInDeviceTypeInfo(DeviceType deviceType, List<State> states, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
            this.deviceType = deviceType;
            this.states = states;
            this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        }


        @Override
        public String getId() {
            return deviceType.getId() + SEPARATOR + deviceType.getDeviceLifeCycle().getId() + SEPARATOR + states.stream().map(HasId::getId).map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

        @Override
        public String getName() {
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("deviceTypeName", deviceType.getName());
                jsonObj.put("lifeCycleStateName", states.stream().map(state -> getStateName(state) + " (" + deviceType.getDeviceLifeCycle().getName() + ")").collect(Collectors.toList()));
                return jsonObj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
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
            if (!(o instanceof DeviceLifeCycleInDeviceTypeInfo)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DeviceLifeCycleInDeviceTypeInfo that = (DeviceLifeCycleInDeviceTypeInfo) o;

            if (!deviceType.equals(that.deviceType)) {
                return false;
            }
            return states.equals(that.states);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + deviceType.hashCode();
            result = 31 * result + states.hashCode();
            return result;
        }
    }


    private class RaiseEventPropsInfoValueFactory implements ValueFactory<HasIdAndName>, RaiseEventPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return new RaiseEventPropsInfo(stringValue);
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

    @XmlRootElement
    static class RaiseEventPropsInfo extends HasIdAndName {

        private transient String value;

        RaiseEventPropsInfo(String value) {
            this.value = value;
        }


        @Override
        public Object getId() {
            return value;
        }

        @Override
        public String getName() {
            return "Log on same alarm : Increase urgency on raised event : Decrease urgency on clearing event";
        }

        public boolean logOnSameAlarm() {
            return Integer.parseInt(Arrays.asList(value.split(SEPARATOR)).get(0)) == 1;
        }

        boolean hasIncreaseUrgency() {
            return Integer.parseInt(Arrays.asList(value.split(SEPARATOR)).get(1)) == 1;
        }

        boolean hasDecreaseUrgency() {
            return Integer.parseInt(Arrays.asList(value.split(SEPARATOR)).get(2)) == 1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RaiseEventPropsInfo)) {
                return false;
            }

            RaiseEventPropsInfo that = (RaiseEventPropsInfo) o;

            return value.equals(that.value);

        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }


    private class RelativePeriodWithCountInfoValueFactory implements ValueFactory<HasIdAndName>, RelativePeriodWithCountPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            List<String> values = Arrays.asList(stringValue.split(SEPARATOR));
            if (values.size() != 2) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS, "Relative period with occurrence count for device alarms");
            }
            int count = Integer.parseInt(values.get(0));
            RelativePeriod relativePeriod = timeService.findRelativePeriod(Long.parseLong(values.get(1))).orElse(null);
            return new RelativePeriodWithCountInfo(count, relativePeriod);
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

    static class RelativePeriodWithCountInfo extends HasIdAndName {

        private RelativePeriod relativePeriod;
        private int occurrenceCount;

        RelativePeriodWithCountInfo(int occurrenceCount, RelativePeriod relativePeriod) {
            this.relativePeriod = relativePeriod;
            this.occurrenceCount = occurrenceCount;
        }

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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RelativePeriodWithCountInfo)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            RelativePeriodWithCountInfo that = (RelativePeriodWithCountInfo) o;

            return occurrenceCount == that.occurrenceCount && relativePeriod.equals(that.relativePeriod);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + relativePeriod.hashCode();
            result = 31 * result + occurrenceCount;
            return result;
        }
    }
}