Ext.define('Mdc.model.DeviceFilter', {
    extend: 'Ext.data.Model',

    requires: [
        'Uni.data.proxy.QueryStringProxy'
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
        /*,
         {name: 'deviceTypeId', type: 'number'},
         {name: 'deviceTypeName', type: 'string'},
         {name: 'deviceConfigurationId', type: 'number'},
         {name: 'deviceConfigurationName', type: 'string'}*/
    ]
});