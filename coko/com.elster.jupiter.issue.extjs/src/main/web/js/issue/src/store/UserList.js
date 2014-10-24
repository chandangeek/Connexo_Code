Ext.define('Isu.store.UserList', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.UserList',
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