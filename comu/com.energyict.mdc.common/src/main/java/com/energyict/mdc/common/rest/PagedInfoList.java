package com.energyict.mdc.common.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * generic helper class to json-serialize a list of info-objects into a json format that is understood by our ExtJS paging component.
 */
@JsonSerialize(using = PagedInfoList.Serializer.class)
public class PagedInfoList {

    private final String jsonListName;
    private List<?> infos = new ArrayList<>();
    private final int total;

    public int getTotal() {
        return total;
    }

    public List<?> getInfos() {
        return ImmutableList.copyOf(infos);
    }

    private PagedInfoList(String jsonListName, List<?> infos, int total) {
        this.jsonListName = jsonListName;
        this.infos = infos;
        this.total = total;
    }

    /**
     * Create a Json serialized object for paged search results.
     * E.g.
     *    ("deviceTypes", {deviceTypeInfo1, deviceTypeInfo2}, queryParameters}
     *    with queryParameters,limit=2 (TWO)
     *    returning 2 results when 2 were asked implicates a full page and the the field 'total' is increased by 1 to indicate there could be a next page.
     *
     * will end up serialized into the following JSON
     *
     *   {
     *       "total":3,
     *       "deviceTypes":[{"name":"...",...},{"name":"...",...}]
     *   }
     * @param jsonListName The name of the list property in JSON
     * @param infos The search results to assign to the list property
     * @param queryParameters The original query parameters used for building the list that is being returned. This is required as it is used to determine
     *                        if the returned 'page' was full, if so, total is incremented by 1 to indicate to ExtJS there could be a next page.
     * @return A map that will be correctly serialized as JSON paging object, understood by ExtJS
     */
    @Deprecated
    public static PagedInfoList asJson(String jsonListName, List<?> infos, QueryParameters queryParameters) {
        return fromPagedList(jsonListName,  infos, queryParameters);
    }

    public static PagedInfoList fromPagedList(String jsonListName, List<?> infos, QueryParameters queryParameters) {
        boolean couldHaveNextPage=queryParameters.getLimit()!=null && infos.size()>queryParameters.getLimit();
        if (couldHaveNextPage) {
            infos=infos.subList(0,queryParameters.getLimit());
        }
        int total = infos.size();
        if (queryParameters.getStart()!=null) {
            total+=queryParameters.getStart();
        }
        if (couldHaveNextPage) {
            total++;
        }

        return new PagedInfoList(jsonListName, infos, total);
    }

    public static PagedInfoList fromCompleteList(String jsonListName, List<?> infos, QueryParameters queryParameters) {
        int totalCount = infos.size();
        if (queryParameters.getStart() != null && queryParameters.getStart() < infos.size()) {
            infos = infos.subList(queryParameters.getStart(), infos.size());
        }
        return new PagedInfoList(jsonListName, infos, totalCount);
    }

    public static class Serializer extends JsonSerializer<PagedInfoList> {

        @Override
        public void serialize(PagedInfoList value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            jgen.writeNumberField("total", value.getTotal());
            jgen.writeArrayFieldStart(value.jsonListName);
            for (Object info : value.infos) {
                jgen.writeObject(info);
            }
            jgen.writeEndArray();
            jgen.writeEndObject();
        }
    }
}
