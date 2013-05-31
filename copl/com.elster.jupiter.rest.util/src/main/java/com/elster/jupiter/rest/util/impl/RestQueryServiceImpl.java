package com.elster.jupiter.rest.util.impl;

import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.*;

@Component (name = "com.elster.jupiter.rest.util")
public class RestQueryServiceImpl implements RestQueryService {
	
	@Override
	public <T> RestQuery<T> wrap(Query<T> query) {
		return new RestQueryImpl<>(query);
	}
	

}
