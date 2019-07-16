package com.elster.jupiter.issue.share.entity.values;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.properties.HasIdAndName;

public final class ProcessValue extends HasIdAndName {

    private final BpmProcessDefinition bpmProcess;

    public ProcessValue(final BpmProcessDefinition bpmProcess) {
        this.bpmProcess = bpmProcess;
    }

    @Override
    public Long getId() {
        return bpmProcess.getId();
    }

    @Override
    public String getName() {
        return bpmProcess.getProcessName();
    }

}
