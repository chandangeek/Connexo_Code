/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Mdc.privileges.DeviceType
 *
 * Class that defines privileges for DeviceType
 */
Ext.define('Mdc.privileges.DeviceType', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.deviceType', 'privilege.view.deviceType'],
    admin: ['privilege.administrate.deviceType'],
    all: function() {
        return Ext.Array.merge(Mdc.privileges.DeviceType.view,Mdc.privileges.DeviceType.admin);
    },
    canAdministrate:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.DeviceType.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.DeviceType.view );
    }
});
