/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SystemModeService {

	String COMPONENT_NAME = "MODE";

	/**
	 * The name of the property that provides the type
	 * of the server on which the server is running.
	 * When not set, the default online server is used.
	 */
	public static final String SERVER_TYPE_PROPERTY_NAME = "com.elster.jupiter.server.type";

	public boolean isOnlineMode();
}
