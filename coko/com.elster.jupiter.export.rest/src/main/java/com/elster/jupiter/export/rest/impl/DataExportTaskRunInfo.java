/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@XmlRootElement
public class DataExportTaskRunInfo {
    public Instant startOn;
    public Instant exportWindowStart;
    public Instant exportWindowEnd;
    public Instant updateDataStart;
    public Instant updateDataEnd;
    public DataExportTaskInfo task;
}
