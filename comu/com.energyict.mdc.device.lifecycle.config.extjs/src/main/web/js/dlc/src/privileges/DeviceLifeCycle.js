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
    all: function() {
        return Ext.Array.merge(Dlc.privileges.DeviceLifeCycle.configure);
    },
    canConfigure : function (){
        return Uni.Auth.checkPrivileges(Dlc.privileges.DeviceLifeCycle.configure);
    }
});