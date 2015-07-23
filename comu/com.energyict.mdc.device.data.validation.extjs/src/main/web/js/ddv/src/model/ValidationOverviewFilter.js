Ext.define('Ddv.model.ValidationOverviewFilter', {
    extend: 'Ext.data.Model',
    requires: ['Uni.data.proxy.QueryStringProxy'],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        {name: 'id', persist: false},
        {name: 'deviceGroup', type: 'auto'}
    ]
});