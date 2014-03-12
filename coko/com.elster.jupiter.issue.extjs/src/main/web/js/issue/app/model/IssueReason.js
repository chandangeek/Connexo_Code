Ext.define('Isu.model.IssueReason', {
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
        url: '/apps/issue/fakedata/reason.json',
//        url: '/api/isu/filter/reason',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});