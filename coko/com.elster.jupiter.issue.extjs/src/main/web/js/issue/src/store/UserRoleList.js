Ext.define('Isu.store.UserRoleList', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.UserRoleList',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/roles',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});