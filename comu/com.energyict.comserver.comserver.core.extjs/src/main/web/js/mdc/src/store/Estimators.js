Ext.define('Mdc.store.Estimators', {
    extend: 'Ext.data.Store',
    requires: [
        'Est.estimationrules.model.Estimator'
    ],
    model: 'Est.estimationrules.model.Estimator',
    proxy: {
        type: 'rest',
        url: '/api/est/estimation/estimators',
        reader: {
            type: 'json',
            root: 'estimators',
            totalProperty: 'total'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
