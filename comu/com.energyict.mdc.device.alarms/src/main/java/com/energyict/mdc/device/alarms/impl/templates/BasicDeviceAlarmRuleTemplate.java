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
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
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
import com.elster.jupiter.properties.rest.RelativePeriodWithCountFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
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

@Component(name = "com.energyict.mdc.device.alarms.BasicDeviceAlarmRuleTemplate",
        property = {"name=" + BasicDeviceAlarmRuleTemplate.NAME},
        service = CreationRuleTemplate.class,
        immediate = true)
public class BasicDeviceAlarmRuleTemplate extends AbstractDeviceAlarmTemplate {
    protected static final Logger LOG = Logger.getLogger(BasicDeviceAlarmRuleTemplate.class.getName());
    static final String NAME = "BasicDeviceAlarmRuleTemplate";
    public static final String EVENTTYPE = NAME + ".eventType";
    public static final String RAISE_EVENT_PROPS = NAME + ".raiseEventProps";
    public static final String TRIGGERING_EVENTS = NAME + ".triggeringEvents";
    public static final String CLEARING_EVENTS = NAME + ".clearingEvents";
    public static final String THRESHOLD = NAME + ".threshold";
    public static final String DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES = NAME + ".deviceLifecyleInDeviceTypes";
    private static final String SEPARATOR = ":";
    private static final int DEFAULT_NUMERICAL_VALUE = 0;
    private static final String RAISE_EVENT_PROPS_DEFAULT_VALUE = "0:0:0";

    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile TimeService timeService;
    private volatile MeteringService meteringService;

    //for OSGI
    public BasicDeviceAlarmRuleTemplate() {
    }

    @Inject
    public BasicDeviceAlarmRuleTemplate(DeviceAlarmService deviceAlarmService, NlsService nlsService, IssueService issueService, PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, TimeService timeService, MeteringService meteringService) {
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

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
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
                "rule \"Basic device alarm rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DeviceAlarmEvent( eventType == \"@{" + DeviceAlarmEventDescription.END_DEVICE_EVENT_CREATED.getUniqueKey() + "}\" )\n" +
                "\teval( event.checkOccurrenceConditions(@{ruleId}, \"@{" + THRESHOLD + "}\", \"@{" + RAISE_EVENT_PROPS + "}\", \"@{" + TRIGGERING_EVENTS + "}\", \"@{" + CLEARING_EVENTS + "}\", \"@{" + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES + "}\") == true )\n" +
                "\teval( event.hasAssociatedDeviceLifecycleStatesInDeviceTypes(\"@{" + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES + "}\") == true )\n" +
                "then\n" +
                "\tSystem.out.println(\"Processing device alarm based on rule template number @{ruleId}\");\n" +
                "\tissueCreationService.processAlarmCreationEvent(@{ruleId}, event," + "\"@{" + RAISE_EVENT_PROPS + "}\");\n" +
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
                .forEach(deviceType ->
                        deviceType.getDeviceLifeCycle().getFiniteStateMachine().getStates().stream().distinct()
                                .sorted(Comparator.comparing(State::getId))
                                .forEach(state -> list.add(new DeviceLifeCycleInDeviceTypeInfo(deviceType, state))));
        DeviceLifeCycleInDeviceTypeInfo[] possibleValues = list.stream().toArray(DeviceLifeCycleInDeviceTypeInfo[]::new);

        builder.add(propertySpecService
                .specForValuesOf(new EventTypeInfoValueFactory())
                .named(TRIGGERING_EVENTS, TranslationKeys.TRIGGERING_EVENTS)
                .fromThesaurus(this.getThesaurus())
                //.markRequired()
                .markMultiValued(",")
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new EventTypeInfoValueFactory())
                .named(CLEARING_EVENTS, TranslationKeys.CLEARING_EVENTS)
                .fromThesaurus(this.getThesaurus())
                .markMultiValued(",")
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new DeviceLifeCycleInDeviceTypeInfoFactory())
                .named(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, TranslationKeys.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markMultiValued(",")
                .addValues(possibleValues)
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new RaiseEventPropsInfoValueFactory())
                .named(RAISE_EVENT_PROPS, TranslationKeys.RAISE_EVENT_PROPS)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(new RaiseEventPropsInfo(RAISE_EVENT_PROPS_DEFAULT_VALUE))
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

