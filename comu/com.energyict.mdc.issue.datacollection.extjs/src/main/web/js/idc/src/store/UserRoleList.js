Ext.define('Idc.store.UserRoleList', {
    extend: 'Ext.data.Store',
    model: 'Idc.model.UserRoleList',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/roles',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});