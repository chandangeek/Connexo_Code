/**
 * @class Mdc.privileges.Device
 *
 * Class that defines privileges for Device
 */
Ext.define('Mdc.privileges.Device', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,

    viewDeviceData:['privilege.administrate.deviceData','privilege.view.device'],
    viewDevice:['privilege.administrate.device','privilege.view.device'],
    deviceOperator: ['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    addDevice:['privilege.add.device'],
    flagDevice:['privilege.administrate.deviceData','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    administrateDeviceData:['privilege.administrate.deviceData'],
    viewDeviceCommunication:['privilege.administrate.deviceData','privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    administrateDeviceCommunication:['privilege.administrate.deviceCommunication'],
    operateDeviceCommunication:['privilege.operate.deviceCommunication'],
    administrateOrOperateDeviceCommunication:['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication'],
    viewOrAdministrateDeviceData : ['privilege.view.device','privilege.administrate.deviceData'],
    viewOrAdministrateOrOperateDeviceCommunication: ['privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication','privilege.view.device'],
    editDeviceAttributes: ['privilege.administrate.attribute.device'],
    all: function() {
        return Ext.Array.merge(Mdc.privileges.Device.viewDevice,
            Mdc.privileges.Device.viewDeviceData,
            Mdc.privileges.Device.deviceOperator,
            Mdc.privileges.Device.addDevice,
            Mdc.privileges.Device.administrateDeviceData,
            Mdc.privileges.Device.viewDeviceCommunication,
            Mdc.privileges.Device.administrateDeviceCommunication,
            Mdc.privileges.Device.administrateOrOperateDeviceCommunication,
            Mdc.privileges.Device.editDeviceAttributes
        );
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.view );
    },
    canAdministrateDeviceData : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.administrateDeviceData );
    },
    canViewOrAdministrateDeviceData:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewOrAdministrateDeviceData );
    },
    canAddDevice: function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.addDevice );
    },
    canSearchDevices : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewDeviceCommunication );
    },
    canOperateDevice : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.deviceOperator);
    },
    canOperateDeviceCommunication : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.operateDeviceCommunication );
    },
    canAdministrateOrOperateDeviceCommunication : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.administrateOrOperateDeviceCommunication );
    },
    canViewDeviceCommunication: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewDeviceCommunication);
    },
    canEditDeviceAttributes: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.editDeviceAttributes);
    }
});
