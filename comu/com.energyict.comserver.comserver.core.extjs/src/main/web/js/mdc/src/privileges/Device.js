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

    viewDevice:['privilege.administrate.deviceData','privilege.view.device'],
    deviceOperator: ['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    addDevice:['privilege.add.device'],
    flagDevice:['privilege.administrate.deviceData','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    adminDeviceData:['privilege.administrate.deviceData'],
    viewDeviceCommunication:['privilege.administrate.deviceData','privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    administrateDeviceCommunication:['privilege.administrate.deviceCommunication'],

    any: function() {
        return Ext.Array.merge(Mdc.privileges.Device.viewDevice,
            Mdc.privileges.Device.deviceOperator,
            Mdc.privileges.Device.addDevice,
            Mdc.privileges.Device.adminDeviceData,
            Mdc.privileges.Device.viewDeviceCommunication,
            Mdc.privileges.Device.administrateDeviceCommunication
        );
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.view );
    }
});
