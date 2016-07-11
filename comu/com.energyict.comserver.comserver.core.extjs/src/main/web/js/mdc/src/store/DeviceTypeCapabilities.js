Ext.define('Mdc.store.DeviceTypeCapabilities', {
    extend: 'Ext.data.Store',

    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{id}/capabilities',

        reader: {
            type: 'json',
            root: 'capabilities'
        },

        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(params) {
            this.url = this.urlTpl.replace('{id}', params.deviceTypeId);
        }

    }
});