/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Isu.privileges.Device
 *
 * Class that defines privileges for Device
 */
Ext.define('Isu.privileges.Device', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,

    viewDeviceCommunication:['privilege.administrate.deviceData','privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    operateDeviceCommunication:['privilege.operate.deviceCommunication'],
    all: function() {
        return Ext.Array.merge(Isu.privileges.Device.viewDeviceCommunication,
            Isu.privileges.Device.operateDeviceCommunication
        );
    },
    canOperateDeviceCommunication : function(){
        return Uni.Auth.checkPrivileges(Isu.privileges.Device.operateDeviceCommunication );
    },
    canViewDeviceCommunication: function() {
        return Uni.Auth.checkPrivileges(Isu.privileges.Device.viewDeviceCommunication);
    }
});
