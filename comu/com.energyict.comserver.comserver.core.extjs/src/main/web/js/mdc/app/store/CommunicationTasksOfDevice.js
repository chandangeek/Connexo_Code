Ext.define('Mdc.store.CommunicationTasksOfDevice', {
    extend: 'Ext.data.Store',
    storeId: 'CommunicationTasksOfDevice',
    model: 'Mdc.model.DeviceCommunicationTask',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mrid}/comtasks',
        reader: {
            type: 'json',
            root: 'comTasks'
        }
    }
});


