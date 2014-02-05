package com.energyict.mdc.device.configuration.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagedInfoList {

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
    public static Map forJson(String jsonListName, List infos, boolean couldHaveNextPage) {
        Map<String, Object> map = new HashMap<>();
        map.put("total", infos.size()+(couldHaveNextPage?1:0));
        map.put(jsonListName, infos);
        return map;
    }
}
