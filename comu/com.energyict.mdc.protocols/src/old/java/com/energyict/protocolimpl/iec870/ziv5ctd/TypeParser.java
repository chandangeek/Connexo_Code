/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TypeParser.java
 *
 * Created on 27 March 2006, 15:35
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

/** @author fbo */

public interface TypeParser {

    InformationObject parse(ByteArray byteArray);

}
