package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.conditions.Order;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QueryParameters {

    public static final String EXTJS_ASCENDING = "ASC";

    private final Integer start;
    private final Integer limit;
    private final JSONArray sort;
    private final String singleSortName;
    private final String singeSortDirection;

    @Inject
    public QueryParameters(@QueryParam("start") Integer start, @QueryParam("limit") Integer limit,
                           @QueryParam("sort") JSONArray sort,
                           @QueryParam("sort") String singleSortName,  @QueryParam("dir") String singeSortDirection)  {
        this.start = start;
        this.limit = limit;
        this.sort = sort;
        this.singleSortName = singleSortName;
        this.singeSortDirection = singeSortDirection;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getLimit() {
        return limit;
    }

    public List<Order> getSortingColumns()  {
        try {
            List<Order> sortingColumns = new ArrayList<>();
            if (sort!=null) {
                for (int index=0; index<sort.length(); index++) {
                    JSONObject object = sort.getJSONObject(index);
                    sortingColumns.add(EXTJS_ASCENDING.equals(object.getString("direction"))?Order.ascending(object.getString("field")):Order.descending(object.getString("field")));
                }
            } else if (singleSortName!=null && singeSortDirection!=null) {
                sortingColumns.add(EXTJS_ASCENDING.equals(singeSortDirection) ? Order.ascending(singleSortName) : Order.descending(singleSortName));
            }
            return sortingColumns;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
