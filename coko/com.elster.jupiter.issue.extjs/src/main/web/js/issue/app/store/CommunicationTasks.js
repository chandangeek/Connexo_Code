Ext.define('Isu.store.CommunicationTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Uni.component.sort.store.Sortable'
    ],
    model: 'Isu.model.CommunicationTasks',
    pageSize: 10,
    autoLoad: false
});