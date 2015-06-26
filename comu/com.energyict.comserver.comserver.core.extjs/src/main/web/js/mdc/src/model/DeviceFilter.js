Ext.define('Mdc.model.DeviceFilter', {
    extend: 'Ext.data.Model',

    requires: [
        'Uni.data.proxy.QueryStringProxy',
        'Ext.data.proxy.Memory'
    ],

    proxy: {
        type: 'querystring',
        root: 'filter'
    },

    fields: [
        {name: 'mRID', type: 'string'},
        {name: 'serialNumber', type: 'string'},
        {name: 'deviceTypes', type: 'auto'},
        {name: 'deviceConfigurations', type: 'auto'}
    ]
});