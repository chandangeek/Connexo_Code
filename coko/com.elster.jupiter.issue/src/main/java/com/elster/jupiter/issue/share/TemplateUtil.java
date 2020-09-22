/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.issue.share;

/**
 * Class to handle common variables which are used in Template classes
 */
public class TemplateUtil {

    private static Long ruleId = null;
    private static String ruleName = null;

    public TemplateUtil(Long id, String name) {
        this.ruleId = id;
        this.ruleName = name;
    }

    public static Long getRuleId() {
        return ruleId;
    }

    public static String getRuleName() {
        return ruleName;
    }
}
