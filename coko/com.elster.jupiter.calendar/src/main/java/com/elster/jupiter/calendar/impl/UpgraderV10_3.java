package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

class UpgraderV10_3 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_3(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 3));

        createNewCategories();
    }

    private void createNewCategories() {
        for (OutOfTheBoxCategory outOfTheBoxCategory : new OutOfTheBoxCategory[] {OutOfTheBoxCategory.WORKFORCE, OutOfTheBoxCategory.COMMANDS}) {
            CategoryImpl category = this.dataModel.getInstance(CategoryImpl.class);
            category.init(outOfTheBoxCategory.getDefaultDisplayName());
            category.save();
        }
    }

}
