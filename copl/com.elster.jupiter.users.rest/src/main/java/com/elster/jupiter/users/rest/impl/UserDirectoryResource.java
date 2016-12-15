package com.elster.jupiter.users.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.AbstractLdapDirectoryImpl;
import com.elster.jupiter.users.rest.LdapUsersInfo;
import com.elster.jupiter.users.rest.LdapUsersInfos;
import com.elster.jupiter.users.rest.UserDirectoryInfo;
import com.elster.jupiter.users.rest.UserDirectoryInfos;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public UserDirectoryInfo getUserDirectory(@PathParam("id") long id,@Context SecurityContext securityContext) {
        LdapUserDirectory ldapUserDirectory = userService.getLdapUserDirectory(id);
        return new UserDirectoryInfo(ldapUserDirectory);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
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
            ldapUserDirectory.setManageGroupsInternal(true);
            ldapUserDirectory.update();
            context.commit();
            return info;
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public UserDirectoryInfo editUserDirectory(UserDirectoryInfo info, @PathParam("id") long id,@Context SecurityContext securityContext) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                if(info.isDefault){
                    UserDirectory fi = userService.findDefaultUserDirectory();
                    fi.setDefault(false);
                    fi.update();
                }
                if(info.name.equals("Local")){
                    Optional<UserDirectory> userDirectory = userService.findUserDirectory(info.name);
                    if(userDirectory.isPresent()){
                        userDirectory.get().setDefault(true);
                        userDirectory.get().update();
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
                    ldapUserDirectory.update();
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
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public PagedInfoList getExtUsers(@BeanParam JsonQueryParameters queryParameters,@PathParam("id") long id,@Context SecurityContext securityContext) {
        LdapUserDirectory ldapUserDirectory = userService.getLdapUserDirectory(id);
        List<LdapUser> ldapUsers = ldapUserDirectory.getLdapUsers();
        List<LdapUsersInfo> ldapUsersInfos = ListPager.of(ldapUsers)
                .paged(queryParameters.getStart().orElse(null), queryParameters.getLimit().orElse(null))
                .find()
                .stream()
                .sorted((s1,s2)-> s1.getUserName().toLowerCase().compareTo(s2.getUserName().toLowerCase()))
                .map(LdapUsersInfo::new)
                .collect(toList());
        return PagedInfoList.fromCompleteList("extusers",ldapUsersInfos,queryParameters);
    }

    @GET
    @Path("/{id}/users")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public PagedInfoList getUsers(@BeanParam JsonQueryParameters queryParameters,@PathParam("id") long id,@Context SecurityContext securityContext) {
        List<User> users = userService.getAllUsers(id);
        List<LdapUsersInfo> ldapUsersInfos = ListPager.of(users)
                .paged(queryParameters.getStart().orElse(null), queryParameters.getLimit().orElse(null))
                .find()
                .stream()
                .map(LdapUsersInfo::new)
                .collect(toList());
        return PagedInfoList.fromCompleteList("users",ldapUsersInfos,queryParameters);
    }

    @POST
    @Path("/{id}/users")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public Response saveUsers(LdapUsersInfos infos ,@PathParam("id") long id,@Context SecurityContext securityContext) {
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory ldapUserDirectory = userService.getLdapUserDirectory(id);
            infos.ldapUsers.stream().forEach(s -> userService.findOrCreateUser(s.name, ldapUserDirectory.getDomain(), ldapUserDirectory.getType(),s.status));
            context.commit();
        }
        return Response.status(Response.Status.OK).build();
    }
    private RestQuery<UserDirectory> getUserDirectoriesQuery() {
        Query<UserDirectory> query = userService.getLdapDirectories();
        return restQueryService.wrap(query);
    }
}