package com.elster.jupiter.issue.impl.actions.parameters;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.AbstractParameterDefinition;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssigneeGroupParameter extends AbstractParameterDefinition {

    private final Thesaurus thesaurus;
    private final IssueService issueService;

    public AssigneeGroupParameter(Thesaurus thesaurus, IssueService issueService) {
        this.thesaurus = thesaurus;
        this.issueService = issueService;
    }

    @Override
    public String getKey() {
        return Parameter.ASSIGNEE_GROUP.getKey();
    }

    @Override
    public ParameterControl getControl() {
        return ComboBoxControl.COMBOBOX;
    }

    @Override
    public List<Object> getDefaultValues() {
        Query<AssigneeTeam> query = issueService.query(AssigneeTeam.class);
        List<AssigneeTeam> teams = query.select(Condition.TRUE);
        List<Object> result = new ArrayList<>(teams.size());
       for (AssigneeTeam team : teams) {
            ComboBoxControl.Values value = new ComboBoxControl.Values();
            value.id = team.getId();
            value.title = team.getName();
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
                return Collections.emptyList();
            }
        };
    }

    @Override
    public String getLabel() {
        return MessageSeeds.PARAMETER_ASSIGNEE_GROUP.getTranslated(thesaurus);
    }
}
