Ext.define('Dsh.store.CommunicationTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.CommunicationTask'
    ],
    model: 'Dsh.model.CommunicationTask',
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


