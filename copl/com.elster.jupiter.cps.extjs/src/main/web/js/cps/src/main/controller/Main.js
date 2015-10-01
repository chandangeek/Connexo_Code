Ext.define('Cps.main.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems'
    ],

    controllers: [
        'Cps.main.controller.History',
        'Cps.customattributesets.controller.AttributeSets'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Cps.main.controller.History'); // Forces route registration.

        me.getApplication().fireEvent('cfginitialized');
    }
});