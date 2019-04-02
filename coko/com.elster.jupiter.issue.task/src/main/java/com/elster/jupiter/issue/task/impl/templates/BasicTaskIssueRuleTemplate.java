/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.templates;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.task.impl.i18n.TranslationKeys;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.RaiseEventUrgencyFactory;
import com.elster.jupiter.properties.rest.TaskPropertyFacory;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.sql.SqlBuilder;

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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@Component(name = "com.elster.jupiter.issue.task.BasicTaskIssueRuleTemplate",
        property = {"name=" + BasicTaskIssueRuleTemplate.NAME},
        service = CreationRuleTemplate.class,
        immediate = true)
public class BasicTaskIssueRuleTemplate extends AbstractTaskIssueTemplate {
    static final String NAME = "BasicTaskIssueRuleTemplate";

    public static final String AUTORESOLUTION = NAME + ".autoresolution";
    public static final String URGENCYPROPS = NAME + ".increaseurgency";
    public static final String LOG_ON_SAME_ISSUE = NAME + ".logOnSameIssue";
    public static final String TASK_PROPS = NAME + ".taskProps";
    private static final String DEFAULT_VALUE = "Do nothing";
    private static final Long DEFAULT_KEY = 0L;
    private static final String COMMA_SEPARATOR = ",";
    private static final String COLON_SEPARATOR = ":";

    //for OSGI
    public BasicTaskIssueRuleTemplate() {
    }

    @Inject
    public BasicTaskIssueRuleTemplate(TaskIssueService taskIssueService, NlsService nlsService, IssueService issueService, PropertySpecService propertySpecService) {
        this();
        setTaskIssueService(taskIssueService);
        setNlsService(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);


        activate();
    }

    @Activate
    public void activate() {
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(TaskIssueService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setTaskIssueService(TaskIssueService taskIssueService) {
        super.setTaskIssueService(taskIssueService);
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
    public void setTaskService(TaskService taskService) {
        super.setTaskService(taskService);
    }

    @Override
    public String getName() {
        return BasicTaskIssueRuleTemplate.NAME;
    }

    @Override
    public String getDescription() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_TASK_DESCRIPTION).format();
    }

    @Override
    public String getContent() {
        return "package com.elster.jupiter.issue.task\n" +
                "import com.elster.jupiter.issue.task.event.TaskFailureEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Basic task rule @{ruleId} with log on same issue\"\n" +
                "when\n" +
                "\tevent : TaskFailureEvent( resolveEvent == false, recurrentTaskId in (@{" + TASK_PROPS + "}))\n" +
                "\teval( event.logOnSameIssue(\"@{" + LOG_ON_SAME_ISSUE + "}\") == true )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by basic task rule=@{ruleId}\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n" +
                "rule \"Basic task rule @{ruleId} without log on same issue\"\n" +
                "when\n" +
                "\tevent : TaskFailureEvent( resolveEvent == false, recurrentTaskId in (@{" + TASK_PROPS + "}))\n" +
                "\teval( event.logOnSameIssue(\"@{" + LOG_ON_SAME_ISSUE + "}\") == false )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by basic task rule=@{ruleId}\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n" +
                "rule \"Auto-resolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent : TaskFailureEvent(resolveEvent == true, @{" + AUTORESOLUTION + "} == 1 )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve issue by basic task rule=@{ruleId}\");\n" +
                "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
                "end";
    }

    @Override
    public void updateIssue(OpenIssue openIssue, IssueEvent event) {
        if (IssueStatus.IN_PROGRESS.equals(openIssue.getStatus().getKey())) {
            openIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).orElseThrow(() ->
                    new IllegalArgumentException(TranslationKeys.ISSUE_REASON_TASKFAILED.getDefaultFormat()) {
                    }));
        }

