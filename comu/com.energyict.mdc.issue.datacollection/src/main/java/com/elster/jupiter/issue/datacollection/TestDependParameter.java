package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.share.cep.AbstractParameterDefenition;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.cep.StringParameterConstraint;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestDependParameter extends AbstractParameterDefenition {
    private ParameterConstraint constraint = new StringParameterConstraint(false, 2, 80);

    @Override
    public String getKey() {
        return "key";
    }

    @Override
    public String getLabel() {
        return "Label";
    }

    @Override
    public String getSuffix() {
        return "Suffix";
    }

    @Override
    public String getDefaultValue() {
        return "One";
    }

    @Override
    public String getHelp() {
        return "Help text";
    }

    @Override
    public ParameterControl getControl() {
        return null;
    }

    @Override
    public ParameterConstraint getConstraint() {
        return constraint;
    }

    @Override
    public boolean isDependant() {
        return true;
    }

    @Override
    public List<String> getDefaultValues() {
        return Arrays.asList("One", "Two", "Three");
    }

    @Override
    public ParameterDefinition getValue(final Map<String, Object> parameters) {
        if (parameters != null) {
            Object leadinParamValue = parameters.get("leading");
            String value = String.valueOf(leadinParamValue);
            if (value.startsWith("some")){
                return new TestDependParameter(){
                    @Override
                    public String getDefaultValue() {
                        Object userValue = parameters.get(TestDependParameter.this.getKey());
                        if(userValue != null){
                            return userValue.toString();
                        }
                        return super.getDefaultValue();
                    }

                    @Override
                    public List<String> getDefaultValues() {
                        return Arrays.asList("Four", "Five", "Six");
                    }
                };
            }
        }
        return this;
    }
}
