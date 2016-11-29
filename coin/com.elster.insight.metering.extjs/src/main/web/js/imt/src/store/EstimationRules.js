Ext.define('Imt.store.EstimationRules', {
    extend: 'Ext.data.Store',
    requires: [
        'Est.estimationrules.model.Rule'
    ],
    model: 'Est.estimationrules.model.Rule',
    proxy: {
        type: 'rest',
        url: '/api/est/estimation/{ruleSetId}/rules',
        reader: {
            type: 'json',
            root: 'rules',
            totalProperty: 'total'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});

