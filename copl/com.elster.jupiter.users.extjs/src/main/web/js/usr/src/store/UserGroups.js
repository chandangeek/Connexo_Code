Ext.define('Usr.store.UserGroups', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.Group',
    pageSize: 100,
    proxy: {
        type: 'rest',
        url: '/api/usr/groups',
        reader: {
            type: 'json',
            root: 'groups'
        }
    }
});