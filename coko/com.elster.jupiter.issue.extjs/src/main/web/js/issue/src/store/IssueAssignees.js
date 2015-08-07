Ext.define('Isu.store.IssueAssignees', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.IssueAssignee',
    pageSize: 50,
    autoLoad: false
});