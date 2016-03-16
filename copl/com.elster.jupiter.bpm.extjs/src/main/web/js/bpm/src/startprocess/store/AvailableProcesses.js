Ext.define('Bpm.startprocess.store.AvailableProcesses', {
    extend: 'Ext.data.Store',
    model: 'Bpm.startprocess.model.AvailableProcess',

    autoLoad: false,
    pageSize: 1000,
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/activeprocesses',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processes'
        }
    }
});