/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Yfn.privileges.DeviceLifeCycle
 *
 * Class that defines privileges for DeviceLifeCycle
 */
Ext.define('Dlc.privileges.DeviceLifeCycle', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    configure : ['privilege.configure.deviceLifeCycle'],
    view : ['privilege.view.deviceLifeCycle'],
    all: function() {
        return Ext.Array.merge(Dlc.privileges.DeviceLifeCycle.view, Dlc.privileges.DeviceLifeCycle.configure);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Dlc.privileges.DeviceLifeCycle.view);
    },
    canConfigure : function (){
        return Uni.Auth.checkPrivileges(Dlc.privileges.DeviceLifeCycle.configure);
    }
});