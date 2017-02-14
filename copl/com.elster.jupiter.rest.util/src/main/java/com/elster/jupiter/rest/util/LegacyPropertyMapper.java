/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.validation.Path;

/**
 * Created by bvn on 5/4/15.
 */
public interface LegacyPropertyMapper {
    public Path getLegacyPropertyPath(Path propertyPath);
}
