/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Sam.privileges.SystemProperties
 *
 * Class that defines privileges for SystemProperties
 */
Ext.define('Sam.privileges.SystemProperties', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.sysProps','privilege.view.sysProps'],
    admin: ['privilege.administrate.sysProps'],
    all: function() {
        return Ext.Array.merge(Sam.privileges.DataPurge.view, Sam.privileges.DataPurge.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Sam.privileges.DataPurge.view);
    },
    canAdmin:function(){
        return Uni.Auth.checkPrivileges(Sam.privileges.DataPurge.admin);
    }

});
