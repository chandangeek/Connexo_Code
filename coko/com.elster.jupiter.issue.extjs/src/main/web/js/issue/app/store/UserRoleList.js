Ext.define('Isu.store.UserRoleList', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.UserRoleList',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/isu/assign/roles',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});