/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Mdc.privileges.Communication
 *
 * Class that defines privileges for Communication
 */
Ext.define('Mdc.privileges.Communication', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.communicationAdministration', 'privilege.view.communicationAdministration'],
    admin: ['privilege.administrate.communicationAdministration'],
    all: function() {
        return Ext.Array.merge(Mdc.privileges.Communication.view,Mdc.privileges.Communication.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Communication.view );
    }
});
