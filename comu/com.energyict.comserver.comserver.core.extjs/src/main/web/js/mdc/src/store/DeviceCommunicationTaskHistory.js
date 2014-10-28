Ext.define('Mdc.store.DeviceCommunicationTaskHistory', {
    extend: 'Ext.data.Store',
    storeId: 'DeviceCommunicationTaskHistory',
    requires: ['Mdc.model.DeviceCommunicationTaskHistory'],
    model: 'Mdc.model.DeviceCommunicationTaskHistory',
    pageSize: 10,

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mRID}/comtasks/{comTaskId}/comtaskexecutionsessions',
        reader: {
            type: 'json',
            root: 'comTaskExecutionSessions'
        }
    }
});
