Ext.define('Bpm.processes.store.Associations', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'type', type: 'string'},
        {name: 'name', type: 'string'}
    ],
    proxy: {
        type: 'rest',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        url: '/api/bpm/runtime/process/associations',
        reader: {
            type: 'json',
            root: 'associations'
        }
    }



});
