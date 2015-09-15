Ext.define('Dsh.store.filter.CurrentState', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'taskStatus'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/taskstatus',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'taskStatuses'
        }
    }
});

