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
    view: ['privilege.administer.sysProps','privilege.view.sysProps'],
    admin: ['privilege.administer.sysProps'],
    all: function() {
        return Ext.Array.merge(Sam.privileges.SystemProperties.view, Sam.privileges.SystemProperties.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Sam.privileges.SystemProperties.view);
    },
    canAdmin:function(){
        return Uni.Auth.checkPrivileges(Sam.privileges.SystemProperties.admin);
    }

});
