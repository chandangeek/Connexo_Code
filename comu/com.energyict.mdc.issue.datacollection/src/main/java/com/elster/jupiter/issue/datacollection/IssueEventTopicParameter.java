package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.share.cep.*;

import java.util.Map;

public class IssueEventTopicParameter extends AbstractParameterDefenition{
    private static final ParameterConstraint CONSTRAINT = new StringParameterConstraint(false, 2, 80);

    @Override
    public String getKey() {
        return "eventTopic";
    }

    @Override
    public ParameterControl getControl() {
        return null;
    }

    @Override
    public ParameterConstraint getConstraint() {
        return CONSTRAINT;
    }

    @Override
    public String getLabel() {
        return "Event topic";
    }

    @Override
    public String getSuffix() {
        return "";
    }

    @Override
    public ParameterDefinition getValue(Map<String, Object> parameters) {
        return this;
    }
}
