package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.domain.util.QueryParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Convenience class to wrap all supported ExtJS constructs regarding paging and sorting (both simple sort and multi sort)
 * Can be passed as-is to the {@link com.elster.jupiter.domain.util.Finder} who knows what to do with it
 */
public class JsonQueryParameters implements QueryParameters {

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
    public JsonQueryParameters(@Context UriInfo uriInfo) {
        queryParameters = uriInfo.getQueryParameters();
    }

    @Override
    public Optional<Integer> getStart() {
        return getIntegerOrEmpty(EXTJS_START);
    }

    @Override
    public Optional<Integer> getLimit() {
        return getIntegerOrEmpty(EXTJS_LIMIT);
    }

    @Override
    public List<Order> getSortingColumns() {
        try {
            List<Order> sortingColumns = new ArrayList<>();
            String singleSortDirection = queryParameters.getFirst(EXTJS_DIR);
            String sort = queryParameters.getFirst(EXTJS_SORT);
            if (singleSortDirection != null && sort != null) {
                sortingColumns.add(EXTJS_ASCENDING.equalsIgnoreCase(singleSortDirection) ? Order.ascending(sort) : Order.descending(sort));
            } else if (sort != null && !sort.isEmpty()) {
                JSONArray jsonArray = new JSONArray(sort);
                for (int index = 0; index < jsonArray.length(); index++) {
                    JSONObject object = jsonArray.getJSONObject(index);
                    sortingColumns.add(EXTJS_ASCENDING.equalsIgnoreCase(object.getString(EXTJS_DIRECTION)) ? Order.ascending(object.getString(EXTJS_FIELD)) : Order.descending(object.getString(EXTJS_FIELD)));
                }
            }
            return sortingColumns;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Integer> getIntegerOrEmpty(String name) {
        String start = queryParameters.getFirst(name);
        if (start != null) {
            return Optional.of(Integer.parseInt(start));
        }
        return Optional.empty();
    }

    public String getLike() {
        return queryParameters.getFirst(EXTJS_LIKE);
    }

}
