Ext.define('Usr.store.WorkgroupUsers', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.WorkgroupUser',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/usr/workgroups/users',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'users'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});