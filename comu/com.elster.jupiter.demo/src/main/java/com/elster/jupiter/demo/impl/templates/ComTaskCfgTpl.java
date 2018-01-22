/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

/**
 * Created by h165696 on 1/11/2018.
 */
public class ComTaskCfgTpl {

    private ComTaskTpl comTaskTpl;
    private SecurityPropertySetTpl securityPropertySetTpl;
    private String connectionTask;
    private boolean ignoreNextExecutionSpecs;

    ComTaskCfgTpl(ComTaskTpl comTaskTpl, SecurityPropertySetTpl securityPropertySetTpl, String connectionTask, boolean ignoreNextExecutionSpecs) {
        this.comTaskTpl = comTaskTpl;
        this.securityPropertySetTpl = securityPropertySetTpl;
        this.connectionTask = connectionTask;
        this.ignoreNextExecutionSpecs = ignoreNextExecutionSpecs;
    }

    ComTaskCfgTpl(ComTaskTpl comTaskTpl) {
        this.comTaskTpl = comTaskTpl;
        this.securityPropertySetTpl = SecurityPropertySetTpl.NO_SECURITY_DEFAULT;
        this.connectionTask = "";
        this.ignoreNextExecutionSpecs = false;
    }

    public ComTaskTpl getComTaskTpl() {
        return comTaskTpl;
    }

    public SecurityPropertySetTpl getSecurityPropertySetTpl() {
        return securityPropertySetTpl;
    }

    public String getConnectionTask() {
        return connectionTask;
    }

    public boolean getIgnoreNextExecutionSpecs() {
        return ignoreNextExecutionSpecs;
    }

}