package com.elster.jupiter.rest.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * generic helper class to json-serialize a list of info-objects into a json format that is understood by our ExtJS infinite scrolling component.
 */
@JsonSerialize(using = InfiniteScrollingInfoList.Serializer.class)
public class InfiniteScrollingInfoList {

    private final String jsonListName;
    private boolean couldHaveNextPage;
    private List<?> infos = new ArrayList<>();
    private Integer totalCount;

    public int getTotal() {
        return totalCount;
    }

    public List<?> getInfos() {
        return ImmutableList.copyOf(infos);
    }

    private InfiniteScrollingInfoList(String jsonListName, List<?> infos, QueryParameters queryParameters, int totalCount) {
        this.jsonListName = jsonListName;
        this.infos = infos;
        couldHaveNextPage=queryParameters.getLimit()!=-1 && infos.size()>queryParameters.getLimit();
        if (couldHaveNextPage) {
            this.infos=infos.subList(0,queryParameters.getLimit());
        }
        this.totalCount = totalCount;
    }

    public static InfiniteScrollingInfoList asJson(String jsonListName, List<?> infos, QueryParameters queryParameters, Integer totalCount) {
        return new InfiniteScrollingInfoList(jsonListName, infos, queryParameters, totalCount);
    }

    public static class Serializer extends JsonSerializer<InfiniteScrollingInfoList> {

        @Override
        public void serialize(InfiniteScrollingInfoList value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            jgen.writeNumberField("total", value.getTotal());
            jgen.writeArrayFieldStart(value.jsonListName);
            for (Object info : value.infos) {
                jgen.writeObject(info);
            }
            jgen.writeEndArray();
            jgen.writeEndObject();
        }
    }}
