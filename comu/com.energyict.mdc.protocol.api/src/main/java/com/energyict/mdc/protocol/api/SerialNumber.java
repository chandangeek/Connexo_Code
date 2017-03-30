/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SerialNumber.java
 *
 * Created on 31 oktober 2003, 14:52
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;

import java.io.IOException;

/**
 * @author Koen
 */
public interface SerialNumber {

    String getSerialNumber(DiscoverInfo discoverInfo) throws IOException;

}