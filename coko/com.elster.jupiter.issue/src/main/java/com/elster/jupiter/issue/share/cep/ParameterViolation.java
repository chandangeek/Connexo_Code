package com.elster.jupiter.issue.share.cep;

public class ParameterViolation {
    private String parameterKey;
    private String messageSeed;
    private String componentName;
    private Object[] args = new Object[] {};

    /**
     * Creates new Parameter Violation instance
     * @param parameterKey unique key of parameter
     * @param message translated error message
     * @param args arguments which will be inserted into string (String.format(...) will be used, so you should use the '%s' signature)
     */
    public ParameterViolation(String parameterKey, String message, Object... args) {
        this(parameterKey, message, null, args);
    }

    /**
     * Creates new Parameter Violation instance
     * @param parameterKey unique key of parameter
     * @param messageSeed the key which points to translated string
     * @param componentName name of component in which the messageSeed was defined
     * @param args arguments which will be inserted into string (String.format(...) will be used, so you should use the '%s' signature)
     */
    public ParameterViolation(String parameterKey, String messageSeed, String componentName, Object... args) {
        this.parameterKey = parameterKey;
        this.messageSeed = messageSeed;
        this.componentName = componentName;
        this.args = args;
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public String getMessageSeed() {
        return messageSeed;
    }

    public Object[] getArgs() {
        return args;
    }

    public String getComponentName() {
        return componentName;
    }
}
