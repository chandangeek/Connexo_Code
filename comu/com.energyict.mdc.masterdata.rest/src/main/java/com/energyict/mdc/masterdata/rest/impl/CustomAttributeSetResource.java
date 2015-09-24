package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.rest.CustomAttributeSetDomainExtensionInfo;
import com.energyict.mdc.masterdata.rest.CustomAttributeSetInfo;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/customattributesets")
public class CustomAttributeSetResource {

    private final CustomPropertySetService customPropertySetService;
    private final Thesaurus thesaurus;

    @Inject
    public CustomAttributeSetResource(CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        super();
        this.customPropertySetService = customPropertySetService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_MASTER_DATA, Privileges.VIEW_MASTER_DATA})
    public PagedInfoList getCustomAttributeSets(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        String domainExtension = filter.getString("domainExtension");
        List<RegisteredCustomPropertySet> customPropertySets = new ArrayList<>();
        if (domainExtension == null) {
            customPropertySets = customPropertySetService.findActiveCustomPropertySets();
        } else {
            try {
                customPropertySets = customPropertySetService.findActiveCustomPropertySets(Class.forName(domainExtension));
            } catch (ClassNotFoundException e) {
                throw new WebApplicationException("No custom attribute domain extension - " + domainExtension, e, Response.Status.NOT_FOUND);
            }
        }
        return PagedInfoList.fromPagedList("customAttributeSets", CustomAttributeSetInfo.from(customPropertySets, thesaurus), queryParameters);
    }

    @GET
    @Path("/domains")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_MASTER_DATA, Privileges.VIEW_MASTER_DATA})
    public PagedInfoList getDomains(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        Set<String> domainExtensions = new HashSet<>(customPropertySetService.findActiveCustomPropertySets()
                .stream()
                .map(sc -> sc.getCustomPropertySet().getDomainClass().getName())
                .collect(Collectors.toList()));
        return PagedInfoList.fromPagedList("domainExtensions",
                CustomAttributeSetDomainExtensionInfo.from(domainExtensions, thesaurus),
                queryParameters);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_MASTER_DATA)
    public CustomAttributeSetInfo updatePrivileges(@PathParam("id") long id, CustomAttributeSetInfo customAttributeSetInfo) {
        RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySets()
                .stream()
                .filter(f -> f.getId() == id)
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("No custom attribute set with id " + id, Response.Status.NOT_FOUND));
        registeredCustomPropertySet.updatePrivileges(customAttributeSetInfo.viewPrivileges, customAttributeSetInfo.editPrivileges);
        return new CustomAttributeSetInfo (registeredCustomPropertySet, thesaurus);
    }
}