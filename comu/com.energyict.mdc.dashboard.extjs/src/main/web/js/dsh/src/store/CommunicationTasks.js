Ext.define('Dsh.store.CommunicationTasks', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Dsh.model.CommunicationTask',
        'Dsh.util.FilterStoreHydrator'
    ],
    model: 'Dsh.model.CommunicationTask',
    hydrator: 'Dsh.util.FilterStoreHydrator',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/communications',
        reader: {
            type: 'json',
            root: 'communicationTasks',
            totalProperty: 'count'
        }
    }
});


