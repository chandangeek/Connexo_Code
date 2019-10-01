package com.elster.jupiter.issue.share;

import java.util.List;
import java.util.Map;

import com.elster.jupiter.util.HasId;

public interface AllowsComTaskFiltering {

    List<? extends HasId> getExcludedComTasks(Map<String, Object> properties);
}
