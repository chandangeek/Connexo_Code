package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.PropertyFactoriesProvider;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.issue.share.entity.values.AssignIssueFormValue;
import com.elster.jupiter.issue.share.entity.values.CloseIssueFormValue;
import com.elster.jupiter.issue.share.entity.values.ProcessValue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ProcessAction extends AbstractIssueAction {

    private static final String NAME = "ProcessAction";
    private static final String PROCESSES_COMBOBOX = NAME + ".processesCombobox";
    private static final String ASSIGN_ISSUE_FORM = "AssignIssueForm";
    private static final String CLOSE_ISSUE_FORM = "CloseIssueForm";

    private final BpmService bpmService;
    private final PropertyFactoriesProvider propertyFactoriesProvider;
    private final ThreadPrincipalService threadPrincipalService;
    private final IssueService issueService;

    @Inject
    public ProcessAction(final DataModel dataModel,
                         final Thesaurus thesaurus,
                         final PropertySpecService propertySpecService,
                         final IssueService issueService,
                         final ThreadPrincipalService threadPrincipalService,
                         final BpmService bpmService,
                         final PropertyFactoriesProvider propertyFactoriesProvider) {
        super(dataModel, thesaurus, propertySpecService);
        this.bpmService = bpmService;
        this.propertyFactoriesProvider = propertyFactoriesProvider;
        this.threadPrincipalService = threadPrincipalService;
        this.issueService = issueService;
    }

    @Override
    public IssueActionResult execute(final Issue issue) {
        final IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();

        final Optional<ProcessValue> processCombobox = getProcessCombobox();

        if (processCombobox.isPresent()) {
            final ProcessValue processCombo = processCombobox.get();
            final Long processId = processCombo.getId();
            final Optional<BpmProcessDefinition> bpmProcessDefinition = bpmService.getActiveBpmProcessDefinitions()
                    .stream()
                    .filter(process -> process.getId() == processId)
                    .findFirst();

            if (bpmProcessDefinition.isPresent()) {
                final BpmProcessDefinition processDefinition = bpmProcessDefinition.get();

                final Map<String, Object> processInputParameters = new HashMap<>();
                processInputParameters.put("issueId", issue.getId());

                if (bpmService.startProcess(processDefinition, processInputParameters)) {
                    assignIssueSubAction(issue);
                    closeIssueSubAction(issue);
                    result.success(getThesaurus().getFormat(TranslationKeys.PROCESS_ACTION_SUCCESS).format());
                } else {
                    result.fail(getThesaurus().getFormat(TranslationKeys.PROCESS_ACTION_FAIL).format());
                }
            } else {
                result.fail(getThesaurus().getFormat(TranslationKeys.PROCESS_ACTION_PROCESS_IS_ABSENT).format());
            }
        } else {
            result.fail(getThesaurus().getFormat(TranslationKeys.PROCESS_ACTION_PROCESS_COMOBOX_IS_ABSENT).format());
        }

        return result;
    }

    private void assignIssueSubAction(final Issue issue) {
        final Optional<AssignIssueFormValue> assignIssueForm = getAssignIssueForm();
        assignIssueForm.ifPresent(aif -> {
            final Boolean checkboxValue = aif.getCheckbox().orElse(Boolean.FALSE);
            if (checkboxValue) {
                final Optional<User> user = assignIssueForm.get().getUser();
                final Optional<WorkGroup> workGroup = assignIssueForm.get().getWorkgroup();
                final Optional<String> assignIssueComment = assignIssueForm.get().getComment();
                if (user.isPresent() && workGroup.isPresent()) {
                    issue.assignTo(user.get().getId(), workGroup.get().getId());
                }
                assignIssueComment.map(comment -> issue.addComment(comment, (User) threadPrincipalService.getPrincipal()));
            }
        });
    }

    private void closeIssueSubAction(final Issue issue) {
        final Optional<CloseIssueFormValue> closeIssueForm = getCloseIssueForm();
        closeIssueForm.ifPresent(cif -> {
            final Boolean checkboxValue = cif.getCheckbox().orElse(Boolean.FALSE);
            if (checkboxValue) {
                final Optional<IssueStatus> issueStatus = closeIssueForm.get().getIssueStatus();
                final Optional<String> closeIssueComment = closeIssueForm.get().getComment();
                final Optional<IssueStatus> status = issueService.findStatus(issueStatus.map(IssueStatus::getKey).orElse(IssueStatus.FORWARDED));
                status.map(((OpenIssue) issue)::close);
                closeIssueComment.map(comment -> issue.addComment(comment, (User) threadPrincipalService.getPrincipal()));
            }
        });
    }

    private Optional<ProcessValue> getProcessCombobox() {
        final Object processesCombobox = properties.get(PROCESSES_COMBOBOX);

        if (Objects.isNull(processesCombobox)) {
            return Optional.empty();
        }

        return Optional.of((ProcessValue) processesCombobox);
    }

    private Optional<AssignIssueFormValue> getAssignIssueForm() {
        final Object assignIssueForm = properties.get(ASSIGN_ISSUE_FORM);

        if (Objects.isNull(assignIssueForm)) {
            return Optional.empty();
        }

        return Optional.of((AssignIssueFormValue) assignIssueForm);
    }

    private Optional<CloseIssueFormValue> getCloseIssueForm() {
        final Object assignIssueForm = properties.get(CLOSE_ISSUE_FORM);

        if (Objects.isNull(assignIssueForm)) {
            return Optional.empty();
        }

        return Optional.of((CloseIssueFormValue) assignIssueForm);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        final ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        final PropertySpec processCombobox = propertyFactoriesProvider
                .getFactory(PropertyType.PROCESS_COMBOBOX)
                .getElement(PROCESSES_COMBOBOX, TranslationKeys.PROCESS_ACTION, TranslationKeys.PROCESS_ACTION, issueType, issueReason);

        final PropertySpec assigneeElementsGroup = propertyFactoriesProvider
                .getFactory(PropertyType.ASSIGN_ISSUE_FORM)
                .getElement(ASSIGN_ISSUE_FORM, TranslationKeys.ACTION_ASSIGN_ISSUE, TranslationKeys.ACTION_ASSIGN_ISSUE, issueType, issueReason);

        final PropertySpec closeIssueForm = propertyFactoriesProvider
                .getFactory(PropertyType.CLOSE_ISSUE_FORM)
                .getElement(CLOSE_ISSUE_FORM, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ISSUE, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ISSUE, issueType, issueReason);

        builder.add(processCombobox);
        builder.add(assigneeElementsGroup);
        builder.add(closeIssueForm);

        return builder.build();
    }

    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(PROCESSES_COMBOBOX);

        if (value == null) {
            return "";
        }

        return ((ProcessValue) value).getName();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.PROCESS_ACTION).format();
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return issue == null;
    }
}
