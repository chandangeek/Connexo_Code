/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.webservicecall;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface DataExportSCCustomInfo {
    String getName();
    void fromString(String info);
}
