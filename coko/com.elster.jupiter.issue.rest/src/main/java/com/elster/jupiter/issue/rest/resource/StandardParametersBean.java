package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.util.conditions.Order;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class StandardParametersBean {
    private static Logger LOG = Logger.getLogger(StandardParametersBean.class.getName());

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

    public MultivaluedMap<String, String> getQueryParameters(){
        return getUriInfo().getQueryParameters();
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }
}
