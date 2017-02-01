Ext.define('Dal.store.CreationRules', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.CreationRule',
    pageSize: 10,
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/creationrules',
        reader: {
            type: 'json',
            root: 'creationRules'
        }
    }
});
