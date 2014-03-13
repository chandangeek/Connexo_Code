Ext.define('Isu.store.Assignee', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Assignee',
    pageSize: 100,
    autoLoad: false
});
