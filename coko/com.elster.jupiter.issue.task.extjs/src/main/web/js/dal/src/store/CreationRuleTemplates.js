Ext.define('Itk.store.CreationRuleTemplates', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.CreationRuleTemplate',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/itk/rules/templates',
        reader: {
            type: 'json',
            root: 'creationRuleTemplates'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});