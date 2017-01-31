/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * generic helper class to json-serialize a list of info-objects into a json format that is understood by our ExtJS paging component.
 *
 */
@JsonSerialize(using = KorePagedInfoList.Serializer.class)
@Deprecated // please use the PagedInfoList migrated from MDC
public class KorePagedInfoList {

    private final String jsonListName;
    private boolean couldHaveNextPage;
    private List<?> infos = new ArrayList<>();
    private QueryParameters queryParameters;
    OptionalInt totalCount;

    public int getTotal() {
        return totalCount.orElseGet(() -> {
            int total = infos.size();
            total+=queryParameters.getStartInt();
            if (couldHaveNextPage) {
                total++;
            }
            return total;
        });
    }

    public List<?> getInfos() {
        return ImmutableList.copyOf(infos);
    }

    private KorePagedInfoList(String jsonListName, List<?> infos, QueryParameters queryParameters, OptionalInt totalCount) {
        this.totalCount = totalCount;
        this.queryParameters = queryParameters;
        this.jsonListName = jsonListName;
        this.infos = infos;
        couldHaveNextPage=queryParameters.getLimit()!=-1 && infos.size()>queryParameters.getLimit();
        if (couldHaveNextPage) {
            this.infos=infos.subList(0,queryParameters.getLimit());
        }
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
    public static KorePagedInfoList asJson(String jsonListName, List<?> infos, QueryParameters queryParameters) {
        return new KorePagedInfoList(jsonListName, infos, queryParameters, OptionalInt.empty());
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
     * @param queryParameters The original query parameters used for building the list that is being returned.
     * @param totalCount If provided the 'total' value will be populated with this value indicate to ExtJS there is the totalCount
     * @return A map that will be correctly serialized as JSON paging object, understood by ExtJS
     */
    public static KorePagedInfoList asJson(String jsonListName, List<?> infos, QueryParameters queryParameters, int totalCount) {
        return new KorePagedInfoList(jsonListName, infos, queryParameters, OptionalInt.of(totalCount));
    }

    public static class Serializer extends JsonSerializer<KorePagedInfoList> {

        @Override
        public void serialize(KorePagedInfoList value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
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
