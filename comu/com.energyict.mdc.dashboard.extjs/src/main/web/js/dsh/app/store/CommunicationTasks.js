Ext.define('Dsh.store.CommunicationTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.CommunicationTask'
    ],
    model: 'Dsh.model.CommunicationTask',
    proxy: {
        type: 'ajax',
        url: '../../apps/dashboard/app/fakeData/CommunicationTasksFake.json',
        reader: {
            type: 'json',
            root: 'communicationsTasks',
            totalProperty: 'count'
        }
    }
});


