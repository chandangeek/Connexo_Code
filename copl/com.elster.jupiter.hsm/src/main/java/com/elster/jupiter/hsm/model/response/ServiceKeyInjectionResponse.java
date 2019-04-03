/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.model.response;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ServiceKeyInjectionResponse {
    String getWarning();
}