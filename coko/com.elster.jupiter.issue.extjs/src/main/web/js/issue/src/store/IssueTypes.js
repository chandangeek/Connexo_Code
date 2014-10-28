Ext.define('Isu.store.IssueTypes', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueType',
    pageSize: 10,
    autoLoad: false
});