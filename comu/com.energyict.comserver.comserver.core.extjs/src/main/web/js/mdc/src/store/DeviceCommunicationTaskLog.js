Ext.define('Mdc.store.DeviceCommunicationTaskLog', {
    extend: 'Uni.data.store.Filterable',
    storeId: 'deviceCommunicationTaskLog',
    requires: ['Mdc.model.DeviceCommunicationTaskLog'],
    model: 'Mdc.model.DeviceCommunicationTaskLog',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/comtasks/{comTaskId}/comtaskexecutionsessions/{sessionId}/journals',
        timeout: 250000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'comTaskExecutionSessions'
        }
    }
});