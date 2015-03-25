Ext.define('Fwc.store.Firmwares', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Fwc.model.Firmware'
    ],
    model: 'Fwc.model.Firmware',
    storeId: 'Firmwares',
    autoLoad: false,
    remoteSort: true,
    sorters: [{
        property: 'version',
        direction: 'DESC'
    }],

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devicetypes/{deviceTypeId}/firmwares',
        reader: {
            type: 'json',
            root: 'firmwares',
            totalProperty: 'total'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});