        Optional<RecurrenceSelectionInfo> newEventProps = openIssue.getRule()
                .getProperties()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(URGENCYPROPS))
                .findFirst()
                .map(found -> (RecurrenceSelectionInfo) found.getValue());
        if (newEventProps.isPresent() &&
                newEventProps.get().hasIncreaseUrgency()) {
            openIssue.setPriority(Priority.get(openIssue.getPriority().increaseUrgency(), openIssue.getPriority()
                    .getImpact()));
        }
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssue openIssue = (OpenIssue) issue.get();
            issue = Optional.of(openIssue.close(issueService.findStatus(IssueStatus.RESOLVED)
                    .get()));
        }
        return issue;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        TaskPropsInfo[] taskPossibleValues = taskService.getRecurrentTasks().stream().map(TaskPropsInfo::new).toArray(TaskPropsInfo[]::new);
        HashMap<Long, String> possibleActionValues = getPossiblePrioValues();
        RecurrenceSelectionInfo[] priorityValues = possibleActionValues.entrySet().stream()
                .map(entry -> new RecurrenceSelectionInfo(entry.getKey(), entry.getValue()))
                .toArray(RecurrenceSelectionInfo[]::new);
        RecurrenceSelectionInfo[] logValues = getPossibleLogValues().entrySet().stream()
                .map(entry -> new RecurrenceSelectionInfo(entry.getKey(), entry.getValue()))
                .toArray(RecurrenceSelectionInfo[]::new);
        builder.add(propertySpecService
                .specForValuesOf(new RecurrenceSelectionInfoValueFactory())
                .named(LOG_ON_SAME_ISSUE, TranslationKeys.ISSUE_CREATION_SELECTION_ON_RECURRENCE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .setDefaultValue(new RecurrenceSelectionInfo(DEFAULT_KEY, thesaurus.getFormat(TranslationKeys.CREATE_NEW_TASK_ISSUE).format()))
                .addValues(logValues)
                .finish());
        builder.add(propertySpecService
                .booleanSpec()
                .named(AUTORESOLUTION, TranslationKeys.PARAMETER_AUTO_RESOLUTION)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(true)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new RecurrenceSelectionInfoValueFactory())
                .named(URGENCYPROPS, TranslationKeys.PARAMETER_RADIO_GROUP)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(new RecurrenceSelectionInfo(DEFAULT_KEY, DEFAULT_VALUE))
                .addValues(priorityValues)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new TaskPropsInfoValueFactory())
                .named(TASK_PROPS, TranslationKeys.TASK_PROPS)
                .fromThesaurus(thesaurus)
                .markRequired()
                .markMultiValued(COMMA_SEPARATOR)
                .addValues(taskPossibleValues)
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());

        return builder.build();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_TASK_NAME).format();
    }


    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    private HashMap<Long, String> getPossiblePrioValues() {
        return new HashMap<Long, String>() {{
            put(0L, getThesaurus().getFormat(TranslationKeys.PARAMETER_DO_NOTHING).format());
            put(1L, getThesaurus().getFormat(TranslationKeys.PARAMETER_INCREASE_URGENCY).format());
        }};
    }

    private HashMap<Long, String> getPossibleLogValues() {
        return new HashMap<Long, String>() {{
            put(0L, getThesaurus().getFormat(TranslationKeys.CREATE_NEW_TASK_ISSUE).format());
            put(1L, getThesaurus().getFormat(TranslationKeys.LOG_ON_SAME_TASK_ISSUE).format());
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
            return new RecurrenceSelectionInfo(Long.parseLong(stringValue), getPossiblePrioValues().get(Long.parseLong(stringValue)));
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

    private class TaskPropsInfoValueFactory implements ValueFactory<HasIdAndName>, TaskPropertyFacory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            RecurrentTask recurrentTask = taskService.getRecurrentTask(Long.parseLong(stringValue)).orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT,
                    "properties." + TASK_PROPS,
                    stringValue));

            return new TaskPropsInfo(recurrentTask);
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

    public static class TaskPropsInfo extends HasIdAndName {

        private RecurrentTask recurrentTask;

        TaskPropsInfo(RecurrentTask recurrentTask) {
            this.recurrentTask = recurrentTask;
        }


        @Override
        public String getId() {
            return String.valueOf(recurrentTask.getId());
        }

        @Override
        public String getName() {
            return recurrentTask.getApplication() + COLON_SEPARATOR + recurrentTask.getName() + COLON_SEPARATOR + recurrentTask.getDestination().getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TaskPropsInfo)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            TaskPropsInfo that = (TaskPropsInfo) o;

            return recurrentTask.equals(that.recurrentTask);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + recurrentTask.hashCode();
            return result;
        }
    }

}