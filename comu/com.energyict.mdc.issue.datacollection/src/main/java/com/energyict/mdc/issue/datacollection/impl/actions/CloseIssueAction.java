/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.actions;

import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.StringAreaFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CloseIssueAction extends AbstractIssueAction {

    private static final String NAME = "CloseIssueAction";
    public static final String CLOSE_STATUS = NAME + ".status";
    public static final String COMMENT = NAME + ".comment";

    private final IssueService issueService;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    protected CloseIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();

        Optional<IssueStatus> closeStatus = getStatusFromParameters(properties);
        if (!closeStatus.isPresent()) {
            result.fail(getThesaurus().getFormat(TranslationKeys.CLOSE_ACTION_WRONG_STATUS).format());
            return result;
        }
        if (isApplicable(issue)) {
            ((OpenIssue) issue).close(closeStatus.get());
            getCommentFromParameters(properties).ifPresent(comment -> issue.addComment(comment, (User)threadPrincipalService.getPrincipal()));
            result.success(getThesaurus().getFormat(TranslationKeys.CLOSE_ACTION_ISSUE_CLOSED).format());
        } else {
            result.fail(getThesaurus().getFormat(TranslationKeys.CLOSE_ACTION_ISSUE_ALREADY_CLOSED).format());
        }
        return result;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(
                getPropertySpecService()
                        .specForValuesOf(new StatusValueFactory())
                        .named(CLOSE_STATUS, TranslationKeys.CLOSE_ACTION_PROPERTY_CLOSE_STATUS)
                        .describedAs(TranslationKeys.CLOSE_ACTION_PROPERTY_CLOSE_STATUS)
                        .fromThesaurus(getThesaurus())
                        .markRequired()
                        .addValues(this.getPossibleStatuses())
                        .markExhaustive()
                        .finish());
        builder.add(getPropertySpecService()
                .specForValuesOf(new CommentsFactory())
                .named(COMMENT, TranslationKeys.CLOSE_ACTION_PROPERTY_COMMENT)
                .describedAs(TranslationKeys.CLOSE_ACTION_PROPERTY_COMMENT)
                .fromThesaurus(getThesaurus())
                .finish());
        return builder.build();
    }

    private Status[] getPossibleStatuses() {
        List<IssueStatus> statuses = issueService.query(IssueStatus.class).select(Where.where("isHistorical").isEqualTo(Boolean.TRUE));
        return statuses.stream().map(Status::new).toArray(Status[]::new);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.CLOSE_ACTION_CLOSE_ISSUE).format();
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return super.isApplicable(issue) && IssueStatus.OPEN.equals(issue.getStatus().getKey());
    }

    @Override
    public boolean isApplicableForUser(User user) {
        return super.isApplicableForUser(user) && user.getPrivileges().stream().filter(p -> Privileges.Constants.CLOSE_ISSUE.equals(p.getName())).findAny().isPresent();
    }

    private Optional<IssueStatus> getStatusFromParameters(Map<String, Object> properties){
        Object value = properties.get(CLOSE_STATUS);
        if (value != null) {
            @SuppressWarnings("unchecked")
            String statusKey = getPropertySpec(CLOSE_STATUS).get().getValueFactory().toStringValue(value);
            return issueService.findStatus(statusKey);
        }
        return Optional.empty();
    }

    private Optional<String> getCommentFromParameters(Map<String, Object> properties) {
        Object value = properties.get(COMMENT);
        if (value != null) {
            return this.getPropertySpec(COMMENT)
                    .map(PropertySpec::getValueFactory)
                    .map(valueFactory -> valueFactory.toStringValue(value));
        }
        return Optional.empty();
    }

    static class Status extends HasIdAndName {

        private IssueStatus status;

        Status(IssueStatus status) {
            this.status = status;
        }

        @Override
        public String getId() {
            return status.getKey();
        }

        @Override
        public String getName() {
            return status.getName();
        }
    }

    private class StatusValueFactory implements ValueFactory<Status> {
        @Override
        public Status fromStringValue(String stringValue) {
            return issueService.findStatus(stringValue).map(Status::new).orElse(null);
        }

        @Override
        public String toStringValue(Status status) {
            return status.getId();
        }

        @Override
        public Class<Status> getValueType() {
            return Status.class;
        }

        @Override
        public Status valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(Status object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, Status value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            }
            else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, Status value) {
            if (value != null) {
                builder.addObject(this.valueToDatabase(value));
            }
            else {
                builder.addNull(Types.VARCHAR);
            }
        }

        @Override
        public boolean isValid(Status value) {
            return !Checks.is(value.getId()).empty();
        }
    }

    public static class Comment implements HasName {

        private String value;

        public Comment(String value) {
            this.value = value;
        }

        @Override
        public String getName() {
            return value;
        }
    }

    private class CommentsFactory implements ValueFactory<Comment>, StringAreaFactory {
        @Override
        public Comment fromStringValue(String stringValue) {
            return new Comment(stringValue);
        }

        @Override
        public String toStringValue(Comment value) {
            return value.getName();
        }

        @Override
        public Class<Comment> getValueType() {
            return Comment.class;
        }

        @Override
        public Comment valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(Comment object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, Comment value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, Comment value) {
            if (value != null) {
                builder.addObject(this.valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }

        @Override
        public boolean isValid(Comment value) {
            return !Checks.is(value.getName()).empty();
        }
    }
}