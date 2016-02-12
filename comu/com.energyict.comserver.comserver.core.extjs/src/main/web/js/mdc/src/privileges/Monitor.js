/**
 * @class Mdc.privileges.Monitor
 *
 * Class that defines privileges for the communication server monitor
 */
Ext.define('Mdc.privileges.Monitor', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    all: function() {
        return this.canMonitor();
    },
    canMonitor: function(){
        return Uni.Auth.checkPrivileges(['privilege.monitor.communication.server']);
    }
});
