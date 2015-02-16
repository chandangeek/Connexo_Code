package com.elster.jupiter.metering;

import com.elster.jupiter.util.HasName;
import java.util.Optional;

public interface AmrSystem extends HasName {
	int getId();
	Meter newMeter(String amrId);
	Meter newMeter(String amrId, String mRID);
	EndDevice newEndDevice(String amrId);
	EndDevice newEndDevice(String amrId, String mRID);
	Optional<Meter> findMeter(String amrId);
	boolean is(KnownAmrSystem knownAmrSystem);
    /**
	* @since 1.1
	*/
	Optional<Meter> lockMeter(String amrId);
}
