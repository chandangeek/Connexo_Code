Ext.define('Idc.store.UserList', {
    extend: 'Ext.data.Store',
    model: 'Idc.model.UserList',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/usr/users',
        reader: {
            type: 'json',
            root: 'users'
        }
    }
});