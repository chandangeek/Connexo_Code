Ext.define('Usr.store.Users', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.User',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '/api/usr/users',
        reader: {
            type: 'json',
            root: 'users'
        }
    }
});