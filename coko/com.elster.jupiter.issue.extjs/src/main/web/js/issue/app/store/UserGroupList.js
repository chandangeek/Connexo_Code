Ext.define('Isu.store.UserGroupList', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.UserGroupList',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/isu/assign/groups',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});