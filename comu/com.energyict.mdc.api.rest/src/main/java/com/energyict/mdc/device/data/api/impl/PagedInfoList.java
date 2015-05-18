package com.energyict.mdc.device.data.api.impl;

import com.elster.jupiter.domain.util.QueryParameters;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * generic helper class to json-serialize a list of info-objects into a json format that is understood by our ExtJS paging component.
 */
public class PagedInfoList {

    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    public List<Link> links;
    public List<?> data = new ArrayList<>();

    public PagedInfoList() {
    }

    /**
     * Create a Json serialized object for unpaged, complete, search results. So no paging was done prior to this method call.
     * This method will be used mostly in conjuncture with full-list getters, e.g. deviceType.getConfigurations()
     * Difference with fromPagedList() is that the total-property will be the actual total, not the faked pageSize+1 in case of full page
     *
     * @param infos The search results to page according to queryParameters
     * @param queryParameters The original query parameters used for building the list that is being returned. This is required as it is used to page
     *                        the list if infos,
     * @param uriBuilder
     * @return A PagedInfoList that will be correctly serialized as JSON paging object, understood by ExtJS
     */
    public static PagedInfoList from(List<?> infos, QueryParameters queryParameters, UriBuilder uriBuilder) {
        PagedInfoList list = new PagedInfoList();
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            int limit = queryParameters.getLimit().get();
            int start = queryParameters.getStart().get();
            boolean hasNextPage = infos.size() > limit;
            boolean hasPreviousPage = start > 0;
            list.links = new ArrayList<>();
            list.links.add(Link.fromUriBuilder(uriBuilder.clone().queryParam("start", start).queryParam("limit", limit)).rel("curr").title("current page").build());

            if (hasNextPage) {
                infos = infos.subList(0, limit);
                int newStart = limit + start;
                list.links.add(Link.fromUriBuilder(uriBuilder.clone().queryParam("start", newStart).queryParam("limit", limit)).rel("next").title("next page").build());
            }
            if (hasPreviousPage) {
                int newStart = Math.max(start - limit, 0);
                list.links.add(Link.fromUriBuilder(uriBuilder.clone().queryParam("start", newStart).queryParam("limit", limit)).rel("prev").title("previous page").build());
            }

            list.data = infos;
        }

        return list;
    }

}
