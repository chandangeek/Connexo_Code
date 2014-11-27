Ext.define('Dsh.store.filter.CommunicationTask', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comtasks',
        reader: {
            type: 'json',
            root: 'comTasks'
        }
    }
});
