package com.elster.jupiter.metering;

import com.elster.jupiter.util.HasName;
import com.google.common.base.Optional;

public interface AmrSystem extends HasName {
	int getId();
	Meter newMeter(String amrId);
	Meter newMeter(String amrId, String mRID);
	Optional<Meter> findMeter(String amrId); 
}
