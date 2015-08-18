Ext.define('Mdc.store.DeviceCommands', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceCommand'
    ],
    model: 'Mdc.model.DeviceCommand',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/devicemessages',
        reader: {
            type: 'json',
            root: 'deviceMessages'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});