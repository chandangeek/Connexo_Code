/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.YesNoAnswer;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 19/02/14
 * Time: 9:12
 * To change this template use File | Settings | File Templates.
 */
public interface UsagePointDetail extends Effectivity {

    YesNoAnswer isCollarInstalled();

    boolean isCurrent();

    boolean conflictsWith(UsagePointDetail other);

    UsagePoint getUsagePoint();

    void update();
}