    private OpenIssue getAlarmForUpdate(OpenIssue openIssue, IssueEvent event) {
        if (openIssue instanceof OpenDeviceAlarm && event instanceof DeviceAlarmEvent) {
            OpenDeviceAlarm alarm = OpenDeviceAlarm.class.cast(openIssue);
            List<String> clearingEvents = new ArrayList<>();
            alarm.getRule().getProperties().entrySet().stream().filter(entry -> entry.getKey().equals(CLEARING_EVENTS))
                    .findFirst().ifPresent(element ->
                    ((ArrayList) (element.getValue())).forEach(value -> clearingEvents.add(((EventTypeInfo) value).getName())));
            Optional<RaiseEventPropsInfo> newEventProps = alarm.getRule().getProperties().entrySet().stream().filter(entry -> entry.getKey().equals(RAISE_EVENT_PROPS))
                    .findFirst().map(found -> new RaiseEventPropsInfo(String.valueOf(found.getValue())));

            if (!clearingEvents.isEmpty() && ((DeviceAlarmEvent) event).isClearing(clearingEvents)) {
                if (!alarm.isStatusCleared()) {
                    alarm.setClearedStatus();
                }
                if (newEventProps.isPresent() && newEventProps.get().hasDecreaseUrgency()) {
                    alarm.setPriority(Priority.get(alarm.getPriority().lowerUrgency(), alarm.getPriority().getImpact()));
                }
            } else if (newEventProps.isPresent() && newEventProps.get().hasIncreaseUrgency() && !((DeviceAlarmEvent) event).isClearing(clearingEvents)) {
                alarm.setPriority(Priority.get(alarm.getPriority().increaseUrgency(), alarm.getPriority().getImpact()));
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
            List<String> splitEventTypeAndDeviceCode = Arrays.asList(stringValue.split(SEPARATOR));
            if (splitEventTypeAndDeviceCode.size() == 2) {
                return meteringService.getEndDeviceEventType(splitEventTypeAndDeviceCode.get(0))
                        .map(type -> new EventTypeInfo(type, splitEventTypeAndDeviceCode.get(1)))
                        .orElse(null);
            } else {
                return null;
            }
        }

        @Override
        public String toStringValue(HasIdAndName object) {
            return String.valueOf(object.getName());
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

        private transient EndDeviceEventType eventType;

        private String deviceCode;

        EventTypeInfo(EndDeviceEventType eventType, String deviceCode) {
            this.eventType = eventType;
            this.deviceCode = deviceCode;
        }

        public int getTypeCode() {
            return eventType.getType().getCode();
        }

        public String getTypeName() {
            return eventType.getType().getMnemonic();
        }

        public int getDomainCode() {
            return eventType.getDomain().getCode();
        }

        public String getDomainName() {
            return eventType.getDomain().getMnemonic();
        }

        public int getSubDomainCode() {
            return eventType.getSubDomain().getCode();
        }

        public String getSubDomainName() {
            return eventType.getSubDomain().getMnemonic();
        }

        public int getEventOrActionCode() {
            return eventType.getEventOrAction().getCode();
        }

        public String getEventOrActionName() {
            return eventType.getEventOrAction().getMnemonic();
        }

        public String getDeviceCode() {
            return deviceCode;
        }


        @Override
        public String getId() {
            return eventType.getMRID().concat(SEPARATOR).concat(deviceCode);
            /*
            return Stream.<HasNumericCode>of(type, domain, subDomain, eventOrAction)
                .map(hasNumericCode -> Optional.ofNullable(hasNumericCode)
                        .map(HasNumericCode::getCode)
                        .map(String::valueOf)
                        .orElse("*")
                )
                .collect(Collectors.joining("."));
             */
        }

        @Override
        public String getName() {
            return eventType.getAliasName() != null ? eventType.getAliasName() : "end device event type " + getId();
        }
    }


    private class DeviceLifeCycleInDeviceTypeInfoFactory implements ValueFactory<HasIdAndName>, DeviceLifeCycleInDeviceTypePropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            List<String> values = Arrays.asList(stringValue.split(SEPARATOR));
            if (values.size() != 2) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUIMENTS, "Device Life Cycle in Device Type");
            }
            DeviceType deviceType = deviceConfigurationService
                    .findDeviceType(Long.parseLong(values.get(0)))
                    .orElse(null);
            State lifeCycleState = deviceLifeCycleConfigurationService
                    .findAllDeviceLifeCycles().find()
                    .stream().map(lifecycle -> lifecycle.getFiniteStateMachine().getStates())
                    .flatMap(Collection::stream)
                    .filter(stateValue -> stateValue.getId() == Long.parseLong(values.get(1)))
                    .findFirst()
                    .orElse(null);
            return new DeviceLifeCycleInDeviceTypeInfo(deviceType, lifeCycleState);
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

    public static class DeviceLifeCycleInDeviceTypeInfo extends HasIdAndName {

        private DeviceType deviceType;
        private State lifeCycleState;

        public DeviceLifeCycleInDeviceTypeInfo(DeviceType deviceType, State lifeCycleState) {
            this.deviceType = deviceType;
            this.lifeCycleState = lifeCycleState;
        }

        @Override
        public String getId() {
            return deviceType.getId() + SEPARATOR + lifeCycleState.getId();
        }

        @Override
        public String getName() {
            try {
                JSONObject jsonId = new JSONObject();
                jsonId.put("deviceTypeName", deviceType.getName());
                jsonId.put("lifeCycleStateName", deviceType.getName() + "." + lifeCycleState.getName());
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
            if (!(o instanceof DeviceLifeCycleInDeviceTypeInfo)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DeviceLifeCycleInDeviceTypeInfo that = (DeviceLifeCycleInDeviceTypeInfo) o;

            return deviceType.equals(that.deviceType) && lifeCycleState.equals(that.lifeCycleState);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + deviceType.hashCode();
            result = 31 * result + lifeCycleState.hashCode();
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

        public boolean hasIncreaseUrgency() {
            return Integer.parseInt(Arrays.asList(value.split(SEPARATOR)).get(1)) == 1;
        }

        public boolean hasDecreaseUrgency() {
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


    private class RelativePeriodWithCountInfoValueFactory implements ValueFactory<HasIdAndName>, RelativePeriodWithCountFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            List<String> values = Arrays.asList(stringValue.split(SEPARATOR));
            if (values.size() != 2) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUIMENTS, "Relative period with occurrence count for device alarms");
            }
            int count = Integer.parseInt(values.get(0));
            RelativePeriod relativePeriod = timeService.findRelativePeriodByName(values.get(1)).orElse(null);
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

    public static class RelativePeriodWithCountInfo extends HasIdAndName {

        private RelativePeriod relativePeriod;
        private int occurrenceCount;

        public RelativePeriodWithCountInfo(int occurrenceCount, RelativePeriod relativePeriod) {
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


    //TODO - write a check method to avoid number format exceptions
    /*
    public static boolean isInteger(String s) {
    try {
        Integer.parseInt(s);
    } catch(NumberFormatException e) {
        return false;
    } catch(NullPointerException e) {
        return false;
    }
    return true;
}
     */
}