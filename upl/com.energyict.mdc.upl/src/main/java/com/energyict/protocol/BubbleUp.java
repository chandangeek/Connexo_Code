/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol;

import java.io.IOException;

public interface BubbleUp {

    BubbleUpObject parseBubbleUpData(byte[] data) throws IOException;

}
