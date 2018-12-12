/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.privileges.CommandLimitationRules', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.commandLimitationRule', 'privilege.view.commandLimitationRule', 'privilege.approve.commandLimitationRule'],
    admin: ['privilege.administrate.commandLimitationRule'],
    approval: ['privilege.approve.commandLimitationRule'],
    all: function() {
        return Ext.Array.merge(Mdc.privileges.CommandLimitationRules.view, Mdc.privileges.CommandLimitationRules.admin, Mdc.privileges.CommandLimitationRules.approval);
    },
    canView: function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.CommandLimitationRules.view );
    },
    canAdmin: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.CommandLimitationRules.admin);
    },
    canApproveReject: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.CommandLimitationRules.approval );
    }
});
