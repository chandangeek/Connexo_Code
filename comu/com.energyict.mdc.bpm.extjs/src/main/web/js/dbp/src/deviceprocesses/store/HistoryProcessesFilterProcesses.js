Ext.define('Dbp.deviceprocesses.store.HistoryProcessesFilterProcesses', {
    extend: 'Ext.data.Store',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/processes',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    },

    fields: [
        {name: 'name', type: 'string'},
        {name: 'id', type: 'string'},
        {name: 'deploymentId', type: 'string'}
    ]
});
