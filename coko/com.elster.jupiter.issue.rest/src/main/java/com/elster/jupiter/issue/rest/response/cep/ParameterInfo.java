package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.ParameterDefinition;

import java.util.List;

public class ParameterInfo {
    private ParameterControlInfo control;
    private ParameterConstraintInfo constraint;

    private String key;
    private String label;
    private String suffix;
    private String help;
    private String defaultValue;
    private List<String> values;

    public ParameterInfo(ParameterDefinition parameter) {
        if (parameter != null) {
            this.key = parameter.getKey();
            this.label = parameter.getLabel();
            this.suffix = parameter.getSuffix();
            this.help = parameter.getHelp();
            this.defaultValue = parameter.getDefaultValue();
            this.values = parameter.getDefaultValues();

            this.control = new ParameterControlInfo(parameter.getControl());
            this.constraint = new ParameterConstraintInfo(parameter.getConstraint());
        }
    }

    public ParameterControlInfo getControl() {
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<String> getValues() {
        return values;
    }
}
