package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.validation.DataValidationOccurence;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.DataValidationTask;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@UniqueName(groups = {Save.Create.class, Save.Update.class})
public final class DataValidationTaskImpl implements DataValidationTask {

    private long id;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;

    private Instant lastRun;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();

    private final DataModel dataModel;

    @Inject
    DataValidationTaskImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static DataValidationTaskImpl from(DataModel model,String name) {
        return model.getInstance(DataValidationTaskImpl.class).init(name);
    }

    private DataValidationTaskImpl init(String name) {
        this.name = name;
        return this;
    }

    @Override
    public void activate() {

    }

    @Override
    public DataValidationStatus execute(DataValidationOccurence taskOccurence) {
        return null;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public Instant getNextExecution() {
        return null;
    }

    @Override
    public void save() {
        if (getId() == 0) {
            dataModel.persist(this);
        } else {
            dataModel.update(this);
        }
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public EndDeviceGroup getEndDeviceGroup() {
        return endDeviceGroup.get();
    }

    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup.set(endDeviceGroup);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return getId() == ((DataValidationTaskImpl) o).getId();
    }

    public long getId() {
        return id;
    }


}
