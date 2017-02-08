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
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.DeviceTypePropertyFactory;
import com.elster.jupiter.properties.rest.EndDeviceEventTypePropertyFactory;
import com.elster.jupiter.properties.rest.LifecycleStatePropertyFactory;
import com.elster.jupiter.properties.rest.RaiseEventPropertyFactory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmEvent;
import com.energyict.mdc.device.alarms.event.EndDeviceEventCreatedEvent;
import com.energyict.mdc.device.alarms.impl.event.DeviceAlarmEventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
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
    public static final String EVENT_OCCURENCE_COUNT = NAME + ".eventCount";
    public static final String DEVICE_LIFECYCLE_STATE = NAME + ".deviceLifecyle";
    public static final String DEVICE_TYPES = NAME + ".deviceTypes";
    public static final String DEVICE_CODES = NAME + ".deviceCodes";

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
                "\tevent : DeviceAlarmEvent( eventType == \"@{" + EVENTTYPE + "}\" )\n" +
                "\teval( event.computeOccurenceCount(@{ruleId}, \"@{" + THRESHOLD + "}\", \"@{" + RAISE_EVENT_PROPS + "}\", \"@{" + TRIGGERING_EVENTS + "}\", \"@{" + CLEARING_EVENTS + "}\", \"@{" + DEVICE_CODES + "}\") >= @{" + EVENT_OCCURENCE_COUNT + "} )\n" +
                "\teval( event.hasAssociatedDeviceLifecycleStateIn(\"@{" + DEVICE_LIFECYCLE_STATE + "}\") == true )\n" +
                "\teval( event.hasAssociatedDeviceTypeIn(\"@{" + DEVICE_TYPES + "}\") == true )\n" +
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
        EventTypes eventTypes = new EventTypes(getThesaurus(), DeviceAlarmEventDescription.values());
        /*EventTypeInfo[] possibleEventTypes = meteringService.getAvailableEndDeviceEventTypes().stream().map(EventTypeInfo::new)
                .toArray(EventTypeInfo[]::new); */
        DeviceTypeInfo[] possibleDeviceTypes = deviceConfigurationService.findAllDeviceTypes()
                .find().stream().map(DeviceTypeInfo::new)
                .toArray(DeviceTypeInfo[]::new);
        DeviceLifecycleStatusInfo[] possibleDeviceLifecycleStates = deviceLifeCycleConfigurationService
                .findAllDeviceLifeCycles().find()
                .stream().map(lifecycle -> lifecycle.getFiniteStateMachine().getStates())
                .flatMap(Collection::stream)
                .map(DeviceLifecycleStatusInfo::new)
                .toArray(DeviceLifecycleStatusInfo[]::new);
        builder.add(propertySpecService
                .specForValuesOf(new EventTypeValueFactory(eventTypes))
                .named(EVENTTYPE, TranslationKeys.PARAMETER_NAME_EVENT_TYPE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .addValues(eventTypes.getEventTypes())
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish());
        builder.add(propertySpecService
                .stringSpec()
                .named(RAISE_EVENT_PROPS, TranslationKeys.RAISE_EVENT_PROPS)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue("0-0-0")
                .finish());
        builder.add(propertySpecService
                .relativePeriodSpec()
                .named(THRESHOLD, TranslationKeys.EVENT_TEMPORAL_THRESHOLD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(timeService.getAllRelativePeriod())
                .finish());
        builder.add(propertySpecService
                .longSpec()
                .named(EVENT_OCCURENCE_COUNT, TranslationKeys.EVENT_OCCURENCE_COUNT)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new EventTypeInfoValueFactory())
                .named(TRIGGERING_EVENTS, TranslationKeys.TRIGGERING_EVENTS)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markMultiValued(",")
              //  .addValues(possibleEventTypes)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new EventTypeInfoValueFactory())
                .named(CLEARING_EVENTS, TranslationKeys.CLEARING_EVENTS)
                .fromThesaurus(this.getThesaurus())
                .markMultiValued(",")
                //.addValues(possibleEventTypes)
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new DeviceLifecycleStatusInfoValueFactory())
                .named(DEVICE_LIFECYCLE_STATE, TranslationKeys.DEVICE_LIFECYCLE_STATE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markMultiValued(",")
                .addValues(possibleDeviceLifecycleStates)
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new DeviceTypeInfoValueFactory())
                .named(DEVICE_TYPES, TranslationKeys.DEVICE_TYPE)
                .fromThesaurus(this.getThesaurus())
                .addValues(possibleDeviceTypes)
                .markRequired()
                .markMultiValued(",")
                .markExhaustive(PropertySelectionMode.LIST)
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .stringSpec()
                .named(DEVICE_CODES, TranslationKeys.DEVICE_CODE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markMultiValued(",")
             //   .markExhaustive(PropertySelectionMode.LIST)
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
                    /*if (!alarm.getPriority().lowerUrgency()) {
                        LOG.log(Level.SEVERE, "Urgency is minimum [" + alarm.getPriority().getUrgency() +"]. Unable to decrement anymore");
                    }*/
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

    // BpmProcessPropertyFactory - maps to SimplePropertyType.SELECTIONGRID, needed PropertyValueInfoService for single or no selection - will rename or create marker interface
    private class EventTypeInfoValueFactory implements ValueFactory<HasName>, EndDeviceEventTypePropertyFactory {
        @Override
        public HasName fromStringValue(String stringValue) {
            //TODO - add inexistent event, after being validated, to MTR_ENDDEVICEEVENTTYPE on creation rule save
         return meteringService.getEndDeviceEventType(stringValue)
                    .map(EventTypeInfo::new)
                    .orElse(null);
        }

        @Override
        public String toStringValue(HasName object) {
            return String.valueOf(object.getName());
        }

        @Override
        public Class<HasName> getValueType() {
            return HasName.class;
        }

        @Override
        public HasName valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(HasName object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, HasName value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, HasName value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    @XmlRootElement
    static class EventTypeInfo implements HasName {

        private transient EndDeviceEventType eventType;

        EventTypeInfo(EndDeviceEventType eventType) {
            this.eventType = eventType;
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


        @Override
        public String getName() {
            return eventType.getMRID();
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
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof EventTypeInfo)) {
                return false;
            }

            EventTypeInfo that = (EventTypeInfo) o;

            return eventType.equals(that.eventType);

        }

        @Override
        public int hashCode() {
            return eventType.hashCode();
        }


    }


    private class DeviceTypeInfoValueFactory implements ValueFactory<HasIdAndName>, DeviceTypePropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return deviceConfigurationService
                    .findDeviceType(Long.parseLong(stringValue))
                    .map(DeviceTypeInfo::new)
                    .orElse(null);
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
    static class DeviceTypeInfo extends HasIdAndName {

        private transient DeviceType deviceType;

        DeviceTypeInfo(DeviceType deviceType) {
            this.deviceType = deviceType;
        }

        @Override
        public Long getId() {
            return deviceType.getId();
        }

        @Override
        public String getName() {
            return deviceType.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DeviceTypeInfo)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DeviceTypeInfo that = (DeviceTypeInfo) o;

            return deviceType.equals(that.deviceType);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + deviceType.hashCode();
            return result;
        }
    }

    private class DeviceLifecycleStatusInfoValueFactory implements ValueFactory<HasIdAndName>, LifecycleStatePropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return deviceLifeCycleConfigurationService
                    .findAllDeviceLifeCycles().find()
                    .stream().map(lifecycle -> lifecycle.getFiniteStateMachine().getStates())
                    .flatMap(Collection::stream)
                    .filter(stateValue -> stateValue.getId() == Long.parseLong(stringValue))
                    .findFirst()
                    .map(DeviceLifecycleStatusInfo::new)
                    .orElse(null);
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
    static class DeviceLifecycleStatusInfo extends HasIdAndName {

        private transient State state;

        DeviceLifecycleStatusInfo(State state) {
            this.state = state;
        }

        @Override
        public Long getId() {
            return state.getId();
        }

        @Override
        public String getName() {
            return state.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DeviceLifecycleStatusInfo)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DeviceLifecycleStatusInfo that = (DeviceLifecycleStatusInfo) o;

            return state.equals(that.state);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + state.hashCode();
            return result;
        }
    }


    private class RaiseEventPropsInfoValueFactory implements ValueFactory<HasName>, RaiseEventPropertyFactory {
        @Override
        public HasName fromStringValue(String stringValue) {
            return new RaiseEventPropsInfo(stringValue);
        }

        @Override
        public String toStringValue(HasName object) {
            return String.valueOf(object.getName());
        }

        @Override
        public Class<HasName> getValueType() {
            return HasName.class;
        }

        @Override
        public HasName valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(HasName object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, HasName value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, HasName value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    @XmlRootElement
    static class RaiseEventPropsInfo implements HasName {

        private transient String value;

        RaiseEventPropsInfo(String value) {
            this.value = value;
        }


        @Override
        public String getName() {
            return value;
        }

        public boolean logOnSameAlarm(){
            return Integer.parseInt(Arrays.asList(value.split("-")).get(0)) == 1;
        }

        public boolean hasIncreaseUrgency(){
            return Integer.parseInt(Arrays.asList(value.split("-")).get(1)) == 1;
        }

        public boolean hasDecreaseUrgency(){
            return Integer.parseInt(Arrays.asList(value.split("-")).get(2)) == 1;
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