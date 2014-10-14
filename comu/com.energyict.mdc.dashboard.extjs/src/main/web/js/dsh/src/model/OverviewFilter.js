Ext.define('Dsh.model.OverviewFilter', {
    extend: 'Ext.data.Model',
    requires: ['Uni.data.proxy.QueryStringProxy'],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'deviceGroup', type: 'auto' }
    ]
});