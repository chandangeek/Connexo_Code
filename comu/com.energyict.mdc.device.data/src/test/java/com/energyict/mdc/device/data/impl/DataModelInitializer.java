package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;

/**
 * Defines the behavior of a component that will
 * initialize the ORM datamodel of the device configuraltion module.
 * All initialization calls are made from a transaction aware environment.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (17:01)
 */
public interface DataModelInitializer {

    public void initializeDataModel (DataModel dataModel);

}
