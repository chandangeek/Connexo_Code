package com.elster.jupiter.rest.util;

import com.elster.jupiter.domain.util.QueryParameters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

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
        this.infos = Collections.unmodifiableList(infos);
        this.total = total;
    }

    /**
     * Create a Json serialized object for paged search results. Paging has to be done with a Finder beforehand.
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
     * @return A PagedInfoList that will be correctly serialized as JSON paging object, understood by ExtJS
     */
    public static PagedInfoList fromPagedList(String jsonListName, List<?> infos, QueryParameters queryParameters) {
        boolean couldHaveNextPage=queryParameters.getLimit().isPresent() && infos.size()>queryParameters.getLimit().get();
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

        return new PagedInfoList(jsonListName, infos, total);
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
                if (endIndex > infos.size()) {
                    endIndex = infos.size();
                }
            }
            infos = infos.subList(startIndex, endIndex);
        }
        return new PagedInfoList(jsonListName, infos, totalCount);
    }

    public static <T> Collector<T, ArrayList<T>, PagedInfoList> toPagedInfoList(String jsonListName, QueryParameters queryParameters) {
        return new Collector<T, ArrayList<T>, PagedInfoList>() {
            @Override
            public Supplier<ArrayList<T>> supplier() {
                return ArrayList<T>::new;
            }

            @Override
            public BiConsumer<ArrayList<T>, T> accumulator() {
                return ArrayList::add;
            }

            @Override
            public BinaryOperator<ArrayList<T>> combiner() {
                return (list1, list2) -> {
                    list1.addAll(list2);
                    return list1;
                };
            }

            @Override
            public Function<ArrayList<T>, PagedInfoList> finisher() {
                return list -> fromCompleteList(jsonListName, list, queryParameters);
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.noneOf(Characteristics.class);
            }
        };
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
