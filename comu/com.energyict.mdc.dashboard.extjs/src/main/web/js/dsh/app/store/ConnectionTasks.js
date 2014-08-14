Ext.define('Dsh.store.ConnectionTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.ConnectionTask'
    ],
    model: 'Dsh.model.ConnectionTask',
    proxy: {
        type: 'ajax',
        url: '../../apps/dashboard/app/fakeData/ConnectionTasksFake.json',
        reader: {
            type: 'json',
            root: 'connectionTasks'
        }
    }
});

