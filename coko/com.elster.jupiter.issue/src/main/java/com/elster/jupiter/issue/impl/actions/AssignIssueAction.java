
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.AssignPropertyFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.util.sql.SqlBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AssignIssueAction extends AbstractIssueAction {

    private static final String NAME = "AssignIssueAction";
    public static final String ASSIGNEE = NAME + ".assignee";
    public static final String COMMENT = NAME + ".comment";

    private IssueService issueService;
    private UserService userService;
    private ThreadPrincipalService threadPrincipalService;

    private Issue issue;

    @Inject
    public AssignIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.userService = userService;
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        IssueAssignee assignee = getAssigneeFromParameters(properties);
        issue.assignTo(assignee);
        issue.update();
        getCommentFromParameters(properties).ifPresent(comment -> issue.addComment(comment, (User) threadPrincipalService.getPrincipal()));
        if (assignee.getUser() == null && assignee.getWorkGroup() == null) {
            result.success(getThesaurus().getFormat(TranslationKeys.ACTION_ISSUE_UNASSIGNED).format());
        } else {
            result.success(getThesaurus().getFormat(TranslationKeys.ACTION_ISSUE_ASSIGNED).format());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private IssueAssignee getAssigneeFromParameters(Map<String, Object> properties) {
        Object value = properties.get(ASSIGNEE);
        if (value != null) {
            String assignee = getPropertySpec(ASSIGNEE).get().getValueFactory().toStringValue(value);
            try {
                JSONObject jsonData = new JSONObject(assignee);
                return issueService.findIssueAssignee(Long.valueOf(jsonData.get("userId").toString()), Long.valueOf(jsonData.get("workgroupId").toString()));
            } catch (JSONException e) {
                return new IssueAssigneeImpl();
            }
        }
        return new IssueAssigneeImpl();
    }

    private Optional<String> getCommentFromParameters(Map<String, Object> properties) {
        Object value = properties.get(ASSIGNEE);
        if (value != null) {
            @SuppressWarnings("unchecked")
            String assignee = getPropertySpec(ASSIGNEE).get().getValueFactory().toStringValue(value);
            try {
                JSONObject jsonData = new JSONObject(assignee);
                return Optional.ofNullable(jsonData.get("comment").toString());
            } catch (JSONException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public AssignIssueAction setIssue(Issue issue) {
        this.issue = issue;
        return this;
    }

    private Assignee getDefaultValues(Issue issue) {
        if (issue != null) {
            return new Assignee(issue.getAssignee().getUser(), issue.getAssignee().getWorkGroup(), "");
        }
        return new Assignee(null, null, null);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                getPropertySpecService()
                        .specForValuesOf(new AssigneeValueFactory())
                        .named(ASSIGNEE, TranslationKeys.ACTION_ASSIGN_ISSUE)
                        .fromThesaurus(this.getThesaurus())
                        .setDefaultValue(getDefaultValues(issue))
                        .finish());
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_ASSIGN_ISSUE).format();
    }

    @Override
    public boolean isApplicableForUser(User user) {
        return super.isApplicableForUser(user) && user.getPrivileges().stream().anyMatch(p -> Privileges.Constants.ASSIGN_ISSUE.equals(p.getName()));
    }

    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(ASSIGNEE);
        if (value != null) {
            return String.format("%s/%s",
                    (((Assignee) value).workgroup).map(WorkGroup::getName).orElse(getThesaurus().getFormat(TranslationKeys.UNASSIGNED).format()),
                    (((Assignee) value).user).map(User::getName).orElse(getThesaurus().getFormat(TranslationKeys.UNASSIGNED).format()));
        }
        return "";
    }

    private class AssigneeValueFactory implements ValueFactory<Assignee>, AssignPropertyFactory {
        @Override
        public Assignee fromStringValue(String stringValue) {

            try {
                JSONObject jsonData = new JSONObject(stringValue);
                User user = userService
                        .getUser(Long.parseLong(jsonData.get("userId").toString()))
                        .orElse(null);

                WorkGroup workgroup = userService
                        .getWorkGroup(Long.parseLong(jsonData.get("workgroupId").toString()))
                        .orElse(null);
                String comment = jsonData.get("comment").toString();
                return new Assignee(user, workgroup, comment);
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        public String toStringValue(Assignee object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<Assignee> getValueType() {
            return Assignee.class;
        }

        @Override
        public Assignee valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(Assignee object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, Assignee value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, Assignee value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    static class Assignee extends HasIdAndName {

        private Optional<User> user;
        private Optional<WorkGroup> workgroup;
        private Optional<String> comment;

        Assignee(User user, WorkGroup workgroup, String comment) {
            this.user = user != null ? Optional.of(user) : Optional.empty();
            this.workgroup = workgroup != null ? Optional.of(workgroup) : Optional.empty();
            this.comment = comment != null ? Optional.of(comment) : Optional.empty();
        }

        @Override
        public String getId() {
            try {
                JSONObject jsonId = new JSONObject();
                jsonId.put("userId", user.map(User::getId).orElse(-1L));
                jsonId.put("workgroupId", workgroup.map(WorkGroup::getId).orElse(-1L));
                jsonId.put("comment", comment.orElse(null));
                return jsonId.toString();
            } catch (JSONException e) {
                return "";
            }
        }

        @Override
        public String getName() {
            try {
                JSONObject jsonId = new JSONObject();
                jsonId.put("userName", user.map(User::getName).orElse(""));
                jsonId.put("workgroupName", workgroup.map(WorkGroup::getName).orElse(""));
                jsonId.put("comment", comment.orElse(""));
                return jsonId.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        }

    }
}
