/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Usr.privileges.Users
 *
 * Class that defines privileges for Users
 */
Ext.define('Usr.privileges.Users', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.userAndRole', 'privilege.view.userAndRole'],
    viewUsers: ['privilege.administrate.userAndRole', 'privilege.view.userAndRole', 'dualcontrol.grant.approval'],
    admin : ['privilege.administrate.userAndRole'],
    adminUsers : ['privilege.administrate.userAndRole', 'dualcontrol.grant.approval'],
    all: function() {
        return Ext.Array.merge(Usr.privileges.Users.view,
            Usr.privileges.Users.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Usr.privileges.Users.view);
    },

    canViewUsers: function() {
        return Uni.Auth.checkPrivileges(Usr.privileges.Users.viewUsers);
    },
    canAdministrate:function(){
        return Uni.Auth.checkPrivileges(Usr.privileges.Users.admin);
    }
});
