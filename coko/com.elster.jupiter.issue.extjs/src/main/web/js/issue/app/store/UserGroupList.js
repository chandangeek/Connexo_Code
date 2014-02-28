Ext.define('Mtr.store.UserGroupList', {
    extend: 'Ext.data.Store',
    model: 'Mtr.model.UserGroupList',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/isu/assign/groups',
        reader: {
            type: 'json',
            root: 'assignees'
        }
    }
});