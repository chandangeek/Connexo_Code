Ext.define('Bpm.processes.store.Processes', {
    extend: 'Ext.data.Store',
    model: 'Bpm.processes.model.Process',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/allprocesses',
        reader: {
            type: 'json',
            root: 'processes'
        }
    }
});
