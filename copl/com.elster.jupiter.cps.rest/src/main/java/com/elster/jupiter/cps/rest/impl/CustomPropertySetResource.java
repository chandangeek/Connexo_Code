package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.Privileges;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.MessageSeeds;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/custompropertysets")
public class CustomPropertySetResource {

    private final TransactionService transactionService;
    private final CustomPropertySetService customPropertySetService;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;

    @Inject
    public CustomPropertySetResource(TransactionService transactionService,
                                     CustomPropertySetService customPropertySetService,
                                     CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        super();
        this.transactionService = transactionService;
        this.customPropertySetService = customPropertySetService;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTER_PRIVILEGES, Privileges.VIEW_PRIVILEGES})
    public PagedInfoList getCustomAttributeSets(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        String domainExtension = filter.getString("domainExtension");
        List<RegisteredCustomPropertySet> customPropertySets;
        if (domainExtension != null) {
            customPropertySets = customPropertySetService.findActiveCustomPropertySets()
                    .stream()
                    .filter(f -> (f.getCustomPropertySet().getDomainClass().getName().equals(domainExtension)))
                    .collect(Collectors.toList());
        } else {
            customPropertySets = customPropertySetService.findActiveCustomPropertySets();
        }
        return PagedInfoList.fromCompleteList("customAttributeSets",
                customPropertySetInfoFactory.from(customPropertySets), queryParameters);
    }

    @GET
    @Path("/domains")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTER_PRIVILEGES, Privileges.VIEW_PRIVILEGES})
    public PagedInfoList getDomains(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        Set<String> domainExtensions = new HashSet<>(customPropertySetService.findActiveCustomPropertySets()
                .stream()
                .map(m -> m.getCustomPropertySet().getDomainClass().getName())
                .collect(Collectors.toList()));
        return PagedInfoList.fromPagedList("domainExtensions",
                customPropertySetInfoFactory.from(domainExtensions), queryParameters);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTER_PRIVILEGES)
    public Response updatePrivileges(@PathParam("id") long id, CustomPropertySetInfo customPropertySetInfo) {
        RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySets()
                .stream()
                .filter(f -> f.getId() == id)
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("No custom attribute set with id " + id, Response.Status.NOT_FOUND));
        try (TransactionContext context = transactionService.getContext()) {
            registeredCustomPropertySet.updatePrivileges(customPropertySetInfo.viewPrivileges, customPropertySetInfo.editPrivileges);
            context.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.UPDATE_CUSTOM_PROPERTY_SET, "privileges",
                    registeredCustomPropertySet.getCustomPropertySet().getName());
        }
        return Response.ok().build();
    }
}