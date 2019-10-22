package com.elster.jupiter.bpm.rest.impl;

public class ProcessInstanceNodeInfoWithSubprocess {

    public ProcessInstanceNodeInfo nodeInfo;
    public ChildProcessInstanceLog childSubprocessLog;

    public ProcessInstanceNodeInfoWithSubprocess(ProcessInstanceNodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
        this.childSubprocessLog = null;
    }

    public ProcessInstanceNodeInfoWithSubprocess(ProcessInstanceNodeInfo nodeInfo,
            ChildProcessInstanceLog childSubprocessLog) {
        this.nodeInfo = nodeInfo;
        this.childSubprocessLog = childSubprocessLog;
    }
}
