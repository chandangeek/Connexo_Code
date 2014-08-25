Ext.define('Dsh.store.ConnectionTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.ConnectionTask'
    ],
    model: 'Dsh.model.ConnectionTask',
    proxy: {
        type: 'ajax',
     //   type: 'rest',
        url: '../../apps/dashboard/app/fakeData/ConnectionTasksFake.json',
  //      url: '/api/dsr/connections',
        reader: {
            type: 'json',
            root: 'connectionTasks',
            totalProperty: 'total'
        }
    }
});

