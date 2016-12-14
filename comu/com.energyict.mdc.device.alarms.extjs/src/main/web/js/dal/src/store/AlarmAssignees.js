Ext.define('Dal.store.AlarmAssignees', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Dal.model.AlarmAssignee',
    pageSize: 50,
    autoLoad: false
});