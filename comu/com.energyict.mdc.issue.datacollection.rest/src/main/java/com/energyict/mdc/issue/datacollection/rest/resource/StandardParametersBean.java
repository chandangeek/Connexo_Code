package com.energyict.mdc.issue.datacollection.rest.resource;

import com.elster.jupiter.util.conditions.Order;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class StandardParametersBean {
    @QueryParam("sort")
    private List<String> sort;

    @QueryParam("start")
    @DefaultValue("0")
    private int start;

    @QueryParam("limit")
    @DefaultValue("50")
    private int limit;

    private UriInfo uriInfo;

    public StandardParametersBean(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public Order[] getOrder() {
        List<Order> orders = new ArrayList<Order>();
        for (String field : this.sort) {
            if(field.startsWith("-")) {
                orders.add(Order.descending(field.substring(1)));
            }
            else {
                orders.add(Order.ascending(field));
            }
        }
        return orders.toArray(new Order[orders.size()]);
    }

    public int getStart() {
        return start;
    }

    public int getLimit() {
        return limit;
    }

    public int getFrom(){
        return start + 1;
    }

    public int getTo(){
        return start + limit;
    }

    public List<String> get(Object key) {
        return getQueryParameters().get(key);
    }

    public String getFirst(Object key){
        List<String> values = get(key);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    public List<Long> getLong(Object key) {
        List<String> original = get(key);
        List<Long> resultList = new ArrayList<>();
        if (original != null) {
            for (String param : original){
                try {
                    resultList.add(Long.parseLong(param));
                } catch (NumberFormatException ex) {
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }
            }
        }
        return resultList;
    }

    public long getFirstLong(Object key){
        List<Long> values = getLong(key);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }
        return 0;
    }

    public MultivaluedMap<String, String> getQueryParameters(){
        return getUriInfo().getQueryParameters();
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }
}
