package com.elster.jupiter.users.security;

public interface Privileges {

    String CREATE_GROUP = "privilege.create.group";
    String UPDATE_GROUP = "privilege.update.group";
    String DELETE_GROUP = "privilege.delete.group";
    String VIEW_GROUP = "privilege.view.group";

    String CREATE_DOMAIN = "privilege.create.domain";
    String UPDATE_DOMAIN = "privilege.update.domain";
    String DELETE_DOMAIN = "privilege.delete.domain";
    String VIEW_DOMAIN = "privilege.view.domain";

    String UPDATE_USER = "privilege.update.user";
    String VIEW_USER = "privilege.view.user";

}
