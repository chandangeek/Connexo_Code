Ext.define('Isu.store.CreationRuleTemplates', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRuleTemplate',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/templates',
        reader: {
            type: 'json',
            root: 'creationRuleTemplates'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});