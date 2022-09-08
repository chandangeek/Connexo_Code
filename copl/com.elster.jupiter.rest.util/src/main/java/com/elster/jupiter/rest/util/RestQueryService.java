/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.domain.util.Query;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface RestQueryService {
    <T> RestQuery<T> wrap(Query<T> query);
}
