Ext.define('Mdc.model.TopologyFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'mrid', type: 'auto' },
        { name: 'sn', type: 'auto' },
        { name: 'type', type: 'auto' },
        { name: 'configuration', type: 'auto' }
    ]
});