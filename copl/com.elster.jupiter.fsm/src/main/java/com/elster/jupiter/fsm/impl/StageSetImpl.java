/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.impl.constraints.AtLeastOneStage;
import com.elster.jupiter.fsm.impl.constraints.NoDuplicateStageName;
import com.elster.jupiter.fsm.impl.constraints.Unique;
import com.elster.jupiter.orm.Table;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Unique(message = "{" + MessageSeeds.Keys.UNIQUE_STAGE_SET_NAME + "}", groups = { Save.Create.class, Save.Update.class })
@AtLeastOneStage(groups = { Save.Create.class, Save.Update.class })
@NoDuplicateStageName(groups = {Save.Create.class, Save.Update.class})
public class StageSetImpl implements StageSet {

    public enum Fields {
        NAME("name"),
        STAGES("stages");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String name;
    @Valid
    private List<Stage> stages = new ArrayList<>();
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;

    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Stage> getStages() {
        return Collections.unmodifiableList(this.stages);
    }

    @Override
    public Optional<Stage> getStageByName(String name) {
        return stages.stream()
                .filter(stage -> stage.getName().equals(name))
                .findAny();
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void addStage(Stage stage) {
        this.stages.add(stage);
    }
}
