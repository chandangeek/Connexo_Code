Ext.define('Isu.store.CreationRuleActionPhases', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRuleActionPhases',
    pageSize: 50,
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/actions/phases',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});