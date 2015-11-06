package com.elster.jupiter.tasks;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.logging.LogEntry;

import java.util.List;

/**
 * Created by igh on 5/11/2015.
 */
public interface TaskFinder {

    public TaskFinder with (Condition condition);

    public TaskFinder setStart (Integer start);

    public TaskFinder setLimit (Integer limit);

    public TaskFinder setCondition (Condition condition);

    public List<? extends RecurrentTask> find();

}
