package com.energyict.mdc.multisense.api.impl.utils;

import com.elster.jupiter.domain.util.QueryParameters;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * generic helper class to json-serialize a list of info-objects into a json format that is understood by our ExtJS paging component.
 * This class has a generic type so Miredot can generate better documentation
 */
public class PagedInfoList<T> {

    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    public List<Link> link;
    public List<T> data = new ArrayList<>();

    public PagedInfoList() {
    }

    /**
     * Create a Json serialized object for unpaged, complete, search results. So no paging was done prior to this method call.
     * This method will be used mostly in conjuncture with full-list getters, e.g. deviceType.getConfigurations()
     * Difference with fromPagedList() is that the total-property will be the actual total, not the faked pageSize+1 in case of full page
     *
     * @param infos The search results to page according to queryParameters
     * @param queryParameters The original query parameters used for building the list that is being returned. This is required as it is used to page
     *                        the list of infos
     * @param uriBuilder Builder pointing to base URL for the paged resource. Template needs already to have been resolved with parameters. Additional URL (paging) parameters will be added
     * @param uriInfo
     * @return A PagedInfoList that will be correctly serialized as JSON paging object
     */
    public static <T> PagedInfoList<T> from(List<T> infos, QueryParameters queryParameters, UriBuilder uriBuilder, UriInfo uriInfo)  {
        PagedInfoList<T> list = new PagedInfoList<>();
        if (uriInfo.getQueryParameters().containsKey("fields")) {
            uriBuilder.queryParam("fields", uriInfo.getQueryParameters().getFirst("fields"));
        }

        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            int limit = queryParameters.getLimit().get();
            int start = queryParameters.getStart().get();
            boolean hasNextPage = infos.size() > limit;
            boolean hasPreviousPage = start > 0;
            list.link = new ArrayList<>();
            list.link.add(Link.fromUriBuilder(uriBuilder.clone().queryParam("start", start).queryParam("limit", limit)).rel("current").title("current page").build());

            if (hasNextPage) {
                infos = infos.subList(0, limit);
                int newStart = limit + start;
                list.link.add(Link.fromUriBuilder(uriBuilder.clone().queryParam("start", newStart).queryParam("limit", limit)).rel("next").title("next page").build());
            }
            if (hasPreviousPage) {
                int newStart = Math.max(start - limit, 0);
                list.link.add(Link.fromUriBuilder(uriBuilder.clone().queryParam("start", newStart).queryParam("limit", limit)).rel("previous").title("previous page").build());
            }

        }
        list.data = infos;

        return list;
    }

    /**
     * Create a Json serialized object for unpaged, complete, search results. So no paging was done prior to this method call.
     * This method will be used mostly in conjuncture with full-list getters, e.g. deviceType.getConfigurations()
     * Difference with fromPagedList() is that the total-property will be the actual total, not the faked pageSize+1 in case of full page
     *
     * @param infos The search results to page according to queryParameters
     * @param queryParameters The original query parameters used for building the list that is being returned. This is required as it is used to page
     *                        the list if infos,
     * @return A PagedInfoList that will be correctly serialized as JSON paging object, understood by ExtJS
     */
    public static <T> PagedInfoList<T> from(List<T> infos, QueryParameters queryParameters) {
        PagedInfoList<T> list = new PagedInfoList<>();
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            int limit = queryParameters.getLimit().get();
            boolean hasNextPage = infos.size() > limit;

            if (hasNextPage) {
                infos = infos.subList(0, limit);
            }
        }
        list.data = infos;

        return list;
    }

}
