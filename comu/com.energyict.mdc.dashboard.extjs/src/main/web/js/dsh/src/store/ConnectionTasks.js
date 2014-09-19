Ext.define('Dsh.store.ConnectionTasks', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Dsh.model.ConnectionTask'
    ],
    model: 'Dsh.model.ConnectionTask',
    autoLoad: false,
    remoteFilter: true,
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

