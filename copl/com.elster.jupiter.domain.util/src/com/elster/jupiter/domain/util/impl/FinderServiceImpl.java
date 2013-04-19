package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.FinderService;
import com.elster.jupiter.orm.DataMapper;

public class FinderServiceImpl implements FinderService {

	@Override
	public <T> Finder<T> wrap(DataMapper<T> tupleHandler) {
		return new FinderImpl<> (tupleHandler);
	}

}
