Ext.define('Mdc.model.LogbookOfDeviceDataFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.Domain',
        'Mdc.model.Subdomain',
        'Mdc.model.EventOrAction',
        'Uni.data.proxy.QueryStringProxy'
    ],
    fields: [
        {name: 'intervalStart', type: 'date', dateFormat: 'Y-m-dTH:i:s'},
        {name: 'intervalEnd', type: 'date', dateFormat: 'Y-m-dTH:i:s'},
        {name: 'domain', type: 'auto'},
        {name: 'subDomain', type: 'auto'},
        {name: 'eventOrAction', type: 'auto'}
    ],

    proxy: {
        type: 'querystring',
        root: 'filter'
    }
});