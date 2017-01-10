
package com.energyict.mdc.device.alarms.impl.actions;

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
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.AssignPropertyFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.device.alarms.impl.records.DeviceAlarmAssigneeImpl;
import com.energyict.mdc.device.alarms.security.Privileges;
import com.energyict.mdc.dynamic.PropertySpecService;

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

public class AssignDeviceAlarmAction extends AbstractIssueAction {

    private static final String NAME = "AssignAlarmAction";
    public static final String ASSIGNEE = NAME + ".assignee";

    private final IssueService issueService;
    private final UserService userService;
    private final ThreadPrincipalService threadPrincipalService;

    private Issue issue;

    @Inject
    public AssignDeviceAlarmAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService) {
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
            result.success(getThesaurus().getFormat(MessageSeeds.ACTION_ALARM_WAS_UNASSIGNED).format());
        } else if (assignee.getUser() == null) {
            result.success(getThesaurus().getFormat(MessageSeeds.ACTION_ALARM_WAS_ASSIGNED_WORKGROUP).format(assignee.getWorkGroup().getName()));
        } else if (assignee.getWorkGroup() == null) {
            result.success(getThesaurus().getFormat(MessageSeeds.ACTION_ALARM_WAS_ASSIGNED_USER).format(assignee.getUser().getName()));
        } else {
            result.success(getThesaurus().getFormat(MessageSeeds.ACTION_ALARM_WAS_ASSIGNED_USER_AND_WORKGROUP).format(assignee.getUser().getName(), assignee.getWorkGroup().getName()));
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
            }
        }
        return new DeviceAlarmAssigneeImpl();
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
            }
        }
        return Optional.empty();
    }

    @Override
    public AssignDeviceAlarmAction setIssue(Issue issue) {
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
                        .named(ASSIGNEE, TranslationKeys.ACTION_ASSIGN_ALARM)
                        .fromThesaurus(this.getThesaurus())
                        .setDefaultValue(getDefaultValues(issue))
                        .finish());
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_ASSIGN_ALARM).format();
    }

    @Override
    public boolean isApplicableForUser(User user) {
        return super.isApplicableForUser(user) && user.getPrivileges().stream().filter(p -> Privileges.Constants.ASSIGN_ALARM.equals(p.getName())).findAny().isPresent();
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
            }
            return null;
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

    public static class Assignee extends HasIdAndName {

        private Optional<User> user;
        private Optional<WorkGroup> workgroup;
        private Optional<String> comment;

        public Assignee(User user, WorkGroup workgroup, String comment) {
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
            }
            return "";
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
            }
            return "";
        }

    }
}
