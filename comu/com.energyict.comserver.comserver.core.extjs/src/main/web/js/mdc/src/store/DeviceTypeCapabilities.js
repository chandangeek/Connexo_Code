Ext.define('Mdc.store.DeviceTypeCapabilities', {
    extend: 'Ext.data.Store',

    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceTypeId}/capabilities',

        reader: {
            type: 'json',
            root: 'capabilities'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});