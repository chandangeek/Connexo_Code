/**
 * @class Mdc.privileges.DeviceGroup
 *
 * Class that defines privileges for DeviceGroup
 */
Ext.define('Mdc.privileges.DeviceGroup', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.deviceGroup', 'privilege.administrate.deviceOfEnumeratedGroup', 'privilege.view.deviceGroupDetail'],
    viewGroupDetails : [ 'privilege.view.deviceGroupDetail'],
    adminDeviceGroup: ['privilege.administrate.deviceGroup'],
    adminDeviceOfEnumeratedGroup:['privilege.administrate.deviceOfEnumeratedGroup'],
    any: function() {
        return Ext.Array.merge(Mdc.privileges.DeviceGroup.view,
            Mdc.privileges.DeviceGroup.adminDeviceGroup,
            Mdc.privileges.DeviceGroup.adminDeviceOfEnumeratedGroup
        );
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.DeviceGroup.view );
    },
    canViewGroupDetails:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.DeviceGroup.viewGroupDetails );
    },
    canAdministrateDeviceGroup:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.DeviceGroup.adminDeviceGroup );
    },
    canAdministrateDeviceOfEnumeratedGroup:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.DeviceGroup.adminDeviceOfEnumeratedGroup );
    }
});
