Ext.define('Dal.store.CreationRuleActionPhases', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.CreationRuleActionPhase',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/actions/phases',
        reader: {
            type: 'json',
            root: 'creationRuleActionPhases'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});