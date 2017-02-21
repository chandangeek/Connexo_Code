/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.StageSetBuilder;
import com.elster.jupiter.orm.DataModel;

public class StageSetBuilderImpl implements StageSetBuilder {
    private StageSetImpl underConstruction;
    private final DataModel dataModel;

    StageSetBuilderImpl(String name, DataModel dataModel) {
        this.dataModel = dataModel;
        underConstruction = new StageSetImpl();
        underConstruction.setName(name);
    }

    @Override
    public StageSetBuilder stage(String name) {
        StageImpl stage = new StageImpl(name, underConstruction);
        underConstruction.addStage(stage);
        return this;
    }

    @Override
    public StageSet add() {
        Save.CREATE.save(dataModel, underConstruction);
        return underConstruction;
    }
}
