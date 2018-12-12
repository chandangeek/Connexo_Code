/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import java.util.List;

/**
 * Interface for classes that will take an action on a given path and its value.
 */
public interface ActionForPath {

    void action(List<String> path, String value);

}
