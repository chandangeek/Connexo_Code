package com.elster.jupiter.users.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.AbstractLdapDirectoryImpl;
import com.elster.jupiter.users.rest.UserDirectoryInfo;
import com.elster.jupiter.users.rest.UserDirectoryInfos;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Path("/userdirectories")
public class UserDirectoryResource {

    private final UserService userService;
    private final TransactionService transactionService;
    private final RestQueryService restQueryService;

    @Inject
    public UserDirectoryResource(UserService userService, TransactionService transactionService,RestQueryService restQueryService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.restQueryService = restQueryService;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_USER_ROLE, Privileges.VIEW_USER_ROLE})
    public UserDirectoryInfos getUserDirectory(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<AbstractLdapDirectoryImpl> userDirectory = (List<AbstractLdapDirectoryImpl>)(List<?>)getUserDirectoriesQuery().select(queryParameters, Order.ascending("domain").toLowerCase());
        List<LdapUserDirectory> ldapUserDirectories = new ArrayList<>();
        for(int i=0; i<userDirectory.size();i++){
            try {
                ldapUserDirectories.add((LdapUserDirectory) userDirectory.get(i));
            }catch (ClassCastException e ){
                Optional<UserDirectory> usr = userService.findUserDirectory("Local");
                if(usr.isPresent()){
                    LdapUserDirectory ldapUserDirectory = userService.createApacheDirectory(usr.get().getDomain());
                    ldapUserDirectory.setDefault(usr.get().isDefault());
                    ldapUserDirectories.add(ldapUserDirectory);
                }
            }
        }
        UserDirectoryInfos infos = new UserDirectoryInfos(queryParameters.clipToLimit(ldapUserDirectories));
        infos.total = queryParameters.determineTotal(ldapUserDirectories.size());
        return infos;

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
            ldapUserDirectory.setDirectoryUser(info.directoryUser);
            ldapUserDirectory.setSecurity(info.securityProtocol);
            ldapUserDirectory.setPassword(info.password);
            ldapUserDirectory.setBaseGroup(info.baseGroup);
            ldapUserDirectory.setBaseUser(info.baseUser);
            ldapUserDirectory.setUrl(info.url);
            ldapUserDirectory.setBackupUrl(info.backupUrl);
            ldapUserDirectory.setDefault(info.isDefault);
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
                if(info.name.equals("Local")){
                    Optional<UserDirectory> userDirectory = userService.findUserDirectory(info.name);
                    if(userDirectory.isPresent()){
                        userDirectory.get().setDefault(true);
                        userDirectory.get().save();
                    }
                }else {
                    LdapUserDirectory ldapUserDirectory = userService.getLdapUserDirectory(id);
                    ldapUserDirectory.setBackupUrl(info.backupUrl);
                    ldapUserDirectory.setDomain(info.name);
                    ldapUserDirectory.setPassword(info.password);
                    ldapUserDirectory.setUrl(info.url);
                    ldapUserDirectory.setDirectoryUser(info.directoryUser);
                    ldapUserDirectory.setSecurity(info.securityProtocol);
                    ldapUserDirectory.setBaseGroup(info.baseGroup);
                    ldapUserDirectory.setBaseUser(info.baseUser);
                    ldapUserDirectory.setDefault(info.isDefault);
                    ldapUserDirectory.setType(info.type);
                    ldapUserDirectory.save();
                }
            }
        });
        if(id == 0 ){
            UserDirectory userDirectory = userService.findUserDirectory(info.name).get();
            LdapUserDirectory ldapUserDirectory = userService.createApacheDirectory(userDirectory.getDomain());
            ldapUserDirectory.setDefault(userDirectory.isDefault());
            return new UserDirectoryInfo(ldapUserDirectory);
        }else {
            return getUserDirectory(id, securityContext);
        }
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

    @GET
    @Path("/{id}/extusers")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_USER_ROLE, Privileges.VIEW_USER_ROLE})
    public PagedInfoList getExtUsers(@BeanParam JsonQueryParameters queryParameters,@PathParam("id") long id,@Context SecurityContext securityContext) {
        LdapUserDirectory ldapUserDirectory = userService.getLdapUserDirectory(id);
        List<String> ldapUsers = ldapUserDirectory.getLdapUsers();
        if(!ldapUsers.isEmpty()) {
            Collections.sort(ldapUsers);
        }
        return PagedInfoList.fromCompleteList("extusers",ldapUsers,queryParameters);
    }

    @GET
    @Path("/{id}/allusers")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_USER_ROLE, Privileges.VIEW_USER_ROLE})
    public PagedInfoList getAllUsers(@BeanParam JsonQueryParameters queryParameters,@PathParam("id") long id,@Context SecurityContext securityContext) {
        List<User> users = userService.getAllUsers(id);
        return null;
    }

    private RestQuery<UserDirectory> getUserDirectoriesQuery() {
        Query<UserDirectory> query = userService.getLdapDirectories();
        return restQueryService.wrap(query);
    }
}