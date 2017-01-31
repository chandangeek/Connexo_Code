/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import java.util.List;

/**
 * Interface for classes that filters matching Json paths.
 */
public interface ValueMatcher {

    /**
     * @param path
     * @return true if the given path matches this ValueMatcher, false otherwise.
     */
    boolean matches(List<String> path);
}
