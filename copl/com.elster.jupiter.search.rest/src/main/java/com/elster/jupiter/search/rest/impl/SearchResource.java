package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.rest.MessageSeeds;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 6/1/15.
 */
@Path("/search")
public class SearchResource {

    private final SearchService searchService;
    private final ExceptionFactory exceptionFactory;
    private final PropertyInfoFactory propertyInfoFactory;

    @Inject
    public SearchResource(SearchService searchService, ExceptionFactory exceptionFactory, PropertyInfoFactory propertyInfoFactory) {
        this.searchService = searchService;
        this.exceptionFactory = exceptionFactory;
        this.propertyInfoFactory = propertyInfoFactory;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response getSearchDomains(@BeanParam JsonQueryParameters jsonQueryParameters, @Context UriInfo uriInfo) {
        List<SearchDomainInfo> list = searchService.getDomains().stream().map(sd -> new SearchDomainInfo(sd, uriInfo)).collect(toList());
        PagedInfoList pagedInfoList = PagedInfoList.fromCompleteList("domains", list, jsonQueryParameters);
        return Response.ok().entity(pagedInfoList).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{domain}")
    public Response getDomainProperties(@PathParam("domain") String domainId) {
        SearchDomain searchDomain = searchService.getDomains().stream().filter(domain -> domain.getId().equals(domainId)).findFirst().orElseThrow(()->exceptionFactory.newException(MessageSeeds.NO_SUCH_SEARCH_DOMAIN));
        PropertyList propertyList = new PropertyList();
        propertyList.properties = searchDomain.getProperties().stream().map(propertyInfoFactory::asInfoObject).collect(toList());
        return Response.ok().entity(propertyList).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{domain}/{property}")
    public Response getDomainPropertyValues(@PathParam("domain") String domainId, @PathParam("property") String property, @BeanParam JsonQueryParameters jsonQueryParameters) {
        SearchDomain searchDomain = searchService.getDomains().stream().filter(domain -> domain.getId().equals(domainId)).findFirst().orElseThrow(()->exceptionFactory.newException(MessageSeeds.NO_SUCH_SEARCH_DOMAIN));
        SearchableProperty searchableProperty = searchDomain.getProperties().stream().filter(p -> p.getName().equals(property)).findFirst().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_PROPERTY, property));
        PropertySpecPossibleValues possibleValues = searchableProperty.getSpecification().getPossibleValues();
        List<?> allValues = possibleValues!=null?possibleValues.getAllValues():Collections.emptyList();
        List allJsonValues = allValues.stream().map(this::asJsonValueObject).collect(toList());
        return Response.ok().entity(PagedInfoList.fromCompleteList("values", allJsonValues, jsonQueryParameters)).build();
    }

    private IdWithDisplayValueInfo asJsonValueObject(Object valueObject) {
        IdWithDisplayValueInfo info = new IdWithDisplayValueInfo();
        if (HasName.class.isAssignableFrom(valueObject.getClass())) {
            info.displayValue = ((HasName)valueObject).getName();
        }
        if (HasId.class.isAssignableFrom(valueObject.getClass())) {
            info.id = ((HasId)valueObject).getId();
        } else if (Enum.class.isAssignableFrom(valueObject.getClass())) {
            info.id = ((Enum)valueObject).name();
        }
        return info;
    }


    class SearchDomainInfo {
        public String name;
        @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
        public Link link;

        public SearchDomainInfo() {
        }

        public SearchDomainInfo(SearchDomain searchDomain, UriInfo uriInfo) {
            this.name = searchDomain.getId();
            this.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(SearchResource.class).path(SearchResource.class, "getDomainProperties")).build(searchDomain.getId());
        }
    }

    class IdWithDisplayValueInfo {
        public Object id;
        public String displayValue;
    }

    class PropertyList {
        public List<PropertyInfo> properties;
    }
}
