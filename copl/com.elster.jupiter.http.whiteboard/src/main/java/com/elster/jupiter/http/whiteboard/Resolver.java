/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard;

import aQute.bnd.annotation.ProviderType;

import java.net.URL;

@ProviderType
public interface Resolver {
	URL getResource(String name);
}
