/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol;

import java.util.logging.Logger;

/**
 * Adds capability to transmit a protocol logger
 */
public interface ProtocolLoggingSupport  {

    void setProtocolLogger(Logger protocolLogger);

}
