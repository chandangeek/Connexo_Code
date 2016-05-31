package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

class InstallerImpl implements FullInstaller {

    private final DataModel dataModel;

    @Inject
    InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create default Calendar categories.",
                this::createTOUCategory,
                logger
        );
    }

    private void createTOUCategory() {
        CategoryImpl category = this.dataModel.getInstance(CategoryImpl.class);
        category.init(CalendarServiceImpl.TIME_OF_USE_CATEGORY_NAME);
        category.save();
    }


}
