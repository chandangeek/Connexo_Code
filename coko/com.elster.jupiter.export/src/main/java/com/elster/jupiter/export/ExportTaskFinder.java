/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.stream.Stream;

@ProviderType
public interface ExportTaskFinder {

    ExportTaskFinder setStart(int start);

    ExportTaskFinder setLimit(int limit);

    ExportTaskFinder ofApplication(String application);

    List<? extends ExportTask> find();

    Stream<? extends ExportTask> stream();
}
