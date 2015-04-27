Ext.define('Fwc.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [],

    controllers: [
        'Fwc.controller.History',
        'Fwc.controller.Firmware',
        'Fwc.devicefirmware.controller.DeviceFirmware'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Fwc.controller.History'); // Forces route registration.
        var router = this.getController('Uni.controller.history.Router');

        me.getApplication().fireEvent('cfginitialized');
    }
});
