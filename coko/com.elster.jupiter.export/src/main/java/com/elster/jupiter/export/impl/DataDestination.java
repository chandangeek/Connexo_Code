/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ExportData;

import java.util.List;
import java.util.logging.Logger;

interface DataDestination extends Destination {
    void send(List<ExportData> data, TagReplacerFactory tagReplacerFactory, Logger logger);

    @Override
    default Type getType() {
        return Type.DATA;
    }
}
