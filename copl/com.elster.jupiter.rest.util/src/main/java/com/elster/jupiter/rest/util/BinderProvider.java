/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import org.glassfish.hk2.utilities.Binder;

public interface BinderProvider {
	Binder getBinder();
}
