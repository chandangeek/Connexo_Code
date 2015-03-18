Ext.define('Fwc.model.FirmwareFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy',
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'type', type: 'auto' },
        { name: 'status', type: 'auto' }
    ]
});