Ext.define('Isu.store.UserList', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.UserList',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/usr/users',
        reader: {
            type: 'json',
            root: 'users'
        }
    }
});