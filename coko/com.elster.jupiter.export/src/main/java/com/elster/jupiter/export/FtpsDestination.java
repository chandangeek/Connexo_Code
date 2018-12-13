/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface FtpsDestination extends FtpDataExportDestination {
    String TYPE_IDENTIFIER = "FTPSX";
}
