package com.elster.jupiter.rest.util;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

public interface RestQuery<T> {
	List<T> select(MultivaluedMap<String,String> map);
}
