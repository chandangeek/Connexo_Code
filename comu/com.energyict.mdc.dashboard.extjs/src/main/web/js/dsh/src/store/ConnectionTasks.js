Ext.define('Dsh.store.ConnectionTasks', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Dsh.model.ConnectionTask',
        'Dsh.util.FilterStoreHydrator',
        'Ext.grid.plugin.BufferedRenderer'
    ],
    model: 'Dsh.model.ConnectionTask',
    hydrator: 'Dsh.util.FilterStoreHydrator',
    autoLoad: false,
    remoteFilter: true,
    buffered: true,
    pageSize: 20,
    leadingBufferZone: 5,
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

