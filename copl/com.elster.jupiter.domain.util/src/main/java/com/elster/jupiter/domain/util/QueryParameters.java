/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.util.conditions.Order;
import java.util.List;
import java.util.Optional;

/**
 * Created by bvn on 5/6/15.
 */
public interface QueryParameters {

    Optional<Integer> getStart();

    Optional<Integer> getLimit();

    List<Order> getSortingColumns();
}
