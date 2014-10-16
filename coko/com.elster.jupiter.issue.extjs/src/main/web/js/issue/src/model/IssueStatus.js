Ext.define('Isu.model.IssueStatus', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
        {
            name: 'id',
            type: 'auto'
        },
        {
            name: 'name',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/statuses',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});