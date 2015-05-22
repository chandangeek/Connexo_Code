Ext.define('Idv.store.UserList', {
    extend: 'Ext.data.Store',
    model: 'Idv.model.UserList',
    autoLoad: false,

    proxy: {
        type: 'rest',
        timeout: 60000,
        pageParam: false,
        startParam: false,
        limitParam: false,
        url: '/api/isu/assignees/users',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});