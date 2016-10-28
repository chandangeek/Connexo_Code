package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
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
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlBuilder;

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
        getCommentFromParameters(properties).ifPresent(comment -> issue.addComment(comment, (User)threadPrincipalService.getPrincipal()));
        result.success(getThesaurus().getFormat(MessageSeeds.ACTION_ISSUE_WAS_ASSIGNED).format());
        return result;
    }

    @SuppressWarnings("unchecked")
    private IssueAssignee getAssigneeFromParameters(Map<String, Object> properties) {
        Object value = properties.get(ASSIGNEE);
        if (value != null) {
            String assigneeId = getPropertySpec(ASSIGNEE).get().getValueFactory().toStringValue(value);
            return issueService.findIssueAssignee(Long.valueOf(assigneeId), null);
        }
        return new IssueAssigneeImpl();
    }

    private Optional<String> getCommentFromParameters(Map<String, Object> properties) {
        Object value = properties.get(COMMENT);
        if (value != null) {
            @SuppressWarnings("unchecked")
            String comment = getPropertySpec(COMMENT).get().getValueFactory().toStringValue(value);
            return Optional.ofNullable(comment);
        }
        return Optional.empty();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Assignee[] possibleAssignees = userService.getUserQuery()
                .select(Condition.TRUE, Order.ascending("authenticationName"))
                .stream()
                .map(Assignee::new)
                .toArray(Assignee[]::new);
        return Arrays.asList(
            getPropertySpecService()
                    .specForValuesOf(new AssigneeValueFactory())
                    .named(ASSIGNEE, TranslationKeys.ASSIGNACTION_PROPERTY_ASSIGNEE)
                    .fromThesaurus(this.getThesaurus())
                    .markRequired()
                    .addValues(possibleAssignees)
                    .markExhaustive()
                    .finish(),
            getPropertySpecService()
                    .stringSpec()
                    .named(COMMENT, TranslationKeys.ASSIGNEACTION_PROPERTY_COMMENT)
                    .fromThesaurus(this.getThesaurus())
                    .finish());
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_ASSIGN_ISSUE).format();
    }

    @Override
    public boolean isApplicableForUser(User user) {
        return super.isApplicableForUser(user) && user.getPrivileges().stream().filter(p -> Privileges.Constants.ASSIGN_ISSUE.equals(p.getName())).findAny().isPresent();
    }

    private class AssigneeValueFactory implements ValueFactory<Assignee> {
        @Override
        public Assignee fromStringValue(String stringValue) {
            return userService
                    .getUser(Long.valueOf(stringValue).longValue())
                    .map(Assignee::new)
                    .orElse(null);
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
            }
            else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, Assignee value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            }
            else {
                builder.addNull(Types.VARCHAR);
            }
        }

        @Override
        public boolean isValid(Assignee value) {
            return value.getId() > 0;
        }
    }

    static class Assignee extends HasIdAndName {

        private User user;

        Assignee(User user) {
            this.user = user;
        }

        @Override
        public Long getId() {
            return user.getId();
        }

        @Override
        public String getName() {
            return user.getName();
        }
    }
}
