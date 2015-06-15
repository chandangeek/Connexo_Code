Ext.define('Isu.store.IssueActions', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Action',
    autoLoad: false,
    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'issueActions'
        }
    }
});