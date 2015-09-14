package com.elster.jupiter.users.rest.impl;


import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserDirectoryInfo;
import com.elster.jupiter.users.rest.UserDirectoryInfos;
import com.elster.jupiter.users.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Optional;

@Path("/userdirectories")
public class UserDirectoryResource {

    private final UserService userService;
    private final TransactionService transactionService;

    @Inject
    public UserDirectoryResource(UserService userService, TransactionService transactionService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_USER_ROLE, Privileges.VIEW_USER_ROLE})
    public UserDirectoryInfos getUserDirectory(@Context UriInfo uriInfo) {
        List<UserDirectory> userDirectory = userService.getUserDirectories();
        List<LdapUserDirectory> ldapDirectories = userService.getLdapDirectories();
        Optional<UserDirectory> usr = userDirectory.stream()
                .filter(s -> s.getDomain().contains("Local"))
                .findFirst();
        if (usr.isPresent()) {
            LdapUserDirectory ldapUserDirectory = userService.createApacheDirectory(usr.get().getDomain());
            ldapUserDirectory.setDefault(usr.get().isDefault());
            ldapDirectories.add(ldapUserDirectory);
        }
        return new UserDirectoryInfos(ldapDirectories);

    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_USER_ROLE, Privileges.VIEW_USER_ROLE})
    public UserDirectoryInfo getUserDirectory(@PathParam("id") long id,@Context SecurityContext securityContext) {
        LdapUserDirectory ldapUserDirectory = userService.getLdapUserDirectory(id);
        return new UserDirectoryInfo(ldapUserDirectory);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_USER_ROLE)
    public UserDirectoryInfo createUserDirectory(UserDirectoryInfo info,@PathParam("id") long id ){
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory ldapUserDirectory;
            if (info.type.equals("APD")) {
                ldapUserDirectory = userService.createApacheDirectory(info.name);
            } else {
                ldapUserDirectory = userService.createActiveDirectory(info.name);
            }
            ldapUserDirectory.setDirectoryUser("admin");
            ldapUserDirectory.setSecurity(info.securityProtocol);
            ldapUserDirectory.setPassword("admin");
            ldapUserDirectory.setBaseGroup(info.baseGroup);
            ldapUserDirectory.setBaseUser(info.baseUser);
            ldapUserDirectory.setUrl(info.url);
            ldapUserDirectory.setBackupUrl(info.backupUrl);
            ldapUserDirectory.setDefault(info.isDefault);
            ldapUserDirectory.setPrefix(info.prefix);
            ldapUserDirectory.save();
            context.commit();
            return info;
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_USER_ROLE)
    public UserDirectoryInfo editUserDirectory(UserDirectoryInfo info, @PathParam("id") long id,@Context SecurityContext securityContext) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                if(info.isDefault){
                    UserDirectory fi = userService.findDefaultUserDirectory();
                    fi.setDefault(false);
                    fi.save();
                }
                LdapUserDirectory ldapUserDirectory = userService.getLdapUserDirectory(id);
                ldapUserDirectory.setBackupUrl(info.backupUrl);
                ldapUserDirectory.setDomain(info.name);
                ldapUserDirectory.setUrl(info.url);
                ldapUserDirectory.setSecurity(info.securityProtocol);
                ldapUserDirectory.setBaseGroup(info.baseGroup);
                ldapUserDirectory.setBaseUser(info.baseUser);
                ldapUserDirectory.setDefault(info.isDefault);
                ldapUserDirectory.setPrefix(info.prefix);
                ldapUserDirectory.setType(info.type);
                ldapUserDirectory.save();
            }
        });
        return getUserDirectory(id,securityContext);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_USER_ROLE)
    public Response deleteUserDirectory(UserDirectoryInfo info, @PathParam("id") long id) {
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory ldapUserDirectory = userService.getLdapUserDirectory(id);
            ldapUserDirectory.delete();
            context.commit();
            return Response.status(Response.Status.OK).build();
        }
    }
}