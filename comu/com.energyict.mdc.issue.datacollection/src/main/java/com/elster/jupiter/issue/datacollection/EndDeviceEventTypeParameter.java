package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.share.cep.AbstractParameterDefenition;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.StringParameterConstraint;

public class EndDeviceEventTypeParameter extends AbstractParameterDefenition{
    private static final ParameterConstraint CONSTRAINT = new StringParameterConstraint(false, 2, 80);

    @Override
    public String getKey() {
        return "endDeviceEventType";
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
        return "End device event type";
    }
}
