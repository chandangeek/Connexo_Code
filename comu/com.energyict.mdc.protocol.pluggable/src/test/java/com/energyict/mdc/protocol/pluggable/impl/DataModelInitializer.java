/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataModel;

/**
 * Defines the behavior of a component that will
 * initialize the ORM datamodel of the protocol pluggable module.
 * All initialization calls are made from a transaction aware environment.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-27 (13:07)
 */
public interface DataModelInitializer {

    public void initializeDataModel (DataModel dataModel);

}