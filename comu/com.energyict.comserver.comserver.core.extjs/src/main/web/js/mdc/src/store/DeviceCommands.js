Ext.define('Mdc.store.DeviceCommands', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceCommand'
    ],
    model: 'Mdc.model.DeviceCommand',
    url: '/api/ddr/devices/',
    commandsPostfix: '/devicemessages',
    setMrid: function (mrid) {
        var me = this;
        me.getProxy().url = me.url + encodeURIComponent(mrid) + me.commandsPostfix;
        return me
    }
});