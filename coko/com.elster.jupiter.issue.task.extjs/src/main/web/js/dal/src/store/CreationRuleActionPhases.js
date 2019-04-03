Ext.define('Itk.store.CreationRuleActionPhases', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.CreationRuleActionPhase',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/itk/actions/phases',
        reader: {
            type: 'json',
            root: 'creationRuleActionPhases'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});