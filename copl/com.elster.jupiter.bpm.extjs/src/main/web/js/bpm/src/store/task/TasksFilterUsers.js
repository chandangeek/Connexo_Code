Ext.define('Bpm.store.task.TasksFilterUsers', {
    extend: 'Ext.data.Store',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/usr/users',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'users'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    },

    fields: [
        {name: 'id',    type: 'int'},
        {name: 'name',  type: 'string'}
    ]
});
