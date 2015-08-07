Ext.define('Idc.store.Issues', {
    extend: 'Ext.data.Store',
    model: 'Idc.model.Issue',
    pageSize: 10,
    autoLoad: false
});