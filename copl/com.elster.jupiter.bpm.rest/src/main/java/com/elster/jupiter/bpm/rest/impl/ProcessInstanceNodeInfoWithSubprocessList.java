package com.elster.jupiter.bpm.rest.impl;

import java.util.ArrayList;
import java.util.List;

public class ProcessInstanceNodeInfoWithSubprocessList {

    public static final String SUBPROCESS_TYPE = "Subprocess";

    public String processInstanceStatus;
    public List<ProcessInstanceNodeInfoWithSubprocess> list = new ArrayList<>();

    public ProcessInstanceNodeInfoWithSubprocessList() {

    }

    public ProcessInstanceNodeInfoWithSubprocessList(ProcessInstanceNodeInfos nodes,
            ChildProcessInstanceLogList childProcessList) {
        this.processInstanceStatus = nodes.processInstanceStatus;
        int subprocessIndex = 0;
        for (ProcessInstanceNodeInfo node : nodes.processInstanceNodes) {
            final ProcessInstanceNodeInfoWithSubprocess element;
            if (SUBPROCESS_TYPE.equals(node.type)) {
                element = new ProcessInstanceNodeInfoWithSubprocess(node,
                        childProcessList.childProcessInstanceLogList.get(subprocessIndex));
                subprocessIndex++;
            } else {
                element = new ProcessInstanceNodeInfoWithSubprocess(node);
            }
            list.add(element);
        }
    }
}
