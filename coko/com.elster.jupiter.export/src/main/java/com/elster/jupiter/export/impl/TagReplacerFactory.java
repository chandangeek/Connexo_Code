/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.StructureMarker;

public interface TagReplacerFactory {

    TagReplacer forMarker(StructureMarker structureMarker);
}
