Ext.define('Mdc.model.RegisterDataFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [ 'onlySuspect', 'onlyNonSuspect' ]
});
