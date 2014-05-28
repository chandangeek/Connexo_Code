package com.elster.jupiter.issue.share.cep;

public enum ParameterDefinitionContext {
    RULE("parameters."),
    ACTION("actions.parameters.");

    private String context;

    private ParameterDefinitionContext(String context) {
        this.context = context;
    }

    public String wrapKey(String key) {
        return context + key;
    }
}
