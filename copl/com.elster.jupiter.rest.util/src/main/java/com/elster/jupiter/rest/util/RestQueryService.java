package com.elster.jupiter.rest.util;

import com.elster.jupiter.domain.util.Query;

public interface RestQueryService {
	<T> RestQuery<T> wrap(Query<T> query);
}
