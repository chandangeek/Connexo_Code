package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.conditions.Order;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class to wrap all supported ExtJS constructs regarding paging and sorting (both simple sort and multi sort)
 * Can be passed as-is to the {@link com.energyict.mdc.common.services.Finder} who knows what to do with it
 */
public class QueryParameters {

    // Below are the fields as they are added to the query by ExtJS
    private static final String EXTJS_ASCENDING = "ASC";
    private static final String EXTJS_START = "start";
    private static final String EXTJS_LIMIT = "limit";
    private static final String EXTJS_SORT = "sort";
    private static final String EXTJS_DIR = "dir";
    private static final String EXTJS_DIRECTION = "direction";
    private static final String EXTJS_FIELD = "property";
    private static final String EXTJS_LIKE = "like";

    private final MultivaluedMap<String, String> queryParameters;

    @Inject
    public QueryParameters(@Context UriInfo uriInfo) {
        queryParameters = uriInfo.getQueryParameters();
    }

    public Integer getStart() {
        return getIntegerOrNull(EXTJS_START);
    }

    public Integer getLimit() {
        return getIntegerOrNull(EXTJS_LIMIT);
    }

    public List<Order> getSortingColumns() {
        try {
            List<Order> sortingColumns = new ArrayList<>();
            String singleSortDirection = queryParameters.getFirst(EXTJS_DIR);
            String sort = queryParameters.getFirst(EXTJS_SORT);
            if (singleSortDirection != null && sort != null) {
                sortingColumns.add(EXTJS_ASCENDING.equals(singleSortDirection) ? Order.ascending(sort) : Order.descending(sort));
            } else if (sort != null && !sort.isEmpty()) {
                JSONArray jsonArray = new JSONArray(sort);
                for (int index = 0; index < jsonArray.length(); index++) {
                    JSONObject object = jsonArray.getJSONObject(index);
                    sortingColumns.add(EXTJS_ASCENDING.equals(object.getString(EXTJS_DIRECTION)) ? Order.ascending(object.getString(EXTJS_FIELD)) : Order.descending(object.getString(EXTJS_FIELD)));
                }
            }
            return sortingColumns;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getIntegerOrNull(String name) {
        String start = queryParameters.getFirst(name);
        if (start != null) {
            return Integer.parseInt(start);
        }
        return null;
    }

    public String getLike() {
        return queryParameters.getFirst(EXTJS_LIKE);
    }

    public Boolean getBoolean(String name) {
        String value = queryParameters.getFirst(name);
        return Boolean.parseBoolean(value);
    }
}
