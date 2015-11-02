Ext.define('Dbp.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems',
        'Mdc.dynamicprivileges.DeviceState',
        'Mdc.dynamicprivileges.Stores'
    ],

    controllers: [
        'Dbp.controller.History',
        'Dbp.deviceprocesses.controller.DeviceProcesses'
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