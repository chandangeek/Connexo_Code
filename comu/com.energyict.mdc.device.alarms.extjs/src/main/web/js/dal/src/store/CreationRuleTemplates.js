Ext.define('Dal.store.CreationRuleTemplates', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.CreationRuleTemplate',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/dal/rules/templates',
        reader: {
            type: 'json',
            root: 'creationRuleTemplates'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});