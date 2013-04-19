package com.elster.jupiter.domain.util;

import java.util.List;
import java.util.Map;

public interface Finder<T> {
	List<T> find(Map<String,Object> map);
	List<T> findLenient(Map<String,String> map);
}
