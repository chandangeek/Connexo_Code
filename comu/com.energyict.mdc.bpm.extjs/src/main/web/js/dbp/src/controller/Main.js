Ext.define('Dbp.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems'
    ],

    controllers: [
        'Dbp.controller.History',
        'Dbp.deviceprocesses.controller.DeviceProcesses',
        'Dbp.deviceprocesses.controller.StartProcess'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Dbp.controller.History'); // Forces route registration.

        me.getApplication().fireEvent('cfginitialized');
    }
});