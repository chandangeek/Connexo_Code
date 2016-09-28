Ext.define('Mdc.store.CommunicationTasksOfDevice', {
    extend: 'Ext.data.Store',
    storeId: 'CommunicationTasksOfDevice',
    model: 'Mdc.model.DeviceCommunicationTask',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/comtasks',
        reader: {
            type: 'json',
            root: 'comTasks'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});


