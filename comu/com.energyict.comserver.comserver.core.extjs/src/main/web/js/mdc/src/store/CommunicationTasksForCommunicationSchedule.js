Ext.define('Mdc.store.CommunicationTasksForCommunicationSchedule',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationTask'
    ],
    model: 'Mdc.model.CommunicationTask',
    storeId: 'CommunicationTasksForCommunicationSchedule',
    remoteSort: true

});