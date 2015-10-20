Ext.define('Bpm.store.process.Processes', {
    extend: 'Ext.data.Store',
    model: 'Bpm.model.process.Process',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/processes',
        reader: {
            type: 'json',
            root: 'processes'
        }
    }
});
