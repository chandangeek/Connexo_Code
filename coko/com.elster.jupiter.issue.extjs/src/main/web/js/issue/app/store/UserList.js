Ext.define('Mtr.store.UserList', {
    extend: 'Ext.data.Store',
    model: 'Mtr.model.UserList',
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