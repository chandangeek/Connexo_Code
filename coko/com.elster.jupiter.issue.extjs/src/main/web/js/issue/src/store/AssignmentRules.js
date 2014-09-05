Ext.define('Isu.store.AssignmentRules', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.AssignmentRules',
    pageSize: 100,
    autoLoad: false
});
