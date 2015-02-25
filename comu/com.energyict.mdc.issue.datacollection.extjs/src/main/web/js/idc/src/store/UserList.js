Ext.define('Idc.store.UserList', {
    extend: 'Ext.data.Store',
    model: 'Idc.model.UserList',
    autoLoad: false,

    proxy: {
        type: 'rest',
        timeout: 60000,
        pageParam: false,
        startParam: false,
        limitParam: false,
        url: '/api/usr/users',
        reader: {
            type: 'json',
            root: 'users'
        }
    }
});