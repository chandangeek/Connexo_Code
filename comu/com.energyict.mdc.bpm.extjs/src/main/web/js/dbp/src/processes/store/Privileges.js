Ext.define('Dbp.processes.store.Privileges', {
    extend: 'Ext.data.Store',
    model: 'Dbp.processes.model.Privilege',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/process/privileges',
        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});
