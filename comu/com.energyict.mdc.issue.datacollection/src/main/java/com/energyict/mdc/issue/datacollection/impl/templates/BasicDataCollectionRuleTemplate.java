/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.AllowsComTaskFiltering;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.ExcludedComTaskPropertyFactory;
import com.elster.jupiter.properties.rest.RaiseEventUrgencyFactory;
import com.elster.jupiter.properties.rest.RelativePeriodWithCountPropertyFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;
import com.energyict.mdc.tasks.TaskService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Clock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.CONNECTION_LOST;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.DEVICE_COMMUNICATION_FAILURE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNABLE_TO_CONNECT;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNKNOWN_OUTBOUND_DEVICE;

public class BasicDataCollectionRuleTemplate extends AbstractDataCollectionTemplate implements AllowsComTaskFiltering {
    private static final Logger LOG = Logger.getLogger(BasicDataCollectionRuleTemplate.class.getName());
    public static final String NAME = "BasicDataCollectionRuleTemplate";

    public static final String EVENTTYPE = NAME + ".eventType";
    public static final String EXCLUDEDCOMTASKS = NAME + ".excludedComTasks";
    public static final String AUTORESOLUTION = NAME + ".autoresolution";
    public static final String RADIOGROUP = NAME + ".increaseurgency";
    public static final String THRESHOLD = NAME + ".threshold";
    private static final String DEFAULT_VALUE = "Do nothing";
    private static final Long DEFAULT_KEY = 0L;
    private static final String SEPARATOR = ":";
    private static final int DEFAULT_NUMERICAL_VALUE = 0;
    public static final String TRIGGERING_EVENTS = NAME + ".triggeringEvents";
    public static final String VALUE_SEPARATOR = ",";

    private volatile TaskService taskService;

