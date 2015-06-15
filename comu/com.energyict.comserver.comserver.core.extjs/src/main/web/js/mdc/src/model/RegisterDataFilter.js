Ext.define('Mdc.model.RegisterDataFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        {name: 'intervalStart', type: 'date', dateFormat: 'Y-m-dTH:i:s'},
        {name: 'duration'},
        {name: 'onlySuspect'},
        {name: 'onlyNonSuspect'}
    ]

});
