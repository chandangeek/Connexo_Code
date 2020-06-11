/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.search.*;
import com.elster.jupiter.search.SearchCriteriaService.SearchCriteriaBuilder;
import com.elster.jupiter.search.location.SearchLocationService;
import com.elster.jupiter.search.rest.InfoFactoryService;
import com.elster.jupiter.search.rest.MessageSeeds;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 6/1/15.
 */
@Path("/search")
public class DynamicSearchResource {

    private final SearchService searchService;
    private final SearchLocationService searchLocationService;
    private final ExceptionFactory exceptionFactory;
    private final SearchCriterionInfoFactory searchCriterionInfoFactory;
    private final InfoFactoryService infoFactoryService;
    private final ThreadPrincipalService threadPrincipalService;
    private final SearchCriteriaService searchCriteriaService;
    private final TransactionService transactionService;


    @Inject
    public DynamicSearchResource(SearchService searchService, SearchLocationService searchLocationService, ExceptionFactory exceptionFactory, SearchCriterionInfoFactory searchCriterionInfoFactory, InfoFactoryService infoFactoryService,
                                 ThreadPrincipalService threadPrincipalService,
                                 SearchCriteriaService searchCriteriaService,
                                 TransactionService transactionService) {
        this.searchCriteriaService = searchCriteriaService;
        this.searchService = searchService;
        this.searchLocationService = searchLocationService;
        this.exceptionFactory = exceptionFactory;
        this.threadPrincipalService = threadPrincipalService;
        this.searchCriterionInfoFactory = searchCriterionInfoFactory;
        this.transactionService = transactionService;

        this.infoFactoryService = infoFactoryService;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getSearchDomains(@BeanParam JsonQueryParameters jsonQueryParameters,
                                     @BeanParam JsonQueryFilter filter,
                                     @BeanParam UriInfo uriInfo) {
        List<SearchDomain> domains;
        if (filter.hasProperty("application")) {
            domains = searchService.getDomains(filter.getString("application"));
        } else {
            domains = searchService.getDomains();
        }
        List<SearchDomainInfo> list = domains.stream()
                .map(sd -> new SearchDomainInfo(sd, uriInfo))
                .sorted((a, b) -> a.displayValue.compareTo(b.displayValue)).collect(toList());
        PagedInfoList pagedInfoList = PagedInfoList.fromCompleteList("domains", list, jsonQueryParameters);
        return Response.ok().entity(pagedInfoList).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{domain}/searchcriteria")
    public Response getSearchablePropertiesForDomain(@PathParam("domain") String domainId,
                                                     @FormParam("filter") String filter,
                                                     @FormParam("start") Integer start,
                                                     @FormParam("limit") Integer limit,
                                                     @Context UriInfo uriInfo) throws InvalidValueException {

        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(filter);
        JsonQueryParameters jsonQueryParameters = new JsonQueryParameters(start, limit);

        Stream<SearchableProperty> propertyStream = getSearchableProperties(searchDomain, jsonQueryFilter);
        List<PropertyInfo> propertyList = propertyStream
                .map(p -> searchCriterionInfoFactory.asListObject(p, uriInfo))
                .collect(toList());
        PagedInfoList pagedProperties = PagedInfoList.fromCompleteList("properties", propertyList, jsonQueryParameters);
        return Response.ok().entity(pagedProperties).build();
    }

    private Stream<SearchableProperty> getSearchableProperties(SearchDomain searchDomain, JsonQueryFilter jsonQueryFilter) {
        Stream<SearchableProperty> propertyStream;
        if (jsonQueryFilter.hasFilters()) {
            List<SearchablePropertyConstriction> searchablePropertyConstrictions = searchDomain
                    .getProperties()
                    .stream()
                    .filter(SearchableProperty::affectsAvailableDomainProperties)
                    .map(constrainingProperty -> SearchablePropertyValueConverter.convert(constrainingProperty, jsonQueryFilter).asConstriction())
                    .collect(toList());
            propertyStream = searchDomain.getPropertiesWithConstrictions(searchablePropertyConstrictions).stream();
        } else {
            propertyStream = searchDomain.getProperties().stream();
        }
        return propertyStream;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{domain}/model")
    public Response getDomainDescription(@PathParam("domain") String domainId) {
        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.model = infoFactoryService.getInfoFactoryFor(searchDomain).modelStructure();

        return Response.ok(modelInfo).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{domain}")
    public Response doSearch(@PathParam("domain") String domainId,
                             @BeanParam JsonQueryFilter jsonQueryFilter,
                             @BeanParam JsonQueryParameters jsonQueryParameters,
                             @Context UriInfo uriInfo) throws InvalidValueException {
        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        InfoFactory infoFactory = infoFactoryService.getInfoFactoryFor(searchDomain);
        List<?> domainObjects = initSearchBuilder(searchDomain, jsonQueryFilter).toFinder().from(jsonQueryParameters).find();

        List searchResults = infoFactory.from(domainObjects);
        return Response.ok().entity(PagedInfoList.fromPagedList("searchResults", searchResults, jsonQueryParameters)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{domain}")
    public Response doSearchPost(@PathParam("domain") String domainId,
                                 @FormParam("page") Integer page,
                                 @FormParam("start") Integer start,
                                 @FormParam("limit") Integer limit,
                                 @FormParam("filter") String filter) throws InvalidValueException {
        Principal principal = threadPrincipalService.getPrincipal();
        User user = (User)principal;
        List searchResults;
        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        InfoFactory infoFactory = infoFactoryService.getInfoFactoryFor(searchDomain);
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(filter);
        JsonQueryParameters jsonQueryParameters = new JsonQueryParameters(start, limit);

        if(checkPrivileges(user, domainId)) {
            List<?> domainObjects = initSearchBuilder(searchDomain, jsonQueryFilter).toFinder().from(jsonQueryParameters).find();
            searchResults = infoFactory.from(domainObjects);
        }else {
            searchResults = new ArrayList();
        }
        return Response.ok().entity(PagedInfoList.fromPagedList("searchResults", searchResults, jsonQueryParameters)).build();
    }

    public boolean checkPrivileges(User user, String domainId){
        boolean adminPrivilege =  user.getPrivileges()
                .stream()
                .filter(privilege -> privilege.getName().equals("privilege.administrate.deviceData"))
                .findAny()
                .isPresent();
        boolean viewPrivilege = user.getPrivileges()
                .stream()
                .filter(privilege -> privilege.getName().equals("privilege.view.device"))
                .findAny()
                .isPresent();
        if (domainId.equals("com.elster.jupiter.users.User") || adminPrivilege && viewPrivilege){
            return true;
        }

        return false;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{domain}/count")
    public Response doCountSearchResults(@PathParam("domain") String domainId,
                                         @FormParam("filter") String filter,
                                         @Context UriInfo uriInfo) {
        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(filter);
        int numberOfSearchResults = initSearchBuilder(searchDomain, jsonQueryFilter).toFinder().count();
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("numberOfSearchResults", numberOfSearchResults);
        return Response.ok().entity(jsonResponse).build();
    }

    private SearchBuilder<?> initSearchBuilder(SearchDomain searchDomain, JsonQueryFilter jsonQueryFilter) {
        final SearchBuilder<?> searchBuilder = searchService.search(searchDomain);
        if (jsonQueryFilter.hasFilters()) {
            searchDomain.getPropertiesValues(searchableProperty -> SearchablePropertyValueConverter.convert(searchableProperty, jsonQueryFilter))
                    .stream()
                    .forEach(propertyValue -> {
                        try {
                            propertyValue.addAsCondition(searchBuilder);
                        } catch (InvalidValueException e) {
                            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "filter." + propertyValue.getProperty().getName());
                        }
                    });
        } else {
            throw new LocalizedFieldValidationException(MessageSeeds.AT_LEAST_ONE_CRITERIA, "filter");
        }
        return searchBuilder;
    }

    private SearchDomain findSearchDomainOrThrowException(@PathParam("domain") String domainId) {
        return searchService.findDomain(domainId).
                orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_SEARCH_DOMAIN, org.apache.commons.text.StringEscapeUtils.escapeHtml4(domainId)));
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{domain}/searchcriteria/{property}")
    public Response getFullCriteriaInfo(@PathParam("domain") String domainId,
                                        @PathParam("property") String property,
                                        @FormParam("filter") String filter,
                                        @Context UriInfo uriInfo){
        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(filter);
        SearchableProperty searchableProperty = getSearchableProperties(searchDomain, jsonQueryFilter)
                .filter(prop -> property.equals(prop.getName()))
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_PROPERTY, org.apache.commons.text.StringEscapeUtils.escapeHtml4(property)));
        List<SearchablePropertyConstriction> searchablePropertyConstrictions =
                searchableProperty.getConstraints().stream().
                        map(constrainingProperty -> SearchablePropertyValueConverter.convert(constrainingProperty, jsonQueryFilter).asConstriction()).
                        collect(toList());
        searchableProperty.refreshWithConstrictions(searchablePropertyConstrictions);
        PropertyInfo propertyInfo = searchCriterionInfoFactory.asSingleObject(searchableProperty, uriInfo, jsonQueryFilter.getString("displayValue"));
        return Response.ok().entity(propertyInfo).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{domain}/locationsearchcriteria/{property}")
    public Response getLocationFullCriteriaInfo(@PathParam("domain") String domainId,
                                                @PathParam("property") String property,
                                                @BeanParam JsonQueryParameters jsonQueryParameters,
                                                @BeanParam JsonQueryFilter jsonQueryFilter,
                                                @BeanParam UriInfo uriInfo) {
        SearchDomain searchDomain = findSearchDomainOrThrowException(domainId);
        SearchableProperty searchableProperty = getSearchableProperties(searchDomain, jsonQueryFilter)
                .filter(prop -> property.equals(prop.getName()))
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_PROPERTY, org.apache.commons.text.StringEscapeUtils.escapeHtml4(property)));

        PropertyInfo propertyInfo = searchCriterionInfoFactory.asSingleObjectWithValues(searchableProperty, uriInfo, searchLocationService.findLocations(jsonQueryFilter.getString("displayValue")));
        return Response.ok().entity(propertyInfo).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/saveCriteria/{name}")
    public Response saveSearchCriteria(@NotNull @PathParam("name") String name,
                                              @NotNull @FormParam("filter") String filter,
                                              @NotNull @FormParam("domain") String domainId) {
        Map<Object, Object> jsonResponse = new HashMap<>();
        String status = "";
        try (TransactionContext transactionContext = transactionService.getContext()) {
            SearchCriteriaBuilder searchCriteriaBuilder = searchCriteriaService.newSearchCriteria();
            searchCriteriaBuilder.setUserName(threadPrincipalService.getPrincipal().getName());
            searchCriteriaBuilder.setCriteria(filter);
            searchCriteriaBuilder.setName(name);
            searchCriteriaBuilder.setDomain(domainId);
            if (checkForSearchUpdate(name)) {
                searchCriteriaBuilder.update();
                status = "Update";
            }
            else {
                searchCriteriaBuilder.complete();
                status = "Save";
            }
            transactionContext.commit();
        }
        jsonResponse.put("status", status);
        return Response.ok().entity(jsonResponse).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/saveSearchCriteria")
    public Response getSearchCriteria() {
        Query<SearchCriteria> searchCriteriaQuery = searchCriteriaService.getCreationRuleQuery();
        List<SearchCriteria> searchCriteriaList = searchCriteriaQuery.select(Where.where("userName").isEqualTo(threadPrincipalService.getPrincipal().getName()));
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("numberOfSearchResults", searchCriteriaList);
        return Response.ok().entity(jsonResponse).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/searchCriteria/{name}")
    public Response deleteSearchCriteria(@PathParam("name") String name) {
        try (TransactionContext transactionContext = transactionService.getContext()) {
            SearchCriteriaBuilder searchCriteriaBuilder = searchCriteriaService.newSearchCriteria();
            searchCriteriaBuilder.setUserName(threadPrincipalService.getPrincipal().getName());
            searchCriteriaBuilder.setName(name);
            searchCriteriaBuilder.delete();
            transactionContext.commit();
        }
        return Response.ok(Response.Status.OK).build();
    }

    private boolean checkForSearchUpdate(String name) {
        Query<SearchCriteria> searchCriteriaQuery = searchCriteriaService.getCreationRuleQuery();
        long size = searchCriteriaQuery.select(Where.where("userName").isEqualTo(threadPrincipalService.getPrincipal().getName())
                .and(Where.where("name").isEqualTo(name))).size();
        return size > 0;
    }


    class SearchDomainInfo {
        public String id;
        public String displayValue;
        @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
        public List<Link> link;

        public SearchDomainInfo() {
        }

        public SearchDomainInfo(SearchDomain searchDomain, UriInfo uriInfo) {
            this.id = searchDomain.getId();
            this.displayValue = searchDomain.displayName();
            this.link = new ArrayList<>();
            link.add(Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DynamicSearchResource.class).path(DynamicSearchResource.class, "doSearch")).rel("self").build(searchDomain.getId()));
            link.add(Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DynamicSearchResource.class).path(DynamicSearchResource.class, "getSearchablePropertiesForDomain")).rel("glossary").build(searchDomain.getId()));
            link.add(Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DynamicSearchResource.class).path(DynamicSearchResource.class, "getDomainDescription")).rel("describedby").build(searchDomain.getId()));
        }
    }

    class ModelInfo {
        public List model;
    }
}
