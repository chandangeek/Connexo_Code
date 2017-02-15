/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import aQute.bnd.annotation.ProviderType;

import java.util.logging.Handler;

@ProviderType
public interface TaskLogHandler {

    Handler asHandler();

}
