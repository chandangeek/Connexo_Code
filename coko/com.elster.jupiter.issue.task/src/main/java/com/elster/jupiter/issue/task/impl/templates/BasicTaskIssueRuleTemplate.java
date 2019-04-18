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
import com.elster.jupiter.issue.task.OpenTaskIssue;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.event.TaskFailureEvent;
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
import com.elster.jupiter.properties.rest.RecurrenceSelectionPropertyFactory;
import com.elster.jupiter.properties.rest.TaskPropertyFacory;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.sql.SqlBuilder;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Component(name = "com.elster.jupiter.issue.task.BasicTaskIssueRuleTemplate",
        property = {"name=" + BasicTaskIssueRuleTemplate.NAME, "app=MDC"},
        service = CreationRuleTemplate.class,
        immediate = true)
public class BasicTaskIssueRuleTemplate extends AbstractTaskIssueTemplate {
    static final String NAME = "BasicTaskIssueRuleTemplate";

    public static final String AUTORESOLUTION = NAME + ".autoresolution";
    public static final String LOG_ON_SAME_ISSUE = NAME + ".logOnSameIssue";
    public static final String TASK_PROPS = NAME + ".taskProps";
    private static final String DEFAULT_KEY = "1:1";
    private static final String COMMA_SEPARATOR = ",";
    public static final String COLON_SEPARATOR = ":";
    private static final List<String> LOG_ON_SAME_ISSUE_POSSIBLE_VALUES = Arrays.asList("0:0", "1:0", "1:1");
    private static final Logger LOG = Logger.getLogger(BasicTaskIssueRuleTemplate.class.getName());
    private static final String SUPPORTED_APPLICATION = "MultiSense";

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
                "\teval( event.logOnSameIssue(@{ruleId}, \"@{" + LOG_ON_SAME_ISSUE + "}\") == true )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by basic task rule=@{ruleId} with log on same issue\");\n" +
                "\tissueCreationService.processAlarmCreationEvent(@{ruleId}, event, true);\n" +
                "end\n" +
                "rule \"Basic task rule @{ruleId} without log on same issue\"\n" +
                "when\n" +
                "\tevent : TaskFailureEvent( resolveEvent == false, recurrentTaskId in (@{" + TASK_PROPS + "}))\n" +
                "\teval( event.logOnSameIssue(@{ruleId}, \"@{" + LOG_ON_SAME_ISSUE + "}\") == false )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by basic task rule=@{ruleId} with create new issue\");\n" +
                "\tissueCreationService.processAlarmCreationEvent(@{ruleId}, event, false);\n" +
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
        if (openIssue instanceof OpenTaskIssue && event instanceof TaskFailureEvent) {
            OpenTaskIssue taskIssue = OpenTaskIssue.class.cast(openIssue);
            TaskFailureEvent taskEvent = (TaskFailureEvent) event;
            taskIssue.addTaskOccurrence(taskEvent.getTaskOccurrence(), taskEvent.getErrorMessage(), taskEvent.getFailureTime());

            if (IssueStatus.IN_PROGRESS.equals(openIssue.getStatus().getKey())) {
                taskIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).orElseThrow(() ->
                        new IllegalArgumentException(TranslationKeys.ISSUE_REASON_TASKFAILED.getDefaultFormat()) {
                        }));
            }
            Optional<RecurrenceSelectionInfo> recurrenceSelectionInfo = openIssue.getRule()
                    .getProperties()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equals(LOG_ON_SAME_ISSUE))
                    .findFirst()
                    .map(found -> (RecurrenceSelectionInfo) found.getValue());
            if (recurrenceSelectionInfo.isPresent() &&
                    recurrenceSelectionInfo.get().hasIncreaseUrgency()) {
                taskIssue.setPriority(Priority.get(openIssue.getPriority().increaseUrgency(), openIssue.getPriority()
                        .getImpact()));
            }
            taskIssue.update();

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
        //As per product management request - this type of tasks will be supported for Multisense only
        TaskPropsInfo[] taskPossibleValues = taskService.getRecurrentTasks()
                .stream()
                .filter(task -> task.getApplication().equals(SUPPORTED_APPLICATION))
                .map(TaskPropsInfo::new)
                .toArray(TaskPropsInfo[]::new);
        RecurrenceSelectionInfo[] logAndPriorityValues = LOG_ON_SAME_ISSUE_POSSIBLE_VALUES.stream()
                .map(RecurrenceSelectionInfo::new)
                .toArray(RecurrenceSelectionInfo[]::new);
        builder.add(propertySpecService
                .specForValuesOf(new RecurrenceSelectionInfoValueFactory())
                .named(LOG_ON_SAME_ISSUE, TranslationKeys.ISSUE_CREATION_SELECTION_ON_RECURRENCE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .setDefaultValue(new RecurrenceSelectionInfo(DEFAULT_KEY))
                .addValues(logAndPriorityValues)
                .finish());
        builder.add(propertySpecService
                .booleanSpec()
                .named(AUTORESOLUTION, TranslationKeys.PARAMETER_AUTO_RESOLUTION)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(true)
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

    @XmlRootElement
    private class RecurrenceSelectionInfo extends HasIdAndName {

        private transient String value;

        RecurrenceSelectionInfo(String value) {
            this.value = value;
        }

        @Override
        public Object getId() {
            return value;
        }


        @Override
        public String getName() {
            return getFormatedName();
        }

        private String getFormatedName() {
            List<Integer> values = parsed();
            String name = values.get(0) == 0 ? format(LogOnSameIssueSelection.CREATE_NEW_ISSUE) :
                    values.get(1) == 0 ? format(LogOnSameIssueSelection.LOG_ON_SAME_ISSUE_WITHOUT_PRIORITY_INCREASE) :
                            format(LogOnSameIssueSelection.LOG_ON_SAME_ISSUE_WITH_PRIORITY_INCREASE);
            List<String> nameValues = Arrays.asList(name.split(COLON_SEPARATOR));
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("logOnSameIssueName", nameValues.get(0));
                jsonObj.put("increaseUrgencyName", nameValues.get(1));
                return jsonObj.toString();
            } catch (JSONException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
            return "";
        }

        private String format(LogOnSameIssueSelection selection) {

            return getThesaurus().getFormat(selection.getLogOnsameIssueDisplay()).format() + COLON_SEPARATOR + getThesaurus().getFormat(selection.getIncreasePriorityDisplay()).format();
        }

        private List<Integer> parsed() {
            List<String> values = Arrays.asList(value.split(COLON_SEPARATOR));
            if (values.size() != 2) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS,
                        "properties." + LOG_ON_SAME_ISSUE,
                        String.valueOf(2),
                        String.valueOf(values.size()));
            }
            if (!LOG_ON_SAME_ISSUE_POSSIBLE_VALUES.contains(value)) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT,
                        "properties." + LOG_ON_SAME_ISSUE,
                        values.stream()
                                .filter(LOG_ON_SAME_ISSUE_POSSIBLE_VALUES::contains)
                                .filter(values::remove).findFirst());

            }
            return Arrays.stream(value.split(COLON_SEPARATOR)).map(Integer::parseInt).collect(Collectors.toList());

        }

        private boolean hasIncreaseUrgency() {
            return parsed().get(1) == 1;
        }

    }

    private class RecurrenceSelectionInfoValueFactory implements ValueFactory<HasIdAndName>, RecurrenceSelectionPropertyFactory {
        @Override
        public RecurrenceSelectionInfo fromStringValue(String stringValue) {
            return new RecurrenceSelectionInfo(stringValue);
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

    private enum LogOnSameIssueSelection {
        CREATE_NEW_ISSUE(TranslationKeys.CREATE_NEW_TASK_ISSUE, TranslationKeys.PARAMETER_DO_NOTHING),
        LOG_ON_SAME_ISSUE_WITH_PRIORITY_INCREASE(TranslationKeys.LOG_ON_SAME_TASK_ISSUE, TranslationKeys.PARAMETER_INCREASE_URGENCY),
        LOG_ON_SAME_ISSUE_WITHOUT_PRIORITY_INCREASE(TranslationKeys.LOG_ON_SAME_TASK_ISSUE, TranslationKeys.PARAMETER_DO_NOTHING);

        TranslationKeys logOnsameIssueDisplay;
        TranslationKeys increasePriorityDisplay;

        LogOnSameIssueSelection(TranslationKeys logOnsameIssueDisplay, TranslationKeys increasePriorityDisplay) {
            this.logOnsameIssueDisplay = logOnsameIssueDisplay;
            this.increasePriorityDisplay = increasePriorityDisplay;
        }

        public TranslationKeys getLogOnsameIssueDisplay() {
            return logOnsameIssueDisplay;
        }

        public TranslationKeys getIncreasePriorityDisplay() {
            return increasePriorityDisplay;
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

            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("destinationName", recurrentTask.getDestination().getName());
                jsonObj.put("recurrentTaskName", recurrentTask.getName());
                return jsonObj.toString();
            } catch (JSONException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
            return "";
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