package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.DataMapper;

public interface FinderService {
	<T> Finder<T> wrap(DataMapper<T> tupleHandler);
}
