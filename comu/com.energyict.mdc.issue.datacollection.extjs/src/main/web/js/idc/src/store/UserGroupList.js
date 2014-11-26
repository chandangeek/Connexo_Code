Ext.define('Idc.store.UserGroupList', {
    extend: 'Ext.data.Store',
    model: 'Idc.model.UserGroupList',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/groups',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});