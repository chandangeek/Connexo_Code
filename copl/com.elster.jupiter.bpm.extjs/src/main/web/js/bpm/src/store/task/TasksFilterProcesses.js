Ext.define('Bpm.store.task.TasksFilterProcesses', {
    extend: 'Ext.data.Store',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/avaiableactiveprocesses',
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
        {name: 'name',  type: 'string'},
        {name: 'id',  type: 'string'},
        {name: 'version',  type: 'string'},
        {
            name: 'displayName',
            type: 'string',
            convert: function (value, record) {
                return Ext.String.format('{0} ({1})', record.get('name'), record.get('version'));
            }
        }
    ]
});
