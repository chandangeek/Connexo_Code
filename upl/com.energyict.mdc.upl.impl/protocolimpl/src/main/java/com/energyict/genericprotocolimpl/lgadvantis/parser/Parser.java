package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.CosemAttribute;
import com.energyict.genericprotocolimpl.lgadvantis.Task;

/**
 * A Parser converts AXDR encoded structures to RegisterValue and ProfileData
 * objects.
 * 
 * Furthermore a parser can put this in the appropriate place of a Task object.
 * This way there is no need for a fixed return type.
 * 
 */

public interface Parser {

	void parse(AbstractDataType dataType, Task task) throws IOException;

	void setAttribute(CosemAttribute attribute);

}
