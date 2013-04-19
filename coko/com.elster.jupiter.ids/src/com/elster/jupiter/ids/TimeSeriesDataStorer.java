package com.elster.jupiter.ids;

import java.util.Date;

public interface TimeSeriesDataStorer {
	void add(TimeSeries timeSeries, Date dateTime , Object... values);
	boolean overrules();
	StorerStats execute();
}
