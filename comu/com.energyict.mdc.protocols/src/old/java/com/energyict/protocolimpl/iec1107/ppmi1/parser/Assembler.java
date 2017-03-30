/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.io.IOException;

public interface Assembler {

	void workOn(Assembly a) throws IOException;

}
