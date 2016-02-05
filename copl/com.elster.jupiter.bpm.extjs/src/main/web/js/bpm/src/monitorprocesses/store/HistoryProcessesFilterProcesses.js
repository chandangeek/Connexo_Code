Ext.define('Bpm.monitorprocesses.store.HistoryProcessesFilterProcesses', {
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
        {name: 'deploymentId', type: 'string'},
        {
            name: 'process',
            type: 'string',
            convert: function (value, record) {
                return record.get('id').replace(' (' +record.get('deploymentId')+') ', '');
            }
        }
    ]
});
