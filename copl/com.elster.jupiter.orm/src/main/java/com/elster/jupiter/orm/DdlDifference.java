/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import java.util.List;

/**
 * Created by bvn on 1/5/17.
 */
public interface DdlDifference extends Difference {
    List<String> ddl();
}
