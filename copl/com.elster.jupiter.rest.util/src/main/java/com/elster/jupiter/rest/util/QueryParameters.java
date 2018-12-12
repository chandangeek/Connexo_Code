/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.UriInfo;

/**
 */
@Deprecated // use JsonQueryParameters from now on
public final class QueryParameters implements MultivaluedMap<String, String> {

    private final MultivaluedMap<String, String> map;

    @Inject
    @Deprecated
    public QueryParameters(@Context UriInfo uriInfo) {
        map = uriInfo.getQueryParameters();
    }

    private QueryParameters(MultivaluedMap<String, String> map) {
        this.map = map;
    }

    public static QueryParameters wrap(MultivaluedMap<String,String> map) {
        return new QueryParameters(map);
    }

    @Override
    public void add(String key, String value) {
        map.add(key, value);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<Entry<String,List<String>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    @Override
    public List<String> get(Object key) {
        return map.get(key);
    }

    @Override
    public String getFirst(String key) {
        return map.getFirst(key);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public List<String> put(String key, List<String> value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> m) {
        map.putAll(m);
    }

    @Override
    public void putSingle(String key, String value) {
        map.putSingle(key, value);
    }

    @Override
    public List<String> remove(Object key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<List<String>> values() {
        return map.values();
    }

    public int getStartInt() {
        return Math.max(0, getLastValue(map, "start"));
    }

    public int getLimit() {
        return getLastValue(map, "limit");
    }

    public int determineTotal(int resultSize) {
        if (resultSize == getLimit() + 1) {
            return getStartInt() + resultSize;
        }
        return getStartInt() + resultSize;
    }

    public <T> List<T> clipToLimit(List<T> result) {
        if (getLimit() >= 0 && getLimit() < result.size()) {
            return result.subList(0, getLimit());
        }
        return result;
    }

    private int getLastValue(MultivaluedMap<String, String> map, String key) {
        String intString = getLast(map,key);
        try {
            return intString == null ? -1 : Integer.parseInt(intString);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String getLast(MultivaluedMap<String, String> map, String key) {
        List<String> values = map.get(key);
        return values == null || values.isEmpty() ? null : values.get(values.size() - 1);
    }

	@Override
	public void addAll(String arg0, String... arg1) {
		map.addAll(arg0,arg1);

	}

	@Override
	public void addAll(String arg0, List<String> arg1) {
		map.addAll(arg0,arg1);
	}

	@Override
	public void addFirst(String arg0, String arg1) {
		map.addFirst(arg0,arg1);
	}

	@Override
	public boolean equalsIgnoreValueOrder(MultivaluedMap<String, String> arg0) {
		return map.equalsIgnoreValueOrder(arg0);
	}


}
