/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import java.time.Instant;
import java.util.List;

@Path("/filters")
public class JsonQueryFilterResource {
    public static final String DEFAUL_FILTER_NAME = "test";
    public static final XmlAdapter<String, Integer> STRING_INTEGER_XML_ADAPTER = new XmlAdapter<String, Integer>() {
        @Override
        public Integer unmarshal(String v) throws Exception {
            if (v != null) {
                return Integer.parseInt(v.replace('_', '0'));
            }
            return 0;
        }

        @Override
        public String marshal(Integer v) throws Exception {
            return "";
        }
    };

    public JsonQueryFilterResource(){}

    @GET
    @Path("/single/string")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public String getStringFilterProperty(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getString(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/list/string")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<String> getStringFilterList(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getStringList(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/single/integer")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Integer getIntegerFilterProperty(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getInteger(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/list/integer")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<Integer> getIntegerFilterList(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getIntegerList(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/single/long")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Long getLongFilterProperty(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getLong(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/list/long")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<Long> getLongFilterList(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getLongList(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/single/instant")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Instant getInstantFilterProperty(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getInstant(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/list/instant")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<Instant> getInstantFilterList(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getInstantList(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/single/boolean")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Boolean getBooleanFilterProperty(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getBoolean(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/list/boolean")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<Boolean> getBooleanFilterList(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getBooleanList(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/single/complex")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public String getComplexFilterProperty(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getComplexProperty(DEFAUL_FILTER_NAME);
    }

    @GET
    @Path("/single/adapter")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Integer getFilterPropertyForAdapter(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getProperty(DEFAUL_FILTER_NAME, STRING_INTEGER_XML_ADAPTER);
    }

    @GET
    @Path("/list/adapter")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<Integer> getFilterListForAdapter(@BeanParam JsonQueryFilter queryFilter){
        return queryFilter.getPropertyList(DEFAUL_FILTER_NAME, STRING_INTEGER_XML_ADAPTER);
    }
}
