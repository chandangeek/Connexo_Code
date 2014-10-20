Ext.define('Mdc.model.ConnectionLogFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'logLevels', type: 'auto' },
        { name: 'logTypes', type: 'auto' }
    ]
});