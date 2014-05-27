Ext.define('Mdc.store.CommunicationTasks',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationTask'
    ],
    model: 'Mdc.model.CommunicationTask',
    storeId: 'CommunicationTasks',
    pageSize: 10,
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/cts/comtasks',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});