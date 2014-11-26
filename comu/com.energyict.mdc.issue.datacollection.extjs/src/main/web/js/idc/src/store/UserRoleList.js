Ext.define('Idc.store.UserRoleList', {
    extend: 'Ext.data.Store',
    model: 'Idc.model.UserRoleList',
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