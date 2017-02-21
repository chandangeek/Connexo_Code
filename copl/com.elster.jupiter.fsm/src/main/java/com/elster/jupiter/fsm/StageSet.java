/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.List;

public interface StageSet extends HasName, HasId {
    List<Stage> getStages();
}
