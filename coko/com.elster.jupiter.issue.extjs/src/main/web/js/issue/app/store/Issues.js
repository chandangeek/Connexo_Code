Ext.define('Mtr.store.Issues', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Mtr.model.Issues',
    pageSize: 10,
    autoLoad: false,

    proxy: {

        type: 'rest',
        url: '/api/isu/issue',
        reader: {
            type: 'json',
            root: 'issueList'
        },
        extraParams: {
            sort: 'dueDate',
            order: 'asc'
        }
    }
});