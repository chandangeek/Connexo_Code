package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.QueryParameters;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(using = PagedInfoList.Serializer.class)
public class PagedInfoList<T> {

    private final String jsonListName;
    private boolean couldHaveNextPage;
    private List<T> infos = new ArrayList<>();

    public int getTotal() {
        return infos.size() + (couldHaveNextPage?1:0);
    }

    private PagedInfoList(String jsonListName, List<T> infos, QueryParameters queryParameters) {
        this.jsonListName = jsonListName;
        this.infos = infos;
        couldHaveNextPage=queryParameters.getLimit()!=null && infos.size()==queryParameters.getLimit();
    }

    /**
     * Create a Json serialized object for paged search results.
     * E.g.
     *    ("deviceTypes", {deviceTypeInfo1, deviceTypeInfo2}, true}
     *
     * will end up serialized into the following JSON
     *
     *   {
     *       "total":3,
     *       "deviceTypes":[{"name":"...",...},{"name":"...",...}]
     *   }
     * @param jsonListName The name of the list property in JSON
     * @param infos The search results to assign to the list property
     * @param queryParameters The original query parameters used for building the list that is being returned
     * @return A map that will be correctly serialized as JSON paging object, understood by ExtJS
     */
    public static <T> PagedInfoList<T> forJson(String jsonListName, List<T> infos, QueryParameters queryParameters) {
        return new PagedInfoList<>(jsonListName, infos, queryParameters);
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
