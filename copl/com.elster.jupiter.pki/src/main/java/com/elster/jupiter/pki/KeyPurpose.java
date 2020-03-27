/*
 * Copyright (c) 2020 by Honeywell Inc. All rights reserved.
 */
package com.elster.jupiter.pki;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface KeyPurpose extends HasName {

    String getId();
}
