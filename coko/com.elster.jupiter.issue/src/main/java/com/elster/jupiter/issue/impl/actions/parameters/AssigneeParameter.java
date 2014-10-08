package com.elster.jupiter.issue.impl.actions.parameters;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.*;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssigneeParameter extends AbstractParameterDefinition {
    private final Thesaurus thesaurus;
    private final IssueService issueService;
    private final UserService userService;

    private static class AssigneeControl implements ParameterControl {
        private final AssigneeUserParameter userParameter;
        private final AssigneeRoleParameter roleParameter;
        private final AssigneeGroupParameter groupParameter;

        public AssigneeControl(IssueService issueService, UserService userService, Thesaurus thesaurus) {
            userParameter = new AssigneeUserParameter(thesaurus, userService);
            roleParameter = new AssigneeRoleParameter(thesaurus, issueService);
            groupParameter = new AssigneeGroupParameter(thesaurus, issueService);
        }

        public ParameterDefinition getUserControl(){
            return  userParameter;
        }

        public ParameterDefinition getRoleControl() {
            return roleParameter;
        }

        public ParameterDefinition getGroupControl() {
            return groupParameter;
        }

        @Override
        public String getXtype() {
            return "issueAssignee";
        }
    }

    private final AssigneeControl assigneeControl;

    public AssigneeParameter(IssueService issueService, UserService userService, Thesaurus thesaurus) {
        this.issueService = issueService;
        this.userService = userService;
        this.thesaurus = thesaurus;

        this.assigneeControl = new AssigneeControl(issueService, userService, thesaurus);
    }

    @Override
    public String getKey() {
        return Parameter.ASSIGNEE.getKey();
    }

    @Override
    public ParameterControl getControl() {
        return assigneeControl;
    }

    @Override
    public List<Object> getDefaultValues() {
        return Collections.emptyList();
    }

    @Override
    public ParameterConstraint getConstraint() {
        return new ParameterConstraint() {

            @Override
            public String getRegexp() {
                return null;
            }

            @Override
            public List<ParameterViolation> validate(String value, String paramKey) {
                List<ParameterViolation> errors = new ArrayList<>();
                if (!IssueAssignee.Types.USER.equalsIgnoreCase(value)
                        && IssueAssignee.Types.GROUP.equalsIgnoreCase(value)
                        && IssueAssignee.Types.ROLE.equalsIgnoreCase(value)){
                    errors.add(new ParameterViolation(paramKey, MessageSeeds.ACTION_WRONG_ASSIGNEE.getTranslated(thesaurus)));
                }
                return errors;
            }

            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public Integer getMin() {
                return null;
            }

            @Override
            public Integer getMax() {
                return null;
            }
        };
    }

    @Override
    public String getLabel() {
        return MessageSeeds.PARAMETER_ASSIGNEE.getTranslated(thesaurus);
    }
}
