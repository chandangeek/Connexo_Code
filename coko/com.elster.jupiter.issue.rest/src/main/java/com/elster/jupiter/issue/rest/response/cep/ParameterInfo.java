package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;

import java.util.List;

import static com.elster.jupiter.util.Checks.is;

public class ParameterInfo {
    private ParameterControl control;
    private ParameterConstraintInfo constraint;

    private String key;
    private String label;
    private String suffix;
    private String help;
    private Object defaultValue;
    private List<String> dependOn;
    private List<Object> defaultValues;

    public ParameterInfo(ParameterDefinition parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("ParameterInfo is initialized with the null ParameterDefinition value");
        }

        this.key = parameter.getKey();
        this.label = parameter.getLabel();
        this.suffix = is(parameter.getSuffix()).emptyOrOnlyWhiteSpace() ? null : parameter.getSuffix();
        this.help = is(parameter.getHelp()).emptyOrOnlyWhiteSpace() ? null : parameter.getHelp();
        this.defaultValue = parameter.getDefaultValue();
        this.defaultValues = parameter.getDefaultValues();
        this.dependOn = parameter.getDependOn();
        this.control = parameter.getControl();
        if (parameter.getConstraint() != null) {
            this.constraint = new ParameterConstraintInfo(parameter.getConstraint());
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

    public List<Object> getDefaultValues() {
        return defaultValues;
    }

    public List<String> getDependOn() {
        return dependOn;
    }
}
