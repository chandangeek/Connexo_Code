/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.actions;

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
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmServiceImpl;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by H165696 on 9/5/2017.
 */
public class WebServiceNotificationAlarmAction extends AbstractIssueAction {

    private static final String NAME = "WebServiceNotificationAlarmAction";
    public static final String WEBSERVICE = NAME + ".webService";
    public static final String CLOSE = NAME + ".close";
    private static final String ASSIGN_ISSUE_FORM = "AssignAlarmForm";
    private static final String CLOSE_ISSUE_FORM = "CloseAlarmForm";

    private final DeviceAlarmService deviceAlarmService;
    private final IssueService issueService;
    private final PropertyFactoriesProvider propertyFactoriesProvider;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public WebServiceNotificationAlarmAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, DeviceAlarmService deviceAlarmService, UserService userService, ThreadPrincipalService threadPrincipalService, EndPointConfigurationService endPointConfigurationService, final PropertyFactoriesProvider propertyFactoriesProvider, final ThreadPrincipalService threadPrincipalService1) {
        super(dataModel, thesaurus, propertySpecService);
        this.deviceAlarmService = deviceAlarmService;
        this.issueService = issueService;
        this.propertyFactoriesProvider = propertyFactoriesProvider;
        this.threadPrincipalService = threadPrincipalService1;
    }

    @Override
    public IssueActionResult execute(final Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();

        Optional<EndPointConfiguration> endPointConfiguration = getEndPointConfiguration();

        if (endPointConfiguration.isPresent()) {
            final EndPointConfiguration endPointConfig = endPointConfiguration.get();
            final List<IssueWebServiceClient> alarmWebServiceClients = ((DeviceAlarmServiceImpl) deviceAlarmService).getIssueWebServiceClients();
            final Optional<IssueWebServiceClient> alarmWebServiceClient = alarmWebServiceClients.stream()
                    .filter(webServiceClient -> webServiceClient.getWebServiceName().equals(endPointConfig.getWebServiceName()))
                    .findFirst();

            if (alarmWebServiceClient.isPresent()) {
                final IssueWebServiceClient webServiceClient = alarmWebServiceClient.get();
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
                .getFactory(PropertyType.PROCESS_COMBOBOX)
                .getElement(WEBSERVICE, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION);

        final PropertySpec assigneeElementsGroup = propertyFactoriesProvider
                .getFactory(PropertyType.ASSIGN_ISSUE_FORM)
                .getElement(ASSIGN_ISSUE_FORM, TranslationKeys.ACTION_ASSIGN_ALARM, TranslationKeys.ACTION_ASSIGN_ALARM);

        final PropertySpec closeIssueForm = propertyFactoriesProvider
                .getFactory(PropertyType.CLOSE_ISSUE_FORM)
                .getElement(CLOSE_ISSUE_FORM, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ALARM, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ALARM);

        builder.add(webServiceNotificationDropdown);
        builder.add(assigneeElementsGroup);
        builder.add(closeIssueForm);

        return builder.build();
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

    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(WEBSERVICE);
        if (value != null) {
            return ((EndPointValue) value).getEndPointConfiguration().getName();
        }
        return "";
    }
}