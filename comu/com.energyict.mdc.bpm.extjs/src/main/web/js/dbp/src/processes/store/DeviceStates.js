Ext.define('Dbp.processes.store.DeviceState', {
    extend: 'Ext.data.Store',
    model: 'Dbp.processes.model.DeviceState',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/process/deviceStates',
        reader: {
            type: 'json',
            root: 'deviceStates'
        }
    }
});
