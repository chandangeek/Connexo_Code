package com.energyict.mdc.device.configuration.rest.impl;

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

    public void setCouldHaveNextPage(){
        couldHaveNextPage = true;
    }

    private PagedInfoList(String jsonListName, List<T> infos, boolean couldHaveNextPage) {
        this.jsonListName = jsonListName;
        this.couldHaveNextPage = couldHaveNextPage;
        this.infos = infos;
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
     * @param couldHaveNextPage Indicates that there is/could be a next page of search results
     * @return A map that will be correctly serialized as JSON paging object, understood by ExtJS
     */
    public static <T> PagedInfoList<T> forJson(String jsonListName, List<T> infos, boolean couldHaveNextPage) {
        return new PagedInfoList<>(jsonListName, infos, couldHaveNextPage);
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
