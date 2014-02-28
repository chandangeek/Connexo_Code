Ext.define('Mtr.store.UserRoleList', {
    extend: 'Ext.data.Store',
    model: 'Mtr.model.UserRoleList',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/isu/assign/roles',
        reader: {
            type: 'json',
            root: 'assignees'
        }
    }
});