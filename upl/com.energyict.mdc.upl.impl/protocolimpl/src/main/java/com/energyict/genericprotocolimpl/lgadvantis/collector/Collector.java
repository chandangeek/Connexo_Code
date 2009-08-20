package com.energyict.genericprotocolimpl.lgadvantis.collector;

import com.energyict.genericprotocolimpl.lgadvantis.ReadResult;
import com.energyict.genericprotocolimpl.lgadvantis.RtuMessageLink;
import com.energyict.genericprotocolimpl.lgadvantis.Task;

public interface Collector {

	ReadResult getAll(Task task, RtuMessageLink messageLink);

}
