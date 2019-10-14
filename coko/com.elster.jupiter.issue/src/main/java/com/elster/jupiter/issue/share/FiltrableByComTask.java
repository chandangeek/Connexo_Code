package com.elster.jupiter.issue.share;

import java.util.List;

import com.elster.jupiter.util.HasId;

public interface FiltrableByComTask {

    boolean matchesByComTask(final List<HasId> comTasks);
}
