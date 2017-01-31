/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

/**
 * Created by bvn on 1/5/17.
 */
@ProviderType
public interface DifferenceCommand extends Difference {
    public void execute();
}
