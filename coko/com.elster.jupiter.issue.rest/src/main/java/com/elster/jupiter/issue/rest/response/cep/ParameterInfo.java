package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;

import java.util.List;

public class ParameterInfo {
    private ParameterControl control;
    private ParameterConstraintInfo constraint;

    private String key;
    private String label;
    private String suffix;
    private String help;
    private Object defaultValue;
    private List<Object> values;

    public ParameterInfo(ParameterDefinition parameter) {
        if (parameter != null) {
            this.key = parameter.getKey();
            this.label = parameter.getLabel();
            this.suffix = parameter.getSuffix();
            this.help = parameter.getHelp();
            this.defaultValue = parameter.getDefaultValue();
            this.values = parameter.getDefaultValues();

            this.control = parameter.getControl();
            if (parameter.getConstraint() != null) {
                this.constraint = new ParameterConstraintInfo(parameter.getConstraint());
            }
        }
    }

    public ParameterControl getControl() {
        return control;
    }

    public ParameterConstraintInfo getConstraint() {
        return constraint;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getHelp() {
        return help;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public List<Object> getValues() {
        return values;
    }
}
