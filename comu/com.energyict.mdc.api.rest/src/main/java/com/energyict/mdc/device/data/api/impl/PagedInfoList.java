package com.energyict.mdc.device.data.api.impl;

import com.elster.jupiter.domain.util.QueryParameters;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

/**
 * generic helper class to json-serialize a list of info-objects into a json format that is understood by our ExtJS paging component.
 */
@JsonSerialize(using = PagedInfoList.Serializer.class)
public class PagedInfoList {

    public List<Link> links;
    public List<?> data = new ArrayList<>();

    private final boolean hasNextPage;

    public PagedInfoList(List<?> infos, QueryParameters queryParameters) {
        this.hasNextPage=queryParameters.getLimit().isPresent() && infos.size()>queryParameters.getLimit().get();
        if (couldHaveNextPage) {
            infos=infos.subList(0,queryParameters.getLimit().get());
        }
        int total = infos.size();
        if (queryParameters.getStart().isPresent()) {
            total+=queryParameters.getStart().get();
        }
        if (couldHaveNextPage) {
            total++;
        }

        return new PagedInfoList(infos);
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    /**
     * Create a Json serialized object for unpaged, complete, search results. So no paging was done prior to this method call.
     * This method will be used mostly in conjuncture with full-list getters, e.g. deviceType.getConfigurations()
     * Difference with fromPagedList() is that the total-property will be the actual total, not the faked pageSize+1 in case of full page
     *
     * @param jsonListName The name of the list property in JSON
     * @param infos The search results to page according to queryParameters
     * @param queryParameters The original query parameters used for building the list that is being returned. This is required as it is used to page
     *                        the list if infos,
     * @return A PagedInfoList that will be correctly serialized as JSON paging object, understood by ExtJS
     */
    public static PagedInfoList fromCompleteList(String jsonListName, List<?> infos, QueryParameters queryParameters) {
        int totalCount = infos.size();
        if (queryParameters.getStart().isPresent() && queryParameters.getStart().get() < infos.size()) {
            int startIndex = queryParameters.getStart().get();
            int endIndex = infos.size();
            if(queryParameters.getLimit().isPresent()) {
                endIndex = queryParameters.getStart().get()+queryParameters.getLimit().get();
                if( endIndex > infos.size())
                    endIndex = infos.size();
            }
            infos = infos.subList(startIndex, endIndex);
        }
        return new PagedInfoList(jsonListName, infos, totalCount);
    }

    public static Object fromCompleteList(String jsonListName, List<?> infos, QueryParameters queryParameters, UriInfo uriInfo) {
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
