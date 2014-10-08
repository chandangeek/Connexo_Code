package com.elster.jupiter.issue.impl.actions.parameters;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.AbstractParameterDefinition;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssigneeRoleParameter extends AbstractParameterDefinition {

    private final Thesaurus thesaurus;
    private final IssueService issueService;

    public AssigneeRoleParameter(Thesaurus thesaurus, IssueService issueService) {
        this.thesaurus = thesaurus;
        this.issueService = issueService;
    }

    @Override
    public String getKey() {
        return Parameter.ASSIGNEE_ROLE.getKey();
    }

    @Override
    public ParameterControl getControl() {
        return ComboBoxControl.COMBOBOX;
    }

    @Override
    public List<Object> getDefaultValues() {
        Query<AssigneeRole> query = issueService.query(AssigneeRole.class);
        List<AssigneeRole> teams = query.select(Condition.TRUE);
        List<Object> result = new ArrayList<>(teams.size());
       for (AssigneeRole role : teams) {
            ComboBoxControl.Values value = new ComboBoxControl.Values();
            value.id = role.getId();
            value.title = role.getName();
            result.add(value);
        }
        return result;
    }

    @Override
    public ParameterConstraint getConstraint() {
        return new ParameterConstraint() {
            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public String getRegexp() {
                return null;
            }

            @Override
            public Integer getMin() {
                return null;
            }

            @Override
            public Integer getMax() {
                return null;
            }

            @Override
            public List<ParameterViolation> validate(String value, String paramKey) {
                /*
                List<ParameterViolation> errors = new ArrayList<>();
                if (!is(value).emptyOrOnlyWhiteSpace()) {
                    long id = Long.parseLong(value);
                    if(!issueService.findAssigneeRole(id).isPresent()){
                        errors.add(new ParameterViolation(paramKey, MessageSeeds.ACTION_WRONG_ASSIGNEE.getTranslated(thesaurus)));
                    }
                }
                return errors;
                */
                return Collections.emptyList();
            }
        };
    }

    @Override
    public String getLabel() {
        return MessageSeeds.PARAMETER_ASSIGNEE_ROLE.getTranslated(thesaurus);
    }
}
