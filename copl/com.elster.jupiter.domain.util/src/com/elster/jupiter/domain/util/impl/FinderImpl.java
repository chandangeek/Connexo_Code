package com.elster.jupiter.domain.util.impl;

import java.util.List;
import java.util.Map;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataMapper;

class FinderImpl<T> implements Finder<T> {
	private final DataMapper<T> tupleHandler;
	
	FinderImpl(DataMapper<T> tupleHandler) {
		this.tupleHandler = tupleHandler;
	}
	
	@Override
	public List<T> find(Map<String,Object> map) {	
		return tupleHandler.find(map);
	}

	@Override
	public List<T> findLenient(Map<String, String> map) {
		return tupleHandler.findLenient(map);
	}

}
