
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.PropertyFactoriesProvider;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.issue.share.entity.values.AssignIssueFormValue;
import com.elster.jupiter.issue.share.entity.values.CloseIssueFormValue;
import com.elster.jupiter.issue.share.entity.values.EndPointValue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class WebServiceNotificationAction extends AbstractIssueAction {

    private static final String NAME = "WebServiceNotificationAction";
    public static final String WEBSERVICE = NAME + ".webService";
    public static final String CLOSE = NAME + ".close";
    public static final String CLOSE_STATUS = NAME + ".closeStatus";
    private static final String ASSIGN_ISSUE_FORM = "AssignIssueForm";
    private static final String CLOSE_ISSUE_FORM = "CloseIssueForm";

    private final IssueService issueService;
    private final ThreadPrincipalService threadPrincipalService;
    private final PropertyFactoriesProvider propertyFactoriesProvider;

    @Inject
    public WebServiceNotificationAction(final DataModel dataModel,
                                        final Thesaurus thesaurus,
                                        final PropertySpecService propertySpecService,
                                        final IssueService issueService,
                                        final ThreadPrincipalService threadPrincipalService,
                                        final PropertyFactoriesProvider propertyFactoriesProvider) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.threadPrincipalService = threadPrincipalService;
        this.propertyFactoriesProvider = propertyFactoriesProvider;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        final IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();

        final Optional<EndPointConfiguration> endPointConfiguration = getEndPointConfiguration();

        if (endPointConfiguration.isPresent()) {
            final EndPointConfiguration endPointConfig = endPointConfiguration.get();
            final List<IssueWebServiceClient> issueWebServiceClients = ((IssueServiceImpl) issueService).getIssueWebServiceClients();
            final Optional<IssueWebServiceClient> issueWebServiceClient = issueWebServiceClients
                    .stream()
                    .filter(webServiceClient -> webServiceClient.getWebServiceName().equals(endPointConfig.getWebServiceName()))
                    .findFirst();

            if (issueWebServiceClient.isPresent()) {
                final IssueWebServiceClient webServiceClient = issueWebServiceClient.get();
                if (webServiceClient.call(issue, endPointConfig)) {
                    assignIssueSubAction(issue);
                    closeIssueSubAction(issue);
                    result.success(getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CALLED).format());
                } else {
                    result.fail(getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CALLED_FAILED).format());
                }
            } else {
                result.fail(getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_ENDPOINT_DOES_NOT_EXIST).format());
            }
        } else {
            result.fail(getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_ENDPOINT_CONFIGURATION_DOES_NOT_EXIST).format());
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

    private Optional<EndPointConfiguration> getEndPointConfiguration() {
        final Object value = properties.get(WEBSERVICE);

        if (Objects.isNull(value)) {
            return Optional.empty();
        }

        return Optional.of(((EndPointValue) value).getEndPointConfiguration());
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

        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        final PropertySpec webServiceNotificationDropdown = propertyFactoriesProvider
                .getFactory(PropertyType.ENDPOINT_COMBOBOX)
                .getElement(WEBSERVICE, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION);

        final PropertySpec assigneeElementsGroup = propertyFactoriesProvider
                .getFactory(PropertyType.ASSIGN_ISSUE_FORM)
                .getElement(ASSIGN_ISSUE_FORM, TranslationKeys.ACTION_ASSIGN_ISSUE, TranslationKeys.ACTION_ASSIGN_ISSUE);

        final PropertySpec closeIssueForm = propertyFactoriesProvider
                .getFactory(PropertyType.CLOSE_ISSUE_FORM)
                .getElement(CLOSE_ISSUE_FORM, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ISSUE, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ISSUE);

        builder.add(webServiceNotificationDropdown);
        builder.add(assigneeElementsGroup);
        builder.add(closeIssueForm);

        return builder.build();
    }

    @Override
    public String getFormattedProperties(Map<String, Object> props) {

        StringBuilder stringBuilder = new StringBuilder();

        Object webServiceProperties = props.get(WEBSERVICE);

        if (webServiceProperties != null) {
            stringBuilder.append(String.format("%s/", Optional.ofNullable(((EndPointValue) webServiceProperties).getEndPointConfiguration()).get().getName()));
        }

        Object assigneeProperties = props.get(ASSIGN_ISSUE_FORM);

        if (assigneeProperties != null) {
            stringBuilder.append(String.format("%s/%s",
                    (((AssignIssueFormValue) assigneeProperties).getWorkgroup())
                            .map(WorkGroup::getName).orElse(getThesaurus().getFormat(TranslationKeys.UNASSIGNED).format()),
                    (((AssignIssueFormValue) assigneeProperties).getUser())
                            .map(User::getName).orElse(getThesaurus().getFormat(TranslationKeys.UNASSIGNED).format())));
        }

        Object closeIssueFormProperties = props.get(CLOSE_ISSUE_FORM);

        if (closeIssueFormProperties != null) {
            stringBuilder.append(String.format("/%s", ((CloseIssueFormValue) closeIssueFormProperties).getIssueStatus()
                    .map(IssueStatus::getName)
                    .orElse(getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ISSUE_CLOSE_STATUS_COMBOBOX_DEFAULT_VALUE).format())));
        }

        return stringBuilder.toString();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION).format();
    }

    @Override
    public boolean isApplicableForUser(User user) {
        return super.isApplicableForUser(user) && user.getPrivileges().stream().anyMatch(p -> Privileges.Constants.ACTION_ISSUE.equals(p.getName()));
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return issue == null;
    }
}
