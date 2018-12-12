/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasName;

@ProviderType
public interface FileImporterProperty extends HasName {

    ImportSchedule getImportSchedule();

    String getDisplayName();

    Object getValue();

    boolean useDefault();

}
