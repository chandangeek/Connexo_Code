/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.Registration;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DataModelUpgrader {

    void upgrade(DataModel dataModel, Version version);

    Registration register(DifferencesListener listener);

}
