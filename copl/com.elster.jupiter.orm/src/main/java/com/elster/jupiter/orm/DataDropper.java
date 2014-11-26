package com.elster.jupiter.orm;

import java.time.Instant;

public interface DataDropper {
	void drop(Instant upTo);
}
