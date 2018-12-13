Ext.define('Dal.store.CreationRuleActionPhases', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.CreationRuleActionPhase',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/dal/actions/phases',
        reader: {
            type: 'json',
            root: 'creationRuleActionPhases'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});