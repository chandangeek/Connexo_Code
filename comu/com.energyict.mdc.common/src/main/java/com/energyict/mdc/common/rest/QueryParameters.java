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

    private final Integer start;
    private final Integer limit;
    private final JSONArray sort;

    @Inject
    public QueryParameters(@QueryParam("start") Integer start, @QueryParam("limit") Integer limit, @QueryParam("sort") JSONArray sort)  {
        this.start = start;
        this.limit = limit;
        this.sort = sort;
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
                    sortingColumns.add("ascending".equals(object.getString("direction"))?Order.ascending(object.getString("field")):Order.descending(object.getString("field")));
                }
            }
            return sortingColumns;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
