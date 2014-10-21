package com.elster.jupiter.time.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.RelativePeriodCategoryUsage;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class RelativePeriodCategoryImpl extends EntityImpl implements RelativePeriodCategory {
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;

    private List<RelativePeriodCategoryUsage> relativePeriodCategoryUsages = new ArrayList<>();

    @Inject
    public RelativePeriodCategoryImpl(DataModel dataModel) {
        super(dataModel);
    }

    static RelativePeriodCategoryImpl from (DataModel dataModel, String name) {
        return dataModel.getInstance(RelativePeriodCategoryImpl.class).initialize(name);
    }

    RelativePeriodCategoryImpl initialize(String name) {
        this.setName(name);
        return this;
    }

    void setName(String name) {
       this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
