package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.rest.MessageSeeds;
import com.elster.jupiter.util.HasId;
import java.util.ArrayList;
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
    @Path("/{domain}/properties")
    public Response getDomainProperties(@PathParam("domain") String domainId,
                                        @BeanParam JsonQueryFilter jsonQueryFilter,
                                        @BeanParam JsonQueryParameters jsonQueryParameters,
                                        @Context UriInfo uriInfo) throws InvalidValueException {
        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        PropertyList propertyList = new PropertyList();
        propertyList.properties = searchDomain.getProperties().stream().map(p -> propertyInfoFactory.asInfoObject(p, uriInfo)).collect(toList());
        return Response.ok().entity(propertyList).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{domain}")
    public Response doSearch(@PathParam("domain") String domainId,
                                        @BeanParam JsonQueryFilter jsonQueryFilter,
                                        @BeanParam JsonQueryParameters jsonQueryParameters,
                                        @Context UriInfo uriInfo) throws InvalidValueException {
        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        final SearchBuilder<Object> searchBuilder = searchService.search(searchDomain);
        if (jsonQueryFilter.hasFilters()) {
            searchDomain.getProperties().stream().
                    filter(p -> jsonQueryFilter.hasProperty(p.getName())).
                    forEach(searchableProperty -> {
                        try {
                            if (searchableProperty.getSelectionMode().equals(SearchableProperty.SelectionMode.MULTI)) {
                                searchBuilder.where(searchableProperty).in(getQueryParameterAsObjectList(jsonQueryFilter, searchableProperty));
                            } else {
                                searchBuilder.where(searchableProperty).isEqualTo(getQueryParameterAsObject(jsonQueryFilter, searchableProperty));
                            }
                        } catch (InvalidValueException e) {
                            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "filter." + searchableProperty.getName());
                        }

                    });
        } else {
            throw new LocalizedFieldValidationException(MessageSeeds.AT_LEAST_ONE_CRITERIA, "filter");
        }
        List<Object> searchResults = searchBuilder.toFinder().from(jsonQueryParameters).stream().map(Object::toString).collect(toList());
        return Response.ok().entity(PagedInfoList.fromPagedList("searchResults", searchResults, jsonQueryParameters)).build();
    }

    private SearchDomain findSearchDomainOrThrowException(@PathParam("domain") String domainId) {
        return searchService.findDomain(domainId).
                    orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_SEARCH_DOMAIN));
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{domain}/properties/{property}")
    public Response getDomainPropertyValues(@PathParam("domain") String domainId,
                                            @PathParam("property") String property,
                                            @BeanParam JsonQueryParameters jsonQueryParameters,
                                            @BeanParam JsonQueryFilter jsonQueryFilter) {
        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        SearchableProperty searchableProperty = findProperty(property, searchDomain);
        ArrayList<SearchablePropertyConstriction> searchablePropertyConstrictions = new ArrayList<>();
        for (SearchableProperty constrainingProperty : searchableProperty.getConstraints()) {
            if (jsonQueryFilter.hasProperty(constrainingProperty.getName())) {
                List<Object> constrainingValues;
                if (constrainingProperty.getSelectionMode().equals(SearchableProperty.SelectionMode.MULTI)) {
                    constrainingValues= getQueryParameterAsObjectList(jsonQueryFilter, constrainingProperty);
                } else {
                    constrainingValues=Collections.singletonList(getQueryParameterAsObject(jsonQueryFilter, constrainingProperty));
                }
                searchablePropertyConstrictions.add(SearchablePropertyConstriction.withValues(constrainingProperty, constrainingValues));
            } else {
                searchablePropertyConstrictions.add(SearchablePropertyConstriction.noValues(constrainingProperty));
            }
        }
        searchableProperty.refreshWithConstrictions(searchablePropertyConstrictions);
        PropertySpecPossibleValues possibleValues = searchableProperty.getSpecification().getPossibleValues();
        List<?> allValues = possibleValues!=null?possibleValues.getAllValues():Collections.emptyList();
        List allJsonValues = allValues.stream().map(v->asJsonValueObject(searchableProperty.toDisplay(v),v)).collect(toList());
        return Response.ok().entity(PagedInfoList.fromCompleteList("values", allJsonValues, jsonQueryParameters)).build();
    }

    private Object getQueryParameterAsObject(@BeanParam JsonQueryFilter jsonQueryFilter, SearchableProperty constrainingProperty) {
        return constrainingProperty.getSpecification().getValueFactory().fromStringValue(jsonQueryFilter.getString(constrainingProperty.getName()));
    }

    private List<Object> getQueryParameterAsObjectList(@BeanParam JsonQueryFilter jsonQueryFilter, SearchableProperty constrainingProperty) {
        return jsonQueryFilter.getPropertyList(constrainingProperty.getName()).stream().
                map(p -> constrainingProperty.getSpecification().getValueFactory().fromStringValue(p)).
                collect(toList());
    }

    private SearchableProperty findProperty(@PathParam("property") String property, SearchDomain searchDomain) {
        return searchDomain.getProperties().stream().filter(p -> p.getName().equals(property)).findFirst().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_PROPERTY, property));
    }

    private IdWithDisplayValueInfo asJsonValueObject(String name, Object valueObject) {
        IdWithDisplayValueInfo info = new IdWithDisplayValueInfo();
        info.displayValue = name;
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
        public List<Link> link;

        public SearchDomainInfo() {
        }

        public SearchDomainInfo(SearchDomain searchDomain, UriInfo uriInfo) {
            this.name = searchDomain.getId();
            this.link = new ArrayList<>();
            link.add(Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(SearchResource.class).path(SearchResource.class, "doSearch")).rel("self").build(searchDomain.getId()));
            link.add(Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(SearchResource.class).path(SearchResource.class, "getDomainProperties")).rel("describedby").build(searchDomain.getId()));
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
