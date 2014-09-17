Ext.define('Dsh.store.ConnectionTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.ConnectionTask'
    ],
    model: 'Dsh.model.ConnectionTask',
    remoteFilter: true,
    sorters: [
        {
            direction: 'DESC',
            property: 'startDateTime'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dsr/connections',
        reader: {
            type: 'json',
            root: 'connectionTasks',
            totalProperty: 'total'
        }
    }
});