    @Inject
    public BasicDataCollectionRuleTemplate(IssueDataCollectionService issueDataCollectionService,
                                           Thesaurus thesaurus,
                                           IssueService issueService,
                                           PropertySpecService propertySpecService,
                                           DeviceConfigurationService deviceConfigurationService,
                                           DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                           TimeService timeService,
                                           Clock clock,
                                           MeteringTranslationService meteringTranslationService,
                                           TaskService taskService) {
        super.setIssueDataCollectionService(issueDataCollectionService);
        super.setThesaurus(thesaurus);
        super.setIssueService(issueService);
        super.setPropertySpecService(propertySpecService);
        super.setDeviceConfigurationService(deviceConfigurationService);
        super.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        super.setTimeService(timeService);
        super.setClock(clock);
        setTaskService(taskService);
        super.setMeteringTranslationService(meteringTranslationService);
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String getName() {
        return BasicDataCollectionRuleTemplate.NAME;
    }

    @Override
    public String getDescription() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DATACOLLECTION_DESCRIPTION).format();
    }

    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datacollection\n" +
                "import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Basic datacollection rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\", resolveEvent == false )\n" +
                "\teval( event.checkOccurrenceConditions(\"@{" + THRESHOLD + "}\", \"@{" + TRIGGERING_EVENTS + "}\") == true )\n" +
                "\teval( event.hasAssociatedDeviceLifecycleStatesInDeviceTypes(\"@{" + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES + "}\") == true )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by basic datacollection rule=@{ruleId}\");\n" +
                "\tevent.setCreationRule(@{ruleId});\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n" +
                "rule \"Auto-resolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\", resolveEvent == true, @{" + AUTORESOLUTION + "} == 1 )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve issue by basic datacollection rule=@{ruleId}\");\n" +
                "\tevent.setCreationRule(@{ruleId});\n" +
                "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
                "end\n" +
                "rule \"Setting priority to default value section @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\", resolveEvent == true, @{" + AUTORESOLUTION + "} == 0 )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to discard issue's priority to default value by basic datacollection rule=@{ruleId}\");\n" +
                "\tevent.setCreationRule(@{ruleId});\n" +
                "\tissueCreationService.processIssueDiscardPriorityOnResolutionEvent(@{ruleId}, event);\n" +
                "end";
    }

    @Override
    public void updateIssue(OpenIssue openIssue, IssueEvent event) {
        if (IssueStatus.IN_PROGRESS.equals(openIssue.getStatus().getKey())) {
            openIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).orElseThrow(() ->
                    new IllegalArgumentException(TranslationKeys.ISSUE_REASON_UNKNOWN.getDefaultFormat()) {
                    }));
        }

        Optional<EventTypeInfo> newEventProps;
        if (openIssue.getRule().isPresent()) {
            newEventProps = openIssue.getRule().get()
                    .getProperties()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equals(RADIOGROUP))
                    .findFirst()
                    .map(found -> (EventTypeInfo) found.getValue());
        } else {
            newEventProps = Optional.empty();
        }
        if (newEventProps.isPresent() &&
                newEventProps.get().hasIncreaseUrgency()) {
            openIssue.setPriority(Priority.get(openIssue.getPriority().increaseUrgency(), openIssue.getPriority()
                    .getImpact()));
        }

        updateConnectionAttempts(openIssue, event).update();
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssue openIssue = (OpenIssue) issue.get();
            issue = Optional.of(updateConnectionAttempts(openIssue, event).close(issueService.findStatus(IssueStatus.RESOLVED)
                    .get()));
        }
        return issue;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        EventTypes eventTypes = new EventTypes(getThesaurus(), CONNECTION_LOST, DEVICE_COMMUNICATION_FAILURE, UNABLE_TO_CONNECT, UNKNOWN_INBOUND_DEVICE, UNKNOWN_OUTBOUND_DEVICE);
        HashMap<Long, String> possibleActionValues = getPossibleValues();

        EventTypeInfo[] possibleValues = possibleActionValues.entrySet().stream()
                .map(entry -> new EventTypeInfo(entry.getKey(), entry.getValue()))
                .toArray(EventTypeInfo[]::new);
        builder.add(propertySpecService
                .specForValuesOf(new DeviceLifeCycleInDeviceTypeInfoValueFactory(deviceConfigurationService, deviceLifeCycleConfigurationService, meteringTranslationService))
                .named(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, TranslationKeys.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markMultiValued(";")
                .addValues(deviceConfigurationService.getDeviceLifeCycleInDeviceTypeInfoPossibleValues())
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new CustomEventTypeValueFactory(eventTypes))
                .named(EVENTTYPE, TranslationKeys.PARAMETER_NAME_EVENT_TYPE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .addValues(eventTypes.getEventTypes())
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish());
        builder.add(propertySpecService.specForValuesOf(new ExcludedComTaskValueFactory())
                .named(EXCLUDEDCOMTASKS, TranslationKeys.PARAMETER_EXCLUDED_COM_TASKS)
                .describedAs(TranslationKeys.PARAMETER_EXCLUDED_COM_TASKS)
                .fromThesaurus(this.getThesaurus())
                .markMultiValued(VALUE_SEPARATOR)
                .finish());
        builder.add(propertySpecService
                .booleanSpec()
                .named(AUTORESOLUTION, TranslationKeys.PARAMETER_AUTO_RESOLUTION)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(true)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new EventTypeInfoValueFactory())
                .named(RADIOGROUP, TranslationKeys.PARAMETER_RADIO_GROUP)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(new EventTypeInfo(DEFAULT_KEY, DEFAULT_VALUE))
                .addValues(possibleValues)
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
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DATACOLLECTION_NAME).format();
    }

    private OpenIssue updateConnectionAttempts(OpenIssue openIssue, IssueEvent event) {
        if (openIssue instanceof OpenIssueDataCollection && event instanceof DataCollectionEvent) {
            OpenIssueDataCollection dcIssue = OpenIssueDataCollection.class.cast(openIssue);
            dcIssue.setLastConnectionAttemptTimestamp(DataCollectionEvent.class.cast(event).getTimestamp());
            dcIssue.incrementConnectionAttempt();
            return dcIssue;
        }
        return openIssue;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    private HashMap<Long, String> getPossibleValues() {
        return new HashMap<Long, String>() {{
            put(0L, getThesaurus().getFormat(TranslationKeys.PARAMETER_DO_NOTHING).format());
            put(1L, getThesaurus().getFormat(TranslationKeys.PARAMETER_INCREASE_URGENCY).format());
        }};
    }

    @XmlRootElement
    private class EventTypeInfo extends HasIdAndName {

        private Long id;
        private String name;


        public EventTypeInfo(Long id, String name) {
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

            EventTypeInfo that = (EventTypeInfo) o;

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

    private class EventTypeInfoValueFactory implements ValueFactory<HasIdAndName>, RaiseEventUrgencyFactory {
        @Override
        public EventTypeInfo fromStringValue(String stringValue) {
            return new EventTypeInfo(Long.parseLong(stringValue), getPossibleValues().get(Long.parseLong(stringValue)));
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

    private class ExcludedComTaskValueFactory implements ValueFactory<ComTask>, ExcludedComTaskPropertyFactory {

        @Override
        public ComTask fromStringValue(String stringValue) {
            return taskService.findComTask(Long.parseLong(stringValue)).orElse(null);
        }

        @Override
        public String toStringValue(ComTask object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<ComTask> getValueType() {
            return ComTask.class;
        }

        @Override
        public ComTask valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(ComTask object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, ComTask value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, ComTask value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    @Override
    public List<? extends HasId> getExcludedComTasks(Map<String, Object> properties) {
        return (List<ComTask>) properties.get(EXCLUDEDCOMTASKS);
    }

    private class RelativePeriodWithCountInfoValueFactory implements ValueFactory<HasIdAndName>, RelativePeriodWithCountPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            List<String> values = Arrays.asList(stringValue.split(SEPARATOR));
            if (values.size() != 2) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS,
                        "properties." + THRESHOLD,
                        String.valueOf(2),
                        String.valueOf(values.size()));
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

    public static class RelativePeriodWithCountInfo extends HasIdAndName {

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
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
            return "";
        }

        public long getRelativePeriodId() {
            return relativePeriod.getId();
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