package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link ComJob} interface
 * that represents a single {@link ComTaskExecution} that can be executed in parallel.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-20 (14:10)
 */
@XmlRootElement
public class ComTaskExecutionJob implements ComJob {

    private ServerComTaskExecution comTaskExecution;

    // For xml serialization purposes only
    public ComTaskExecutionJob () {
        super();
    }

    public ComTaskExecutionJob (ServerComTaskExecution comTaskExecution) {
        this();
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public boolean isGroup () {
        return false;
    }

    @Override
    @XmlElement
    public ScheduledConnectionTask getConnectionTask () {
        return (ScheduledConnectionTask) this.comTaskExecution.getConnectionTask();
    }

    @Override
    @XmlElement
    public List<ComTaskExecution> getComTaskExecutions () {
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>(1);
        comTaskExecutions.add(this.comTaskExecution);
        return comTaskExecutions;
    }

}
