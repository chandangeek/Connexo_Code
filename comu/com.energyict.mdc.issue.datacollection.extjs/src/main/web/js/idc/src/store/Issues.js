Ext.define('Idc.store.Issues', {
    extend: 'Uni.data.store.Filterable',
    model: 'Idc.model.Issue',
    pageSize: 10,
    autoLoad: false
});