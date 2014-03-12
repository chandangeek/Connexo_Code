Ext.define('Isu.model.IssueStatus', {
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
        url: '/apps/issue/fakedata/status.json',
//        url: '/api/isu/filter/status',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});