Ext.define('Isu.model.IssueMeter', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/meters',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});