package com.elster.jupiter.issue.share.cep;

public interface CreationRuleTemplateParameter {
    String getName();
    String getType();
    String getLabel();
    boolean isOptional();
    String getSuffix();
    int getMin();
    int getMax();
}

