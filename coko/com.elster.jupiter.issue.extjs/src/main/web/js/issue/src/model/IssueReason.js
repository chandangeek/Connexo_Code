Ext.define('Isu.model.IssueReason', {
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
        url: '/api/isu/reasons',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});