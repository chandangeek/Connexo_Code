package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplateParameter;

public class CreationRuleTemplateParameterInfo {
    private String type;
    private String label;
    private boolean optional;
    private int min;
    private int max;
    private String suffix;

    public CreationRuleTemplateParameterInfo() {}

    public CreationRuleTemplateParameterInfo(CreationRuleTemplateParameter parameter) {
        if (parameter != null) {
            this.type = parameter.getType();
            this.label = parameter.getLabel();
            this.optional = parameter.isOptional();
            this.min = parameter.getMin();
            this.max = parameter.getMax();
            this.suffix = parameter.getSuffix();
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
